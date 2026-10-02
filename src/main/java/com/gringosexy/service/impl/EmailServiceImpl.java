package com.gringosexy.service.impl;

import com.gringosexy.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.from:noreply@gringosexy.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:GRINGO SEXY}")
    private String fromName;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String verificationUrl = baseUrl + "/verify-email?token=" + token;
        String subject = "Verifica tu cuenta en GRINGO SEXY";
        
        String htmlContent = buildEmailTemplate(
            "¡Bienvenido a GRINGO SEXY!",
            "Hola, " + fullName + ". Gracias por registrarte en nuestra plataforma de optimización móvil de alto rendimiento.",
            "Para completar tu registro y activar tu cuenta, por favor confirma tu dirección de correo electrónico haciendo clic en el siguiente botón:",
            "Verificar mi cuenta",
            verificationUrl,
            "Este enlace de verificación expirará en 24 horas. Si no creaste esta cuenta, puedes ignorar este mensaje."
        );

        sendHtmlEmail(toEmail, subject, htmlContent, verificationUrl);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        String subject = "Recuperación de contraseña - GRINGO SEXY";

        String htmlContent = buildEmailTemplate(
            "Recuperación de Contraseña",
            "Hola, " + fullName + ". Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.",
            "Para crear una nueva contraseña segura, haz clic en el siguiente botón:",
            "Restablecer Contraseña",
            resetUrl,
            "Este enlace expirará en 60 minutos por razones de seguridad. Si no solicitaste este cambio, ignora este correo."
        );

        sendHtmlEmail(toEmail, subject, htmlContent, resetUrl);
    }

    @Async
    @Override
    public void sendAccountStatusNotification(String toEmail, String fullName, String newStatusDescription) {
        String dashboardUrl = baseUrl + "/login";
        String subject = "Actualización en el estado de tu cuenta - GRINGO SEXY";

        String htmlContent = buildEmailTemplate(
            "Estado de Cuenta Actualizado",
            "Hola, " + fullName + ".",
            "Te informamos que el estado de tu cuenta en GRINGO SEXY ha sido modificado a: <strong>" + newStatusDescription + "</strong>.",
            "Ir a GRINGO SEXY",
            dashboardUrl,
            "Si tienes dudas o requieres asistencia técnica, contacta a nuestro equipo de soporte."
        );

        sendHtmlEmail(toEmail, subject, htmlContent, dashboardUrl);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent, String directLink) {
        try {
            // Check if SMTP is configured
            if (mailUsername == null || mailUsername.trim().isEmpty()) {
                log.info("==========================================================================================");
                log.info("[SIMULATED EMAIL LOG] SMTP not configured in .env (MAIL_USERNAME is empty).");
                log.info("To: {}", toEmail);
                log.info("Subject: {}", subject);
                log.info("Direct Action URL: {}", directLink);
                log.info("==========================================================================================");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email successfully sent to: {} | Subject: {}", toEmail, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            log.info("[FALLBACK ACTION LINK] Direct Action URL for {}: {}", toEmail, directLink);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage());
            log.info("[FALLBACK ACTION LINK] Direct Action URL for {}: {}", toEmail, directLink);
        }
    }

    private String buildEmailTemplate(String headerTitle, String greeting, String bodyText, String buttonText, String buttonUrl, String footerNote) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='utf-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>" + headerTitle + "</title>" +
               "<style>" +
               "  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0d0f17; color: #e2e8f0; margin: 0; padding: 20px; line-height: 1.6; }" +
               "  .container { max-width: 600px; margin: 0 auto; background: #131722; border-radius: 16px; border: 1px solid #1e293b; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }" +
               "  .header { background: linear-gradient(135deg, #1e1b4b, #311042); padding: 32px 24px; text-align: center; border-bottom: 1px solid #2e1065; }" +
               "  .logo { font-size: 26px; font-weight: 800; letter-spacing: 1px; color: #f43f5e; text-shadow: 0 0 15px rgba(244,63,94,0.4); margin: 0; }" +
               "  .logo span { color: #38bdf8; }" +
               "  .content { padding: 32px 28px; }" +
               "  .title { color: #ffffff; font-size: 20px; font-weight: 700; margin-top: 0; }" +
               "  .text { color: #94a3b8; font-size: 15px; margin-bottom: 20px; }" +
               "  .btn-wrapper { text-align: center; margin: 32px 0; }" +
               "  .btn { display: inline-block; background: linear-gradient(135deg, #e11d48, #9333ea); color: #ffffff !important; text-decoration: none; font-weight: 600; font-size: 16px; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 20px rgba(225,29,72,0.4); }" +
               "  .url-box { background: #0b0e14; padding: 12px; border-radius: 8px; font-size: 12px; color: #64748b; word-break: break-all; border: 1px solid #1e293b; margin-top: 15px; }" +
               "  .footer { background: #090c10; padding: 20px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #1e293b; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "  <div class='container'>" +
               "    <div class='header'>" +
               "      <h1 class='logo'>GRINGO <span>SEXY</span></h1>" +
               "    </div>" +
               "    <div class='content'>" +
               "      <h2 class='title'>" + headerTitle + "</h2>" +
               "      <p class='text'>" + greeting + "</p>" +
               "      <p class='text'>" + bodyText + "</p>" +
               "      <div class='btn-wrapper'>" +
               "        <a href='" + buttonUrl + "' class='btn' target='_blank'>" + buttonText + "</a>" +
               "      </div>" +
               "      <p class='text' style='font-size:13px; margin-top:25px;'>O copia y pega este enlace en tu navegador:</p>" +
               "      <div class='url-box'>" + buttonUrl + "</div>" +
               "    </div>" +
               "    <div class='footer'>" +
               "      <p>" + footerNote + "</p>" +
               "      <p>© " + java.time.Year.now().getValue() + " GRINGO SEXY. Todos los derechos reservados.</p>" +
               "    </div>" +
               "  </div>" +
               "</body>" +
               "</html>";
    }
}
