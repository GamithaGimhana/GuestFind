package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.service.EmailService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.sender.email}")
    private String senderEmail;

    public void sendEmail(String to, String subject, String body) throws IOException {
//        Email from = new Email("noreply@yourapp.com"); // verified sender
        Email from = new Email(senderEmail);
        Email recipient = new Email(to);

//        Content content = new Content("text/plain", body);
        Content content = new Content("text/html", "<h3>" + body + "</h3>");
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("Email sent! Status: " + response.getStatusCode());
        } catch (IOException ex) {
            throw ex;
        }
    }
}
