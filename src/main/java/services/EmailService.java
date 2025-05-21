package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;

public class EmailService {
    private final String username = "tuntransport.sup@gmail.com"; // Configure this
    private final String password = "kwwv qbkv xzgr bhwe"; // Configure this
    // EmailSerivce (reciever , sujet , content)
    public void sendEmail(String to, String subject, String content) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
      /**
     * Sends a payment confirmation email with a QR code to the user
     * @param to Email address of the recipient
     * @param subject Email subject
     * @param userName User's name
     * @param reservationId ID of the reservation
     * @param amount Amount paid
     * @param qrCodePath Path to QR code image (could be a URL or local file path, can be null)
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendPaymentConfirmationEmail(String to, String subject, String userName, 
                                         String reservationId, double amount, String qrCodePath) {
        try {
            if (to == null || to.trim().isEmpty()) {
                System.err.println("Erreur: adresse email de destination non fournie.");
                return false;
            }
            
            // Generate QR code URL with the reservation details
            String reservationData = "TunTransport:Reservation:" + reservationId + ":Amount:" + amount;
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + 
                java.net.URLEncoder.encode(reservationData, "UTF-8");
            
            String htmlContent = buildPaymentConfirmationEmailContent(userName, reservationId, amount, qrCodeUrl);
            return sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Erreur lors de la préparation de l'email de confirmation de paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Builds HTML content for payment confirmation email
     */    private String buildPaymentConfirmationEmailContent(String userName, String reservationId, 
                                                     double amount, String qrCodeUrl) {
        return "<!DOCTYPE html>"
             + "<html>"
             + "<head>"
             + "<style>"
             + "body { font-family: Arial, sans-serif; }"
             + ".container { width: 80%; margin: 0 auto; padding: 20px; }"
             + ".header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }"
             + ".content { padding: 20px; border: 1px solid #ddd; }"
             + ".footer { background-color: #f1f1f1; padding: 10px; text-align: center; }"
             + "</style>"
             + "</head>"
             + "<body>"
             + "<div class='container'>"
             + "<div class='header'><h2>Confirmation de Paiement</h2></div>"
             + "<div class='content'>"
             + "<p>Cher(e) " + userName + ",</p>"
             + "<p>Nous vous remercions pour votre paiement. Votre réservation est maintenant confirmée.</p>"
             + "<p><strong>Numéro de réservation:</strong> " + reservationId + "</p>"
             + "<p><strong>Montant payé:</strong> " + amount + " TND</p>"
             + "<p>Veuillez présenter ce code QR au chauffeur pour vérification:</p>"
             + "<div style='text-align:center'><img src='" + qrCodeUrl + "' alt='Code QR' style='width:200px;height:200px;'></div>"
             + "<p>Merci d'avoir choisi notre service.</p>"
             + "</div>"
             + "<div class='footer'>"
             + "<p>Équipe TunTransport</p>"
             + "</div>"
             + "</div>"
             + "</body>"
             + "</html>";
    }
    
    /**
     * Sends an email with HTML content
     * @param to Email address of the recipient
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     */    /**
     * Sends an email with HTML content
     * @param to Email address of the recipient
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.connectiontimeout", "10000");
        prop.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            
            // Set HTML content
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            
            message.setContent(multipart);
            
            Transport.send(message);
            System.out.println("Email de confirmation de paiement envoyé avec succès à: " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email à " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Génère le contenu HTML pour l'embauche d'un conducteur
     */
    public String buildHiringEmailContent(String prenom, String nom) {
        return "<!DOCTYPE html>"
                + "<html><head><style>body{font-family:Arial,sans-serif;}" +
                ".header{background-color:#4CAF50;color:white;padding:10px;text-align:center;}" +
                ".content{padding:20px;border:1px solid #ddd;}" +
                ".footer{background-color:#f1f1f1;padding:10px;text-align:center;}" +
                "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'><h2>Bienvenue chez TuniTransport !</h2></div>"
                + "<div class='content'>"
                + "<p>Cher(e) " + prenom + " " + nom + ",</p>"
                + "<p>Nous avons le plaisir de vous informer que vous avez été <b>recruté(e)</b> en tant que conducteur au sein de notre équipe.</p>"
                + "<img src='https://cdn-icons-png.flaticon.com/512/194/194938.png' alt='Conducteur' style='width:120px;display:block;margin:20px auto;'>"
                + "<p>Bienvenue à bord ! Nous vous souhaitons beaucoup de succès dans vos nouvelles fonctions.</p>"
                + "</div>"
                + "<div class='footer'><p>L'équipe TuniTransport</p></div>"
                + "</div></body></html>";
    }

