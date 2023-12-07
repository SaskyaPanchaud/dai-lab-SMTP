package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    static final private int PORT = 1025;
    static final private String SERVER_ADDRESS = "localhost";

    static final int MIN_GROUP_ADDRESSES = 2;
    static final int MAX_GROUP_ADDRESSES = 5;

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException("Required args: <nGroups> <addresses_list_path> <messages_list_path>");
        }

        var addresses = Address.readJSONFile(args[1]);
        var messages = Message.readJSONFile(args[2]);

        int nGroups = Integer.parseInt(args[0]);
        if (nGroups <= 0) {
            throw new RuntimeException("Invalid number of groups %d".formatted(nGroups));
        }

        var groups = Group.createGroups(nGroups, addresses, messages);

        Client client = new Client();

        for (var group : groups) {
            client.run(group);
        }
    }

    private void run(Group group) {
        try(Socket socket = new Socket(SERVER_ADDRESS, PORT);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        ) {
            SMTP smtp = new SMTP(in, out);
            smtp.waitAccept();
            smtp.sendHello(SERVER_ADDRESS);
            smtp.sendSender(group.getSenderStr());
            for (var recipient : group.getVictimsStr()) {
                smtp.sendRecipient(recipient);
            }
            smtp.sendDataStart();
            smtp.sendData(group.getSenderStr(), group.getVictimsStr(), group.getSubject(), group.getBody());

            // This creates a "command not recognized" error on MailDev.
            // smtp.sendQuit();

            System.out.println("Successfully sent message to group.\n");
        }
        catch (IOException e) {
                System.out.println("Client IOException: %s".formatted(e.getMessage()));
                return;
            }
    }
}