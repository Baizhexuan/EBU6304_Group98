import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AIConversationService {
    private static final int CONNECT_TIMEOUT_MS = 6000;
    private static final int READ_TIMEOUT_MS = 20000;
    private static final int MAX_CONTEXT_CHARS = 5000;

    private AIConversationService() {
    }

    public static String ask(String question, String context) {
        if (ValidationUtils.isBlank(question)) {
            return "Please enter a question about TA matching, workload balancing, or applicant screening.";
        }
        if (!isConfigured()) {
            return buildLocalAnswer(question, context);
        }
        try {
            String response = sendResponsesRequest(question, context);
            if (ValidationUtils.notBlank(response)) {
                return "External AI model response (" + getModelName() + ")\n\n" + response.trim();
            }
            return buildLocalAnswer(question, context) + "\n\nExternal model fallback reason: empty response text.";
        } catch (Exception ex) {
            return buildLocalAnswer(question, context)
                    + "\n\nExternal model fallback reason: " + summariseError(ex.getMessage());
        }
    }

    public static boolean isConfigured() {
        return ValidationUtils.notBlank(System.getenv("OPENAI_API_KEY"));
    }

    public static String buildStatusText() {
        if (!isConfigured()) {
            return "Local fallback mode: set OPENAI_API_KEY to call the external model.";
        }
        return "External model ready: " + getModelName() + " via " + getBaseUrl() + "/responses";
    }

    private static String sendResponsesRequest(String question, String context) throws IOException {
        URL url = new URL(getBaseUrl() + "/responses");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));

        String payload = buildResponsesPayload(question, context);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        String body = readFully(stream);
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " - " + summariseError(body));
        }
        return extractResponsesText(body);
    }

    private static String buildResponsesPayload(String question, String context) {
        String instructions = "You are an explainable AI assistant for a university Teaching Assistant recruitment system. "
                + "Use the provided system context only as decision support. Do not make final hiring decisions blindly. "
                + "Return concise advice with: recommendation, evidence, risks, and next action.";
        String input = "Current recruitment context:\n" + limit(safe(context), MAX_CONTEXT_CHARS)
                + "\n\nUser question:\n" + question.trim();

        return "{"
                + "\"model\":\"" + escapeJson(getModelName()) + "\","
                + "\"instructions\":\"" + escapeJson(instructions) + "\","
                + "\"input\":\"" + escapeJson(input) + "\","
                + "\"max_output_tokens\":700"
                + "}";
    }

    private static String extractResponsesText(String responseBody) {
        String outputText = extractJsonString(responseBody, "output_text");
        if (ValidationUtils.notBlank(outputText)) {
            return outputText;
        }
        String text = extractJsonString(responseBody, "text");
        if (ValidationUtils.notBlank(text)) {
            return text;
        }
        return responseBody;
    }

    private static String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int index = json.indexOf(marker);
        if (index < 0) {
            return "";
        }
        int start = index + marker.length();
        StringBuilder value = new StringBuilder();
        boolean escaping = false;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaping) {
                if (ch == 'n') {
                    value.append('\n');
                } else if (ch == 'r') {
                    value.append('\r');
                } else if (ch == 't') {
                    value.append('\t');
                } else {
                    value.append(ch);
                }
                escaping = false;
                continue;
            }
            if (ch == '\\') {
                escaping = true;
                continue;
            }
            if (ch == '"') {
                break;
            }
            value.append(ch);
        }
        return value.toString().trim();
    }

    private static String buildLocalAnswer(String question, String context) {
        return "Local AI assistant mode\n"
                + "Question: " + question.trim() + "\n\n"
                + "Structured recommendation:\n"
                + "1. Review workload risk before making another selection.\n"
                + "2. Compare match score with missing skills and current hours.\n"
                + "3. Prefer candidates who remain under " + FileStorage.getOverloadLimit() + " hours after assignment.\n"
                + "4. Record a reviewer note explaining why the final choice was made.\n\n"
                + "Current system context:\n" + safe(context);
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString().trim();
    }

    private static String getBaseUrl() {
        String value = System.getenv("OPENAI_BASE_URL");
        return ValidationUtils.notBlank(value) ? trimTrailingSlash(value.trim()) : "https://api.openai.com/v1";
    }

    private static String getModelName() {
        String value = System.getenv("OPENAI_MODEL");
        return ValidationUtils.notBlank(value) ? value.trim() : "gpt-4o-mini";
    }

    private static String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "\n[Context truncated for model request]";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", " ");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String summariseError(String message) {
        if (ValidationUtils.isBlank(message)) {
            return "unknown API error";
        }
        return message.length() > 180 ? message.substring(0, 180) + "..." : message;
    }
}
