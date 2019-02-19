package org.akvo.caddisfly.util;

import java.io.File;
import java.security.Security;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GMailSender extends javax.mail.Authenticator {
    static {
        Security.addProvider(new JSSEProvider());
    }

    private String user;
    private String password;
    private Session session;

    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        String mailHost = "smtp.gmail.com";
        props.setProperty("mail.host", mailHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, File firstImage, File turbidImage,
                                      File lastImage, String sender, String recipients) throws Exception {
        MimeMessage message = new MimeMessage(session);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        MimeBodyPart imagePart1 = new MimeBodyPart();
        imagePart1.addHeader("Content-ID", "<firstImage>");
        imagePart1.setDisposition(MimeBodyPart.INLINE);
        imagePart1.attachFile(firstImage);
        multipart.addBodyPart(imagePart1);

        if (turbidImage != null) {
            MimeBodyPart imagePart2 = new MimeBodyPart();
            imagePart2.addHeader("Content-ID", "<turbidImage>");
            imagePart2.setDisposition(MimeBodyPart.INLINE);
            imagePart2.attachFile(turbidImage);
            multipart.addBodyPart(imagePart2);
        }

        MimeBodyPart imagePart3 = new MimeBodyPart();
        imagePart3.addHeader("Content-ID", "<lastImage>");
        imagePart3.setDisposition(MimeBodyPart.INLINE);
        imagePart3.attachFile(lastImage);
        multipart.addBodyPart(imagePart3);

        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);

        message.setContent(multipart);

        if (recipients.indexOf(',') > 0)
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

        Transport.send(message);
    }
}