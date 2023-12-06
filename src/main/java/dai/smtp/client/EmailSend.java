package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class EmailSend {
    static final String SERVER_ADDRESS = "localhost";
    static final int SERVER_PORT = 1025;

    private String arrayListToString(ArrayList<String> arrayList) {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < arrayList.size(); ++i) {
            if (i != 0) {
                s.append(", ");
            }
            s.append(arrayList.get(i));
        }
        return s.toString();
    }

    public boolean send(ArrayList<String> adresses, HashMap<String, String> message) {
        // definition du sender et des receivers
        String sender = adresses.get(0);
        ArrayList<String> receivers = new ArrayList<>();
        for (int i = 1; i < adresses.size(); ++i) {
            receivers.add(adresses.get(i));
        }

        // debut conversation avec serveur
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            // lecture premier message
            String firstResponse = in.readLine();
            if (firstResponse.startsWith("2")) {
                System.out.println("Successfully connected to SMTP server");
            } else {
                throw new RuntimeException("Could't connect to SMTP server. Response: " + firstResponse);
            }
            // envoi premier message
            out.write("EHLO " + SERVER_ADDRESS + "\n");
            out.flush();
            // lecture extensions
            String extension;
            while((extension = in.readLine()) != null) {
                System.out.println(extension);
                if(extension.substring(0, 4).equals("250 ")) {
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
            for (var r : receivers) {
                out.write("rcpt to: <" + r + ">\n");
                out.flush();
                String resultReceivers = in.readLine();
                if (resultReceivers.startsWith("2")) {
                    System.out.println("Receivers OK");
                } else {
                    throw new RuntimeException("Receivers KO : " + resultReceivers);
                }
            }

            // envoi "data" + lecture reponse
            out.write("data\n");
            out.flush();
            String resultData = in.readLine();
            System.out.println(resultData);

            // envoi contenu email
            out.write("From: <" + sender + ">\n");
            out.flush();
            out.write("To: <" + arrayListToString(receivers) + ">\n");
            out.flush();
            out.write("Date: " + java.time.LocalDate.now() + "\n");
            out.flush();
            out.write("Subject: " + message.keySet() + "\n");
            out.flush();
            out.write("\n");
            out.flush();
            out.write(message.values() + "\n");
            out.flush();
            out.write("\r\n.\r\n");
            out.flush();

            // lire reponse
            System.out.println(in.readLine());

            //out.write("quit" + "\n");
            //out.flush();

        } catch (IOException e) {
            System.out.println("EmailSend: exception while using socket: " + e);
        }
        return true;
    }
}