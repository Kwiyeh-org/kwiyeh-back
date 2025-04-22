package com.kwiyeh.back.service;

import java.util.Properties;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public final class MailService {

    JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
    Properties props = mailSenderImpl.getJavaMailProperties();
    public MailService(){
        this.initialize();
    }

    public void initialize(){
        mailSenderImpl.setHost("smtp.gmail.com");
        mailSenderImpl.setPort(587);
        mailSenderImpl.setUsername("ttemtsa@gmail.com");
        mailSenderImpl.setPassword("xnfa pgav xbsc brvh");

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "3000");
        props.put("mail.smtp.writetimeout", "5000");
    }

    public void sendSignupMail(String email, String receiverName){

        SimpleMailMessage mailMsg = new SimpleMailMessage();
        mailMsg.setFrom("ttemtsa@gmail.com");
        mailMsg.setSubject("Welcome to kwiyeh");
        mailMsg.setTo(email);
        mailMsg.setText(
            "Dear " + receiverName
				+ ", thank you for signing up to kwiyeh "
        );

        try {
			    mailSenderImpl.send(mailMsg);
		    }
		    catch (MailException ex) {
			    System.err.println(ex.getMessage());
		    }
    }

    public void sendForgetPasswordMail(String email, String receiverName, String forgetPasswordCode){

        SimpleMailMessage mailMsg = new SimpleMailMessage();
        mailMsg.setFrom("ttemtsa@gmail.com");
        mailMsg.setSubject("Forget Password");
        mailMsg.setTo(email);
        mailMsg.setText(
            "Dear " + receiverName
				+ ", here password reset code "+ forgetPasswordCode
        );

        try {
			    mailSenderImpl.send(mailMsg);
		    }
		    catch (MailException ex) {
			    System.err.println(ex.getMessage());
		    }
    }

    public JavaMailSenderImpl getMailSenderImpl() {
        return mailSenderImpl;
    }

    public void setMailSenderImpl(JavaMailSenderImpl mailSenderImpl) {
        this.mailSenderImpl = mailSenderImpl;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }
}
