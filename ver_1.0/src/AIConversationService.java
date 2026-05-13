import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AIConversationService {
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS = 10000;

    private AIConversationService() {
    }

    public static String ask(String question, String context) {
        if (ValidationUtils.isBlank(question)) {
            return "Please enter a question about TA matching, workload balancing, or applicant screening.";
        }
        if (!ValidationUtils.notBlank(System.getenv("OPENAI_API_KEY"))) {
            return buildLocalAnswer(question, context);
        }
        try {
            return sendChatCompletion(question, context);
        } catch (Exception ex) {
            return buildLocalAnswer(question, context)
                    + "\n\nExternal model fallback reason: " + summariseError(ex.getMessage());
        }
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

    private static String sendChatCompletion(String question, String context) throws IOException {
        URL url = new URL(getBaseUrl() + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));

        String prompt = "You are assisting a university TA recruitment system. Give concise, explainable advice. "
                + "Never make a final hiring decision blindly; explain trade-offs.\n\n"
                + "Context:\n" + safe(context) + "\n\nQuestion:\n" + question.trim();
        String payload = "{"
                + "\"model\":\"" + escapeJson(getModelName()) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"You support explainable TA recruitment decisions.\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}"
                + "]}";
        try (OutputStream output = connection.getOutputStream()) {
            output.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        String body = readFully(stream);
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " - " + body);
        }
        return extractContent(body);
    }

    private static String extractContent(String responseBody) {
        String marker = "\"content\":\"";
        int index = responseBody.indexOf(marker);
        if (index < 0) {
            return responseBody;
        }
        int start = index + marker.length();
        StringBuilder content = new StringBuilder();
        boolean escaping = false;
        for (int i = start; i < responseBody.length(); i++) {
            char ch = responseBody.charAt(i);
            if (escaping) {
                content.append(ch == 'n' ? '\n' : ch);
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else if (ch == '"') {
                break;
            } else {
                content.append(ch);
            }
        }
        return content.toString().trim();
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
        return ValidationUtils.notBlank(value) ? value.trim() : "https://api.openai.com/v1";
    }

    private static String getModelName() {
        String value = System.getenv("OPENAI_MODEL");
        return ValidationUtils.notBlank(value) ? value.trim() : "gpt-4o-mini";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", " ");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String summariseError(String message) {
        if (ValidationUtils.isBlank(message)) {
            return "unknown API error";
        }
        return message.length() > 120 ? message.substring(0, 120) + "..." : message;
    }
}
