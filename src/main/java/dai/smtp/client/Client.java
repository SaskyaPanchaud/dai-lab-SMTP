package dai.smtp.client;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Client {
    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException("Required args: <victims_list_path> <messages_list_path> <nGroups>.");
        }

        int numberGroups;
        File emailsList;
        File messagesList;

        try {
            numberGroups = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Give a number for the third arg (number of groups).");
        }

        try {
            emailsList = new File(args[0]);
            messagesList = new File(args[1]);
        } catch (Exception e) {
            throw new RuntimeException("Give filepaths valid.");
        }

        EmailGroup group = new EmailGroup(emailsList);
        EmailContent content = new EmailContent(messagesList);
        EmailSend sending = new EmailSend(  );

        for (int n = 0; n < numberGroups; ++n) {
            ArrayList<String> adresses = group.formGroup();
            HashMap<String, String> message = content.chooseMessage();
            boolean send = sending.send(adresses, message);
            if (!send) {
                throw new RuntimeException("Error while sending email...");
            }
        }
    }
}