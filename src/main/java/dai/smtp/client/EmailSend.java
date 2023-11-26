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
        // definition du sender et des receivers
        String sender = adresses.get(0);
        ArrayList<String> receivers = new ArrayList<>();
        for (int i = 1; i < adresses.size(); ++i) {
            receivers.add(adresses.get(i));
        }

        // debut conversation avec serveur
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            // lecture premier message
            String firstResponse = in.readLine();
            if (firstResponse.startsWith("2")) {
                System.out.println("Successfully connected to SMTP server");
            } else {
                throw new RuntimeException("Could't connect to SMTP server. Response: " + firstResponse);
            }
            // envoi premier message
            out.write("EHLO" + SERVER_ADDRESS + "\n");
            out.flush();
            // lecture extensions
            String extension;
            while((extension = in.readLine()) != null) {
                if (extension.substring(0,4) != "250 ") {
                    System.out.println(extension);
                } else {
                    break;
                }
            }
            // envoi "mail from" + lecture reponse
            out.write("mail from: <" + sender + ">\n");
            out.flush();
            String resultSender = in.readLine();
            if (resultSender.startsWith("2")) {
                System.out.println("Sender OK");
            } else {
                throw new RuntimeException("Sender KO : " + resultSender);
            }

            // envoi "rcpt to" + lecture reponse
            // FIXME : comment mettre plusieurs receivers ?, tester avec ncat
            out.write("rcpt to: <" + receivers + ">\n");
            out.flush();
            String resultReceivers = in.readLine();
            if (resultReceivers.startsWith("2")) {
                System.out.println("Receivers OK");
            } else {
                throw new RuntimeException("Receivers KO : " + resultReceivers);
            }

            // envoi "data" + lecture reponse
            out.write("data\n");
            System.out.println(in.readLine());

            // envoi contenu email
            out.write("From: <" + sender + ">\n");
            // FIXME : comment envoyer la liste ?, a tester
            out.write("To: <" + receivers + ">\n");
            out.write("Date: " + java.time.LocalDate.now() + "\n");
            // TODO : s'occuper de l'encodage du header
            out.write("Subject: " + message.keySet() + "\n");
            out.write("\n");
            out.write(message.values() + "\n");
            out.write("\n.\n");
            out.flush();
            // TODO des que tests ok
            // lire reponse


            // envoi fin
            out.write("quit" + "\n");
            out.flush();
            // lire reponse

        } catch (IOException e) {
            System.out.println("EmailSend: exception while using socket: " + e);
        }
        return true;
    }

    private String sendToServer(String smtpKeyword, String content) {
        return smtpKeyword + content + "\n";
    }
}