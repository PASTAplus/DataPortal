package edu.lternet.pasta.portal;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ExceptionFormatter {

    private static final Logger logger = Logger.getLogger(ExceptionFormatter.class);

    private String decodeBase64(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String extractStatus(String jsonString) {
        String status = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("status")) {
                status = jsonObject.getString("status");
            }
        } catch (Exception e) {
            status = "Could not determine exception status";
        }
        return status;
    }

    private String extractMessage(String jsonString) {
        String message = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("status")) {
                message = jsonObject.getString("message");
            }
        } catch (Exception e) {
            message = "Could not determine exception message";
        }
        return message;
    }

    public String getExceptionType(String exceptionMessage) {
        String[] parts = exceptionMessage.split(": ", 2);
        return parts[0];
    }

    public String getStatus(String exceptionMessage) {
        String[] parts = exceptionMessage.split(": ", 2);
        String details = parts[1];
        String status = extractStatus(details);
        return status;
    }

    public String getMessage(String exceptionMessage) {
        String[] parts = exceptionMessage.split(": ", 2);
        String details = parts[1];
        String message = extractMessage(details);
        return message;
    }

    public String getJSONString(String exceptionMessage) {
        String exceptionType = this.getExceptionType(exceptionMessage);
        String status = this.getStatus(exceptionMessage);
        String message = this.getMessage(exceptionMessage);
        String jsonString = String.format(
                "{ \\\"type\\\": \\\"%s\\\", \\\"status\\\": \\\"%s\\\", \\\"message\\\": \\\"%s\\\" }",
                exceptionType,
                status,
                message.replace("&#39;", "'")
        );
        return jsonString;
    }

}
