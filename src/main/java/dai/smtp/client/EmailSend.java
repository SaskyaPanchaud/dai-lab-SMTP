package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class EmailSend {
    public EmailSend(EmailGroup group, EmailContent content) {
        // FIXME : server adresse + port ?
        final String SERVER_ADDRESS = "localhost";
        final int SERVER_PORT = 1234;

        // TODO : implementer protocole SMTP
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
        // TODO
        } catch (IOException e) {
            System.out.println("EmailSend: exception while using socket: " + e);
        }
    }
}