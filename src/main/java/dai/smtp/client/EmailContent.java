package dai.smtp.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EmailContent {
    static int MIN_INDEX = 0;
    static int MAX_INDEX;
    static HashMap<String, String> messages = new HashMap<>();
    static int NB_MESSAGES;

    public EmailContent(File messagesList) {
        messages = fileToString(messagesList);
        NB_MESSAGES = messages.size();
        MAX_INDEX = NB_MESSAGES - 1;
    }

    private HashMap<String, String> fileToString(File messagesList) {
        HashMap<String, String> messages = new HashMap<>();
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(messagesList), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                var header = line;
                StringBuilder body = new StringBuilder();
                while ((line = in.readLine()) != "FIN MESSAGE") {
                    body.append(line);
                }
                messages.put(header, body.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading messages file...");
        }
        return messages;
    }

    public HashMap<String, String> chooseMessage() {
        HashMap<String, String> message = new HashMap<>();
        int i = 0;
        int index = (int) (Math.random() * (MAX_INDEX - MIN_INDEX + 1) + MIN_INDEX);
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            if (i == index) {
                message.put(entry.getKey(), entry.getValue());
            }
            ++i;
        }
        return message;
    }
}