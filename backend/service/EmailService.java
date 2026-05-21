package service;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
public class EmailService {
    private static final String FROM = "hethongbanvegadieutri@gmail.com";
    private static final String PASS = "dlzghpgausvfnzyp";
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;

    public static void sendOtp(String toEmail, String otp) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", String.valueOf(PORT));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASS);
            }
        });
        session.setDebug(true);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject("Mã OTP đặt lại mật khẩu");
        msg.setText("Mã OTP của bạn là: " + otp);

        // CÁCH NÀY ÉP BUỘC KẾT NỐI
        Transport transport = session.getTransport("smtp");
        transport.connect(HOST, FROM, PASS); 
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }
}