    /**
     * Génère le contenu HTML pour l'assignation d'un véhicule
     */
    public String buildVehicleAssignmentEmailContent(String prenom, String nom, String vehicule, String immatriculation) {
        return "<!DOCTYPE html>"
                + "<html><head><style>body{font-family:Arial,sans-serif;}" +
                ".header{background-color:#2196F3;color:white;padding:10px;text-align:center;}" +
                ".content{padding:20px;border:1px solid #ddd;}" +
                ".footer{background-color:#f1f1f1;padding:10px;text-align:center;}" +
                "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'><h2>Véhicule Assigné</h2></div>"
                + "<div class='content'>"
                + "<p>Cher(e) " + prenom + " " + nom + ",</p>"
                + "<p>Un véhicule vous a été assigné :</p>"
                + "<ul><li><b>Véhicule :</b> " + vehicule + "</li>"
                + "<li><b>Immatriculation :</b> " + immatriculation + "</li></ul>"
                + "<img src='https://cdn-icons-png.flaticon.com/512/743/743007.png' alt='Véhicule' style='width:120px;display:block;margin:20px auto;'>"
                + "<p>Merci de vérifier l'état du véhicule avant chaque prise de service.</p>"
                + "</div>"
                + "<div class='footer'><p>L'équipe TuniTransport</p></div>"
                + "</div></body></html>";
    }

    /**
     * Génère le contenu HTML pour la notification de licenciement
     */
    public String buildFiringEmailContent(String prenom, String nom, String organisation) {
        return "<!DOCTYPE html>"
                + "<html><head><style>body{font-family:Arial,sans-serif;}" +
                ".header{background-color:#f44336;color:white;padding:10px;text-align:center;}" +
                ".content{padding:20px;border:1px solid #ddd;}" +
                ".footer{background-color:#f1f1f1;padding:10px;text-align:center;}" +
                "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'><h2>Fin de Contrat</h2></div>"
                + "<div class='content'>"
                + "<p>Cher(e) " + prenom + " " + nom + ",</p>"
                + "<p>Nous vous informons que votre contrat avec " + organisation + " a été résilié.</p>"
                + "<img src='https://cdn-icons-png.flaticon.com/512/753/753345.png' alt='Fin de contrat' style='width:120px;display:block;margin:20px auto;'>"
                + "<p>Veuillez contacter le service RH pour plus d'informations.</p>"
                + "</div>"
                + "<div class='footer'><p>L'équipe TuniTransport</p></div>"
                + "</div></body></html>";
    }

    /**
     * Génère le contenu HTML pour la notification de maintenance du véhicule
     */
    public String buildMaintenanceEmailContent(String prenom, String nom, String vehicule, String immatriculation) {
        return "<!DOCTYPE html>"
                + "<html><head><style>body{font-family:Arial,sans-serif;}" +
                ".header{background-color:#FF9800;color:white;padding:10px;text-align:center;}" +
                ".content{padding:20px;border:1px solid #ddd;}" +
                ".footer{background-color:#f1f1f1;padding:10px;text-align:center;}" +
                "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'><h2>Véhicule en Maintenance</h2></div>"
                + "<div class='content'>"
                + "<p>Cher(e) " + prenom + " " + nom + ",</p>"
                + "<p>Votre véhicule <b>" + vehicule + "</b> (" + immatriculation + ") a été envoyé en maintenance.</p>"
                + "<img src='https://cdn-icons-png.flaticon.com/512/3062/3062634.png' alt='Maintenance' style='width:120px;display:block;margin:20px auto;'>"
                + "<p>Nous vous tiendrons informé(e) dès qu'il sera de nouveau disponible.</p>"
                + "</div>"
                + "<div class='footer'><p>L'équipe TuniTransport</p></div>"
                + "</div></body></html>";
    }

    /**
     * Envoie un email HTML générique
     */
    public boolean sendGenericHtmlEmail(String to, String subject, String htmlContent) {
        return sendHtmlEmail(to, subject, htmlContent);
    }
}
