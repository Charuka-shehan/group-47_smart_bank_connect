package com.lankatrust.smartbank.util;

public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static String otp(String fullName, String otp) {
        String name = fullName != null ? fullName : "Valued Customer";
        return wrap("Email Verification OTP",
                "<p>Hello " + escape(name) + ",</p>"
                        + "<p>Your SmartBank Connect verification code is:</p>"
                        + "<p style=\"font-size:32px;letter-spacing:8px;font-weight:700;color:#1814F3;text-align:center;margin:24px 0;\">"
                        + escape(otp) + "</p>"
                        + "<p>This code expires in <strong>5 minutes</strong>. Do not share it with anyone.</p>"
                        + "<p>If you did not request this code, please ignore this email or contact LankaTrust Bank immediately.</p>");
    }

    public static String otp(String fullName, String otp, String operationType, int expiryMinutes) {
        String name = fullName != null ? fullName : "Valued Customer";
        String operationLabel = getOperationLabel(operationType);
        return wrap("Email Verification Code",
                "<p>Hello " + escape(name) + ",</p>"
                        + "<p>A verification code has been requested for: <strong>" + escape(operationLabel) + "</strong></p>"
                        + "<p>Your SmartBank Connect verification code is:</p>"
                        + "<p style=\"font-size:32px;letter-spacing:8px;font-weight:700;color:#1814F3;text-align:center;margin:24px 0;\">"
                        + escape(otp) + "</p>"
                        + "<p>This code expires in <strong>" + expiryMinutes + " minutes</strong>. Do not share it with anyone.</p>"
                        + "<p style=\"background:#FFF3CD;border-left:4px solid #FFC107;padding:12px;border-radius:4px;color:#856404;\">"
                        + "<strong>Security Note:</strong> We will never ask you to share this code by phone, email or in person. "
                        + "If you did not request this code, please contact LankaTrust Bank immediately.</p>");
    }

    public static String generic(String fullName, String bodyHtml) {
        String name = fullName != null ? fullName : "Valued Customer";
        return wrap("SmartBank Connect",
                "<p>Hello " + escape(name) + ",</p>" + bodyHtml);
    }

    private static String wrap(String heading, String inner) {
        return "<!DOCTYPE html><html><body style=\"margin:0;background:#F5F7FA;font-family:Inter,Arial,sans-serif;\">"
                + "<div style=\"max-width:560px;margin:24px auto;background:#ffffff;border:1px solid #E6EFF5;border-radius:12px;overflow:hidden;\">"
                + "<div style=\"background:#1814F3;color:#ffffff;padding:20px 24px;font-size:18px;font-weight:700;\">LankaTrust Bank PLC</div>"
                + "<div style=\"padding:24px;color:#343C6A;line-height:1.6;\">"
                + "<h2 style=\"margin-top:0;font-size:20px;\">" + escape(heading) + "</h2>"
                + inner
                + "<p style=\"margin-top:28px;font-size:13px;color:#718EBF;\">Regards,<br>SmartBank Connect Team<br>LankaTrust Bank PLC</p>"
                + "</div></div></body></html>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String getOperationLabel(String operationType) {
        if (operationType == null) {
            return "Security Verification";
        }
        return switch (operationType.toUpperCase()) {
            case "FUND_TRANSFER" -> "Fund Transfer";
            case "CARD_PAYMENT" -> "Card Payment";
            case "PASSWORD_CHANGE" -> "Password Change";
            case "EMAIL_CHANGE" -> "Email Address Change";
            case "PHONE_CHANGE" -> "Phone Number Change";
            case "BENEFICIARY_ADD" -> "Add New Beneficiary";
            case "LOAN_APPLICATION" -> "Loan Application";
            case "SECURITY_SETTINGS_CHANGE" -> "Security Settings Change";
            default -> "Security Verification";
        };
    }
}
