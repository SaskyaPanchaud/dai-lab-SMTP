package dai.smtp.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SMTP {
    static final String NEWLINE = "\n";
    static final String EOL = "\r" + NEWLINE;
    static final String DATA_END = "%s.%s".formatted(EOL, EOL);

    BufferedReader in;
    BufferedWriter out;
    
    SMTP(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    void waitAccept() throws IOException {
        String firstMessage = in.readLine();
        if (firstMessage.startsWith("2")) {
            System.out.println("Connection to the SMTP server successful");
        }
        else {
            throw new IOException(firstMessage);
        }
    }

    static String helloMsg(String server) {
        return "HELO %s".formatted(server);
    }

    void sendHello(String server) throws IOException {
        send(helloMsg(server));
    }

    static String senderMsg(String sender) {
        return "MAIL FROM:<%s>".formatted(sender);
    }

    void sendSender(String sender) throws IOException {
        send(senderMsg(sender));
        return;
    }

    static String recipientMsg(String recipient) {
        return "RCPT TO:<%s>".formatted(recipient);
    }

    void sendRecipient(String recipient) throws IOException {
        send(recipientMsg(recipient));
    }

    static String dataStartMsg() {
        return "DATA";
    }

    void sendDataStart() throws IOException {
        send(dataStartMsg());
    }

    static String convertBody(String body) {
        return body.replace("%s.%s".formatted(NEWLINE, NEWLINE), "%s..%s".formatted(NEWLINE, NEWLINE));
    }

    static String dataMsg(String sender, String[] recipients, String subject, String body) {
        StringBuilder message = new StringBuilder();
        body = convertBody(body);

        message.append("From: %s".formatted(sender) + NEWLINE);
        message.append("To: %s".formatted(String.join(", ", recipients)) + NEWLINE);
        message.append("Date: %s".formatted(ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)) + NEWLINE);
        message.append("Subject: %s".formatted(subject) + NEWLINE + NEWLINE);
        message.append(body + DATA_END);

        return message.toString();
    }

    void sendData(String sender, String[] recipients, String subject, String body) throws IOException {
        send(dataMsg(sender, recipients, subject, body));
    }

    static String quitMsg() {
        return "QUIT";
    }

    void sendQuit() throws IOException {
        send(quitMsg());
    }

    static String replaceNewLines(String message) {
        // Two replace calls to ensure the result does not contain \r\r\n.
        return message.replace(EOL, NEWLINE).replace(NEWLINE, EOL);
    }

    void send(String message) throws IOException {
        try {
                out.write(replaceNewLines(message) + EOL);
                out.flush();
                String answer = in.readLine();
                if (!(answer.startsWith("2") || answer.startsWith("3"))) {
                    throw new RuntimeException("SMTP error: %s".formatted(answer));
                }
            }
        catch (IOException e) {
            throw new IOException("IO error: Could not send SMTP message %s\n%s".formatted(message, e.getMessage()));
        }
    }
}
