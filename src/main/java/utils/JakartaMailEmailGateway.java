package utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class JakartaMailEmailGateway implements EmailGateway {

    private final String host;
    private final int port;
    private final boolean startTls;
    private final boolean ssl;
    private final String username;
    private final String password;
    private final String fromAddress;
    private final String fromName;

    public JakartaMailEmailGateway(String host, int port, boolean startTls, boolean ssl,
                                   String username, String password, String fromAddress, String fromName) {
        this.host = trimToNull(host);
        this.port = port;
        this.startTls = startTls;
        this.ssl = ssl;
        this.username = trimToNull(username);
        this.password = trimToNull(password);
        this.fromAddress = trimToNull(fromAddress);
        this.fromName = trimToNull(fromName);
    }

    @Override
    public void send(String toAddress, String subject, String body) {
        String recipient = trimToNull(toAddress);
        if (host == null || fromAddress == null || recipient == null) {
            throw new IllegalStateException("Thiếu cấu hình SMTP hoặc địa chỉ email không hợp lệ.");
        }

        Session session = createSession();

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress, fromName != null ? fromName : "", StandardCharsets.UTF_8.name()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject != null ? subject : "", StandardCharsets.UTF_8.name());
            message.setText(body != null ? body : "", StandardCharsets.UTF_8.name());
            Transport.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể gửi email xác thực.", ex);
        }
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", Integer.toString(port));
        props.put("mail.smtp.auth", Boolean.toString(username != null && password != null));
        props.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        props.put("mail.smtp.ssl.enable", Boolean.toString(ssl));
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.mime.charset", StandardCharsets.UTF_8.name());

        if (username != null && password != null) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }

        return Session.getInstance(props);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
