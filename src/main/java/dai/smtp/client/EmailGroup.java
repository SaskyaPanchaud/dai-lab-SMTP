package dai.smtp.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class EmailGroup {

    static int MAX_NB_RECEIVERS = 5;
    int nbAdr;
    String sender;
    String[] receivers;

    public EmailGroup(File emailsList) {
        // TODO : controler le contenu du fichier -> si @ dans chaque ligne + verifier que minimum 2 adresses dans fichier des victimes
        this.nbAdr = nbAdresses(emailsList);
        this.sender = chooseSender(emailsList);
        this.receivers = chooseReceivers();
    }

    private int nbAdresses(File emailsList) {
        int count = 0;
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(emailsList));
            while (lnr.readLine() != null) {
                ++count;
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem while counting the number of adresses...");
        }
        return count;
    }

    // TODO : supprimer si plus utile
    /*private String[] toStringEmails(File emailsList) {
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
    }*/

    private String chooseSender(File emailsList) {
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(emailsList), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder sender = new StringBuilder();
            Random rdm = new Random();
            int indexRandom = rdm.nextInt(nbAdr + 1);
            int i = 0;
            while ((line = in.readLine()) != null) {
                if (i == indexRandom) {
                    sender.append(line);
                    break;
                }
                ++i;
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem while choosing the sender...");
        }
        return sender.toString();
    }

    private String[] chooseReceivers() {
        ArrayList<String> receivers = new ArrayList<>();
        Random rdm = new Random();
        int nbReceivers = rdm.nextInt(MAX_NB_RECEIVERS) + 1;
        for (int i = 0; i < nbReceivers; ++i) {
            // FIXME : ajouter un controle pour ne pas choisir plusieurs fois la meme adresse ?
            // TODO
            receivers.add(line);
        }
        return receivers.toArray(new String[receivers.size()]);
    }
}