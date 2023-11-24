package dai.smtp.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EmailGroup {
    String[] emails;
    String sender;
    String[] receivers;

    public EmailGroup(File emailsList) {
        // TODO : verifier que minimum 2 adresses dans fichier des victimes
        // TODO : selectionner entre 2 - 5 adresses, assigner premiere a expediteur et reste a destinataire(s)
        this.emails = toStringEmails(emailsList);
        this.sender = chooseSender();
        this.receivers = chooseReceivers();
    }

    private String[] toStringEmails(File emailsList) {
        ArrayList<String> emails = new ArrayList<>();
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(emailsList), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                emails.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Bad adresses file...");
        }
        return emails.toArray();
    }

    private String chooseSender() {
        return emails[RandomInt()];
    }

    private String[] chooseReceivers() {
        ArrayList<String> receivers = new ArrayList<>();
        // TODO
        return receivers.toArray();
    }
}