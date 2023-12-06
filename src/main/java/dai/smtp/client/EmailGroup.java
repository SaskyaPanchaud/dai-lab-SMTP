package dai.smtp.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EmailGroup {

    static final private int MIN_IN_GROUP = 2;
    static private int MAX_IN_GROUP = 5;
    static final private int MIN_INDEX = 0;
    static private int MAX_INDEX;
    static private ArrayList<String> emails = new ArrayList<>();
    static int NB_EMAILS;

    public EmailGroup(File emailsList) {
        emails = fileToString(emailsList);
        NB_EMAILS = emails.size();
        MAX_IN_GROUP = NB_EMAILS >= 5 ? 5 : NB_EMAILS - 1;
        MAX_INDEX = NB_EMAILS - 1;
    }

    private ArrayList<String> fileToString(File emailsList) {
        ArrayList<String> emails = new ArrayList<>();
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(emailsList), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                emails.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading adresses file...");
        }
        if (emails.size() < 2) {
            throw new RuntimeException("Not enough adresses in file...");
        }
        for (var e : emails) {
            if (!(e.contains("@"))) {
                throw new RuntimeException("Adresses file doesn't only contain addresses...");
            }
        }
        return emails;
    }

    public ArrayList<String> formGroup() {
        ArrayList<String> group = new ArrayList<>();
        int groupSize = (int) (Math.random() * (MAX_IN_GROUP - MIN_IN_GROUP + 1) + MIN_IN_GROUP);
        int index;
        for (int i = 0; i < groupSize; ++i) {
            index = (int) (Math.random() * (MAX_INDEX - MIN_INDEX + 1) + MIN_INDEX);
            if (group.contains(emails.get(index))) {
                --i;
                continue;
            }
            group.add(emails.get(index));
        }
        return group;
    }
}