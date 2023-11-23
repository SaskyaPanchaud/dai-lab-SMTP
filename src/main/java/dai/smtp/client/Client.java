package dai.smtp.client;

import java.io.*;

public class Client {
    public static void main(String[] args) {

        // controles des args
        if (args.length != 3) {
            throw new RuntimeException("Required args: <victims_list_path> <messages_list_path> <nGroups>.");
        }
        // TODO : verfier si chemin valide
        if (args[0] && args[1]) {
            throw new RuntimeException("Filepath not valid...");
        }
        // TODO : verfier si type = int
        if (args[2]) {
            throw new RuntimeException("Arg for number of groups doesn't match.");
        }

        // stockage des args
        File victimsList = new File(args[0]);
        File messagesList = new File(args[1]);
        int numberGroups = Integer.parseInt(args[2]);

        // FIXME : besoin de convertir les fichiers en tableau de strings ?

        // traitement du nombre de groupe souhaite
        for (int n = 0; n < numberGroups; ++n) {
            // TODO : controle du contenu des args dans chaque classe
            // formation du groupe
            EmailGroup group = new EmailGroup(victimsList);
            // attribution du message
            EmailContent content = new EmailContent(messagesList);
            // envoi du message
            // TODO : s'occuper de l'encodage du header
            new EmailSend(group, content);
        }
    }
}