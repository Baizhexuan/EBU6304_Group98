                + "Use short labelled sections: Recommendation, Evidence, Risks, Next Action.";
        String userMessage = "Current recruitment context:\n" + limit(safe(context), MAX_CONTEXT_CHARS)
                + "\n\nUser question:\n" + question.trim();
        return "{"
                + "\"model\":\"" + escapeJson(getModelName()) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemMessage) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userMessage) + "\"}"
                + "],"
                + "\"temperature\":0.3,"
                + "\"max_tokens\":700"
                + "}";
    }

    private static String buildResponsesPayload(String question, String context) {
        String instructions = "You are an explainable AI assistant for a university Teaching Assistant recruitment system. "
                + "Use the provided system context only as decision support. Do not make final hiring decisions blindly. "
                + "Return plain text only. Do not use Markdown, emoji, tables, code blocks, or decorative symbols. "
                + "Use short labelled sections: Recommendation, Evidence, Risks, Next Action.";
        String input = "Current recruitment context:\n" + limit(safe(context), MAX_CONTEXT_CHARS)
                + "\n\nUser question:\n" + question.trim();

        return "{"
                + "\"model\":\"" + escapeJson(getModelName()) + "\","
                + "\"instructions\":\"" + escapeJson(instructions) + "\","
                + "\"input\":\"" + escapeJson(input) + "\","
                + "\"max_output_tokens\":700"
                + "}";
    }

    private static String extractChatCompletionText(String responseBody) {
        String content = extractJsonString(responseBody, "content");
        return ValidationUtils.notBlank(content) ? content : responseBody;
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
        String value = AIConfig.get("OPENAI_BASE_URL");
        if (ValidationUtils.notBlank(value)) {
            return trimTrailingSlash(value.trim());
        }
        return isQwenModel() ? "https://dashscope.aliyuncs.com/compatible-mode/v1" : "https://api.openai.com/v1";
    }

    private static String getModelName() {
        String value = AIConfig.get("OPENAI_MODEL");
        return ValidationUtils.notBlank(value) ? value.trim() : "qwen-plus";
    }

    private static boolean isChatCompletionsMode() {
        String mode = AIConfig.get("AI_API_MODE");
        if (ValidationUtils.notBlank(mode)) {
            return "CHAT_COMPLETIONS".equalsIgnoreCase(mode.trim())
                    || "QWEN".equalsIgnoreCase(mode.trim())
                    || "DASHSCOPE".equalsIgnoreCase(mode.trim());
        }
        return isQwenModel() || getBaseUrl().toLowerCase().contains("dashscope");
    }

    private static boolean isQwenModel() {
        return getModelName().toLowerCase().startsWith("qwen");
    }

    private static String getApiModeLabel() {
        return isChatCompletionsMode() ? "chat/completions" : "responses";
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
