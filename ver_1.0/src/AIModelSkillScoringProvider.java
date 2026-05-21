import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Optional external scoring provider for live AI-backed skill matching.
 *
 * <p>The class is deliberately conservative for coursework use: it accepts local configuration,
 * makes a short network call to a compatible chat-completions endpoint, parses a compact plain
 * text result, and falls back cleanly when configuration or connectivity is unavailable.</p>
 */
public class AIModelSkillScoringProvider implements SkillScoringProvider {
    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 6000;

    /**
     * Creates an AI-backed scoring provider using the current local configuration.
     */
    public AIModelSkillScoringProvider() {
    }

    /**
     * Evaluates TA-to-job skill compatibility using an external AI model.
     *
     * <p>Falls back to the local {@link MatchingService} result when the
     * model is not configured or the network call fails.</p>
     *
     * @param profile the TA's academic and skills profile
     * @param job     the target job posting
     * @return a {@link MatchResult} containing a numeric score and explanation
     */
    @Override
    public MatchResult evaluate(TAProfile profile, Job job) {
        MatchResult fallback = MatchingService.evaluate(profile, job);
        if (profile == null || job == null) {
            return new MatchResult(fallback.score, fallback.summary + " | AI placeholder received incomplete input.");
        }
        if (!isReady()) {
            return new MatchResult(fallback.score,
                    fallback.summary + " | AI placeholder ready but inactive: " + getStatusDescription());
        }

        try {
            String response = sendChatCompletion(profile, job, fallback);
            MatchResult parsed = parseModelResponse(response, fallback);
            return new MatchResult(parsed.score, parsed.summary + " | Source: external AI placeholder call");
        } catch (Exception ex) {
            return new MatchResult(fallback.score,
                    fallback.summary + " | AI fallback engaged: " + summariseError(ex.getMessage()));
        }
    }

    /**
     * Returns the provider identifier including the configured model name.
     *
     * @return provider name string
     */
    @Override
    public String getProviderName() {
        return "AIModelSkillScoringProvider(" + getModelName() + ")";
    }

    /**
     * Indicates that this provider delegates scoring to an external model.
     *
     * @return {@code true} always
     */
    @Override
    public boolean isExternalModel() {
        return true;
    }

    /**
     * Returns {@code true} when an API key is configured for live scoring.
     *
     * @return {@code true} if the API key environment variable is set
     */
    @Override
    public boolean isReady() {
        return ValidationUtils.notBlank(getApiKey());
    }

    /**
     * Returns a human-readable description of the current provider status.
     *
     * @return status string suitable for display in the Admin dashboard
     */
    @Override
    public String getStatusDescription() {
        if (!isReady()) {
            return "Set OPENAI_API_KEY and optionally AI_SCORING_MODE=AI to enable live scoring.";
        }
        return "Live placeholder calls target " + getBaseUrl() + "/chat/completions using model " + getModelName() + ".";
    }

    private String sendChatCompletion(TAProfile profile, Job job, MatchResult fallback) throws IOException {
        URL url = new URL(getBaseUrl() + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + getApiKey());

        String payload = buildRequestPayload(profile, job, fallback);
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

    private String buildRequestPayload(TAProfile profile, Job job, MatchResult fallback) {
        String prompt = "You are assisting a university TA recruitment demo. Evaluate the fit between a TA and a job. "
                + "Reply in exactly this format: SCORE: <0-100>\\nSUMMARY: <one concise sentence>.\\n"
                + "TA skills: " + safe(profile.skills) + "\\n"
                + "TA GPA: " + profile.gpa + "\\n"
                + "TA availability: " + safe(profile.availability) + "\\n"
                + "Job title: " + safe(job.title) + "\\n"
                + "Module: " + safe(job.module) + "\\n"
                + "Required skills: " + safe(job.requiredSkills) + "\\n"
                + "Location: " + safe(job.location) + "\\n"
                + "Local fallback score: " + fallback.score + "\\n"
                + "Local fallback summary: " + safe(fallback.summary);

        return "{"
                + "\"model\":\"" + escapeJson(getModelName()) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"You score TA recruitment matches for a demo system.\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}"
                + "]}";
    }

    private MatchResult parseModelResponse(String content, MatchResult fallback) {
        if (ValidationUtils.isBlank(content)) {
            return new MatchResult(fallback.score, fallback.summary + " | AI returned an empty response.");
        }

        int score = fallback.score;
        String summary = fallback.summary;
        String[] lines = content.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toUpperCase().startsWith("SCORE:")) {
                score = clamp(ValidationUtils.parseInt(trimmed.substring(6).trim(), fallback.score));
            } else if (trimmed.toUpperCase().startsWith("SUMMARY:")) {
                summary = trimmed.substring(8).trim();
            }
        }

        if (summary.equals(fallback.summary)) {
            summary = fallback.summary + " | AI response: " + content.trim().replace('\n', ' ');
        }
        return new MatchResult(score, summary);
    }

    private String extractContent(String responseBody) {
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
                if (ch == 'n') {
                    content.append('\n');
                } else if (ch == '"' || ch == '\\' || ch == '/') {
                    content.append(ch);
                } else {
                    content.append(ch);
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
            content.append(ch);
        }
        return content.toString();
    }

    private String readFully(InputStream stream) throws IOException {
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

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String getApiKey() {
        return AIConfig.get("OPENAI_API_KEY");
    }

    private String getBaseUrl() {
        String value = AIConfig.get("OPENAI_BASE_URL");
        return ValidationUtils.notBlank(value) ? trimTrailingSlash(value.trim()) : "https://dashscope.aliyuncs.com/compatible-mode/v1";
    }

    private String getModelName() {
        String value = AIConfig.get("OPENAI_MODEL");
        return ValidationUtils.notBlank(value) ? value.trim() : "qwen-plus";
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String summariseError(String message) {
        if (ValidationUtils.isBlank(message)) {
            return "unknown transport error";
        }
        return message.length() > 90 ? message.substring(0, 90) + "..." : message;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", " ");
    }
}
