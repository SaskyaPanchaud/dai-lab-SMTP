package dai.smtp.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;

public class EmailContent {
    HashMap<String, String> messages = new HashMap<>();
    public EmailContent(File messagesList) {
        // TODO : controler le contenu des args dans chaque classe
        int messageChoosenNumber = Random();
        // TODO : selectionner un message, attention a l'encodage et renvoyer ???
    }
    // TODO : verifier que fichier valide

    private void toStringMessages(File messagesList) {
        // TODO : encodage du header, voir consigne
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(messagesList), StandardCharsets.UTF_8))) {
            String line;
            int nbMessages = 0;
            while ((line = in.readLine()) != null) {
                if (line.equals("FIN OBJET")) {

                }
                this.emails.add(line);
                ++nbMessages;
            }
        } catch (IOException e) {
            throw new RuntimeException("Bad messages file...");
        }
    }
}