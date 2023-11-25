package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class EmailSend {
    public EmailSend(EmailGroup group, EmailContent content) {
        // FIXME : dans EmailGroup : deja separation entre sender et receivers, utiliser ca ?
        // TODO : controler le contenu des args dans chaque classe
        // TODO : s'occuper de l'encodage du header

        // content = tableau de string ou premier element = header et deuxieme = message
        // group = tableau de string ou premier = sender et autres = receivers

        // FIXME : server adresse + port ?
        final String SERVER_ADDRESS = "localhost";
        final int SERVER_PORT = 1234;

        // TODO : implementer protocole SMTP
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            String s;
            // contacter serveur
            out.write("ehlo " + "\n");
            out.flush();

            // lire et afficher lignes des extensions
            while((s = in.readLine()) != null) {
                if (s.substring(0,4) != "250 ") {
                    System.out.println(s);
                } else {
                    break;
                }
            }

            // envoyer sender
            out.write("mail from: " + group[0]) + "\n";
            out.flush();
            // lire reponse
            String resultSender;
            while((resultSender = in.readLine()) != null) {
                System.out.println(resultSender);
            }

            // envoyer receivers
            StringBuilder receivers;
            for (int i = 1; i < receivers.length; ++i) {
                receivers.append(receivers[i]);
                if(i < receivers.lenght - 1) {
                    receivers.append("; ");
                }
            }
            out.write("rcpt to: " + receivers + "\n");
            // lire reponse
            String resultReceivers;
            while((resultReceivers = in.readLine()) != null) {
                System.out.println(resultReceivers);
            }

            // envoyer data
            out.write("data");

            // From

            // To

            // Date

            // Subject

            // texte et terminer avec logne.ligne

            // envoyer fin
            out.write("quit" + "\n");
            out.flush();

            // lire reponse


        } catch (IOException e) {
            System.out.println("EmailSend: exception while using socket: " + e);
        }
    }
}