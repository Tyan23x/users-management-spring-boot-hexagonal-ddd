package com.jcaa.usersmanagement.infrastructure.adapter.email;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Slf4j
@Component
public class JavaMailEmailSenderAdapter implements EmailSenderPort {

  private static final String MAIL_SMTP_HOST = "mail.smtp.host";
  private static final String MAIL_SMTP_PORT = "mail.smtp.port";
  private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
  private static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";
  private static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
  private static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
  private static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
  private static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";
  private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
  private static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
  private static final String MAIL_SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
  private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
  private static final String MAIL_SMTP_WRITE_TIMEOUT = "mail.smtp.writetimeout";

  private static final String SSL_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";
  private static final String VALUE_TRUE = "true";
  private static final String VALUE_FALSE = "false";
  private static final String VALUE_ALL = "*";
  private static final String TIMEOUT_MS = "10000";
  private static final int SMTPS_PORT = 465;

  private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
  private static final String CHARSET_UTF8 = "UTF-8";
  private static final String LOG_SENT = "[JavaMailEmailSenderAdapter] correo enviado exitosamente.";

  private final Session mailSession;
  private final String fromAddress;
  private final String fromName;

  public JavaMailEmailSenderAdapter(final SmtpConfig config) {
    this.fromAddress = config.fromAddress();
    this.fromName = config.fromName();
    this.mailSession = buildSession(config);
  }

  @Override
  public void send(final EmailDestinationModel destination) {
    try {
      final MimeMessage message = buildMessage(destination);
      Transport.send(message);
      log.info(LOG_SENT);
    } catch (final MessagingException | UnsupportedEncodingException exception) {
      throw EmailSenderException.becauseSmtpFailed(
          destination.getDestinationEmail(), exception.getMessage());
    }
  }

  private MimeMessage buildMessage(final EmailDestinationModel destination)
      throws MessagingException, UnsupportedEncodingException {
    final MimeMessage message = new MimeMessage(mailSession);
    message.setFrom(new InternetAddress(fromAddress, fromName, CHARSET_UTF8));
    message.addRecipient(
        Message.RecipientType.TO,
        new InternetAddress(
            destination.getDestinationEmail(), destination.getDestinationName(), CHARSET_UTF8));
    message.setSubject(destination.getSubject(), CHARSET_UTF8);
    message.setContent(destination.getBody(), CONTENT_TYPE_HTML);
    return message;
  }

  private static Session buildSession(final SmtpConfig config) {
    final Properties properties = buildSmtpProperties(config);
    return Session.getInstance(
        properties,
        new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(config.username(), config.password());
          }
        });
  }

  private static Properties buildSmtpProperties(final SmtpConfig config) {
    final Properties properties = new Properties();
    properties.put(MAIL_SMTP_HOST, config.host());
    properties.put(MAIL_SMTP_PORT, String.valueOf(config.port()));
    properties.put(MAIL_SMTP_AUTH, VALUE_TRUE);
    properties.put(MAIL_SMTP_SSL_TRUST, VALUE_ALL);
    properties.put(MAIL_SMTP_CONNECTION_TIMEOUT, TIMEOUT_MS);
    properties.put(MAIL_SMTP_TIMEOUT, TIMEOUT_MS);
    properties.put(MAIL_SMTP_WRITE_TIMEOUT, TIMEOUT_MS);

    if (config.port() == SMTPS_PORT) {
      properties.put(MAIL_SMTP_SSL_ENABLE, VALUE_TRUE);
      properties.put(MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf(SMTPS_PORT));
      properties.put(MAIL_SMTP_SOCKET_FACTORY_CLASS, SSL_FACTORY_CLASS);
      properties.put(MAIL_SMTP_SOCKET_FACTORY_FALLBACK, VALUE_FALSE);
    } else {
      properties.put(MAIL_SMTP_STARTTLS, VALUE_TRUE);
      properties.put(MAIL_SMTP_STARTTLS_REQUIRED, VALUE_TRUE);
    }
    return properties;
  }
}
