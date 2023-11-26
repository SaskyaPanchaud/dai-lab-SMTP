package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class EmailSend {
    static final String SERVER_ADDRESS = "localhost";
    static final int SERVER_PORT = 1025;

    public boolean send(ArrayList<String> adresses, HashMap<String, String> message) {
        String sender = adresses.get(0);
        ArrayList<String> receivers = new ArrayList<>();
        for (int i = 1; i < adresses.size(); ++i) {
            receivers.add(adresses.get(i));
        }




        // TODO : s'occuper de l'encodage du header

        // TODO : implementer protocole SMTP
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            String firstResponse = in.readLine();
            if (firstResponse.startsWith("2")) {
                System.out.println("Successfully connected to SMTP server");
            } else {
                throw new RuntimeException("Could't connect to SMTP server. Response: " + firstResponse);
            }

            // TODO : finir envois et reponses avec serveur

            // contacter serveur
            out.write("ehlo");
            out.flush();

            String s;
            // lire et afficher lignes des extensions
            while((s = in.readLine()) != null) {
                if (s.substring(0,4) != "250 ") {
                    System.out.println(s);
                } else {
                    break;
                }
            }

            // envoyer sender
            out.write(sendToServer("mail from: ", sender));
            out.flush();
            // lire reponse
            String resultSender;
            while((resultSender = in.readLine()) != null) {
                System.out.println(resultSender);
            }

            // envoyer receivers
            StringBuilder receiversList = new StringBuilder();
            for (var r : receivers) {
                receiversList.append(r);
                receiversList.append("; ");
            }
            out.write(sendToServer("rcpt to: ", receivers));
            // lire reponse
            String resultReceivers;
            while((resultReceivers = in.readLine()) != null) {
                System.out.println(resultReceivers);
            }

            // envoyer data
            out.write("data");

            // From
            out.write("From " + sender + "\n");
            out.flush();
            // To

            // Date

            // Subject
               out.write(message.get)
            // texte et terminer avec logne.ligne
            out.write(message.entrySet().)

            // envoyer fin
            out.write("quit" + "\n");
            out.flush();

            // lire reponse


        } catch (IOException e) {
            System.out.println("EmailSend: exception while using socket: " + e);
        }
    }

    private String sendToServer(String smtpKeyword, String content) {
        return smtpKeyword + content + "\n";
    }
}