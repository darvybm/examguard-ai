package pucmm.eict.proyectofinal.examguard.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pucmm.eict.proyectofinal.examguard.model.Folder;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.User;
import pucmm.eict.proyectofinal.examguard.utils.DynamicTemplatePersonalization;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${sendgrid.api.key}")
    private String API_KEY;

    @Value("${sendgrid.api.sender-mail}")
    private String SENDER_MAIL;

    public void sendEmailRegistration(User user) {

        final String TEMPLATE_ID = "d-ffc8f556ac294c4985a772dd8c9ee054";

        Email from = new Email(SENDER_MAIL);
        Email to = new Email(user.getEmail());
        Mail mail = new Mail();

        DynamicTemplatePersonalization personalization = new DynamicTemplatePersonalization();
        personalization.addTo(to);
        mail.setFrom(from);
        mail.setSubject("Registro Exitoso");

        personalization.addDynamicTemplateData("name", user.getName());
        mail.addPersonalization(personalization);
        mail.setTemplateId(TEMPLATE_ID);

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();

        try {
            System.out.println("MAIL");
            System.out.println(mail.build());
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendFraudEmail(User user, Recording recording, Folder folder) {

        final String TEMPLATE_ID = "d-94215d27faf04472aaa6c3f5139a15fb";

        Email from = new Email(SENDER_MAIL);
        Email cc = new Email(user.getEmail());
        Email to = new Email(recording.getStudent().getEmail());
        Mail mail = new Mail();

        DynamicTemplatePersonalization personalization = new DynamicTemplatePersonalization();
        personalization.addTo(to);
        personalization.addCc(cc);
        mail.setFrom(from);
        mail.setSubject("Fraude Detectado");

        personalization.addDynamicTemplateData("name", user.getName());
        personalization.addDynamicTemplateData("folder", folder.getName());
        personalization.addDynamicTemplateData("student", recording.getStudent().getEmail());
        //formatear el porcentaje de fraude a 2 decimales
        personalization.addDynamicTemplateData("fraud", String.format("%.2f", recording.getFraudPercentage()));
        mail.addPersonalization(personalization);
        mail.setTemplateId(TEMPLATE_ID);

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();

        try {
            System.out.println("MAIL");
            System.out.println(mail.build());
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
