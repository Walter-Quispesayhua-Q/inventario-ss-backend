package com.upeu.gestioninventario.shared.services.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("Iniciando preparación de correo para: {}. Asunto: {}", to, subject);
        try {
            final Context context = new Context();
            context.setVariables(variables);
            final String body = templateEngine.process(templateName, context);

            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            log.info("Correo enviado correctamente a: {}", to);
        } catch (MessagingException e){
            log.error("Error al enviar correo a {}: {}", to, e.getMessage(), e);
        }
    }
}
