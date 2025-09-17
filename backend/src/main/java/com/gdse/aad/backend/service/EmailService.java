package com.gdse.aad.backend.service;

import java.io.IOException;

public interface EmailService {
    void sendEmail(String to, String subject, String text) throws IOException;
}
