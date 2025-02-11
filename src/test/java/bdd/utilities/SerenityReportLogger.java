package bdd.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.core.Serenity;

public final class SerenityReportLogger {

    private SerenityReportLogger() {
    }

    public static void logMessage(String title, String message) {
        Serenity.recordReportData()
                .withTitle(title)
                .andContents(message);
    }

    public static void logJsonResponse(String title, String jsonResponse) {
        String prettyJson = jsonResponse;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object jsonObject = objectMapper.readValue(jsonResponse, Object.class);
            prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            // If parsing fails, we'll log the raw JSON
        }
        Serenity.recordReportData()
                .withTitle(title)
                .andContents(prettyJson);
    }

    public static void logMarkdown(String title, String markdownString) {
        Serenity.recordReportData()
                .withTitle(title)
                .andContents(markdownString);
    }

    public static void logHtml(String title, String htmlContent) {
        Serenity.recordReportData()
                .withTitle(title)
                .andContents(htmlContent);
    }

    public static void logResponse(String title, String responseBody) {
        logJsonResponse(title, responseBody);
    }
}
