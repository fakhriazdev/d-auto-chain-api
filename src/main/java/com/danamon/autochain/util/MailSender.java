package com.danamon.autochain.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Properties;

@Slf4j
public class MailSender {
    private static final String protocol = "smtp";
    private static final Boolean smtpAuth = true;
    private static final String smtpHost = "smtp.gmail.com";
    private static final Integer smtpPort = 587;
    private static final Boolean smtpStartTls = true;
    private static final String sander = "wiryaalways@gmail.com";
    private static final String appPassword = "bwqj tlqu stni ocst";

    public static void mailer(String subject, HashMap<String,String> content, String to) throws MessagingException {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", smtpAuth);
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.starttls.enable", smtpStartTls);
        properties.put("mail.transport.protocl", protocol);

        Session session = Session.getInstance(
                properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(sander, appPassword);
                    }
                }
        );

        Message message = new MimeMessage(session);

        message.setSubject(subject);

        StringBuilder html = new StringBuilder();

        for (String c: content.keySet()) {
            html.append(content.get(c));
        }

        message.setContent(String.valueOf(html), "text/html");

        Address address = new InternetAddress(to);
        message.setRecipient(Message.RecipientType.CC,address);

        try {
            Transport.send(message);
        }catch (SendFailedException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mail Address Not Found Or Mail Provider Not Acceptable");
        }
    }
}
