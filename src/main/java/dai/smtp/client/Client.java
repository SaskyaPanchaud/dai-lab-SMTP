package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    static final private int PORT = 1025;
    static final private String SERVER_ADDRESS = "localhost";

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    private void run() {
        try(Socket socket = new Socket(SERVER_ADDRESS, PORT);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        ) {

        }
        catch (IOException e) {
                System.out.println("Client IOException: %s".formatted(e.getMessage()));
                return;
            }
    }
}