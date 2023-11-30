package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Random;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.json.*;

public class Client {
    static final private int MIN_GROUP_ADDRESSES = 2;
    static final private int MAX_GROUP_ADDRESSES = 5;

    static final String NEWLINE = "\n";
    static final String CARRIAGE_RETURN = "\r";
    static final String SMTP_EOL = CARRIAGE_RETURN + NEWLINE;

    static final private int PORT = 1025;
    static final private String SERVER_ADDRESS = "localhost";

    public static void main(String[] args) {
        if (args.length != 3)
        {
            throw new RuntimeException("Required args: <nGroups> <addresses_list_path> <messages_list_path>");
        }

        var addresses = Address.readJSONFile(args[1]);
        var messages = Message.readJSONFile(args[2]);

        int nGroups = Integer.parseInt(args[0]);
        if (nGroups <= 0)
        {
            throw new RuntimeException("Invalid number of groups %d".formatted(nGroups));
        }
        var groups = createGroups(nGroups, (Address[]) addresses, messages);

        Client client = new Client();
        for (var group : groups)
        {
            client.run(group);
        }
    }

    private void run(Group group)
    {
            try(Socket socket = new Socket(SERVER_ADDRESS, PORT);
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            )
            {
                String firstMessage = in.readLine();
                if (firstMessage.startsWith("2"))
                {
                    System.out.println("Successfully connected to SMTP server");
                }
                else
                {
                    throw new RuntimeException("Could not connect to SMTP server. Response: " + firstMessage);
                }

                sendSMTPMessage(SMTPHello(SERVER_ADDRESS), out, in);
                sendSMTPMessage(SMTPSender(group.getSender().toString()), out, in);
                for (var recipient : group.getVictims())
                {
                    sendSMTPMessage(SMTPRecipient(recipient.toString()), out, in);
                }
                sendSMTPMessage(SMTPData(), out, in);
                sendSMTPMessage(group.toSMTPData(), out, in);

                // This creates a "command not recognized" error on MailDev.
                // sendSMTPMessage(SMTPQuit(), out, in);

                System.out.println("Successfully sent emails to group\n");
            }
            catch (IOException e)
            {
                System.out.println("Client IOException: %s".formatted(e.getMessage()));
                return;
            }
    }

    private static Object[] getRandomElements(Object[] elements, int minAmount, int maxAmount)
    {
        if (elements.length < minAmount)
        {
            throw new RuntimeException("There are not enough elements to select in the input array");
        }
        final Random RAND = new Random();
        // Min ensures that the random amount of elements is not greater than the total length of the elements.
        int nElements = Math.min(RAND.nextInt(maxAmount - minAmount + 1) + minAmount, elements.length);
        Object[] selectedElements = new Object[nElements];

        for (int i = 0; i < nElements; ++i)
        {
            Object selected = null;
            do
            {
                var idx = RAND.nextInt(elements.length);
                selected = elements[idx];
            } while (Arrays.asList(selectedElements).contains(selected));
            selectedElements[i] = selected;
        }

        return selectedElements;
    }

    static Address[] getRandomAddresses(Address[] addresses)
    {
        var elements = getRandomElements(addresses, MIN_GROUP_ADDRESSES, MAX_GROUP_ADDRESSES);
        return Arrays.copyOf(elements, elements.length, Address[].class);
    }

    static Message getRandomMessage(Message[] messages)
    {
        return (Message) getRandomElements(messages, 1, 1)[0];
    }


    static Group[] createGroups(int nGroups, Address[] addresses, Message[] messages)
    {
        var groups = new Group[nGroups];
        for (int i = 0; i < nGroups; ++i)
        {
            var selectedAddresses = Client.getRandomAddresses(addresses);
            Message msg = Client.getRandomMessage(messages);
            groups[i] = new Group(selectedAddresses, msg);
        }

        return groups;
    }

    private static String SMTPHello(String server)
    {
        return "HELO %s".formatted(server);
    }

    private static String SMTPSender(String sender)
    {
        return "MAIL FROM:<%s>".formatted(sender);
    }

    private static String SMTPRecipient(String recipient)
    {
        return "RCPT TO:<%s>".formatted(recipient);
    }


    private static String SMTPData()
    {
        return "DATA";
    }

    private static String SMTPQuit()
    {
        return "QUIT";
    }

    private static String replaceNewlines(String message)
    {
        // Two replace calls to ensure the result does not contain \r\r\n.
        return message.replace(SMTP_EOL, NEWLINE).replace(NEWLINE, SMTP_EOL);
    }

    private static void sendSMTPMessage(String message, Writer out, BufferedReader in)
    {
        try
        {
            out.write(replaceNewlines(message) + SMTP_EOL);
            out.flush();
            String answer = in.readLine();
            if (!(answer.startsWith("2") || answer.startsWith("3")))
            {
                throw new RuntimeException("SMTP error: %s".formatted(answer));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("IO error: Could not send message %s".formatted(e.getMessage()));
        }
    }
}

class JSONReader
{
    static <T extends JSONReadable> List<T> readJSON(String path, Class<T> clazz)
    {   
        var json = readJSONFile(path);
        try
        {
            var result = new ArrayList<T>();

            for (int i = 0; i < json.length(); ++i)
            {
                var object = clazz.getDeclaredConstructor().newInstance();
                object.fromJSON(json.getJSONObject(i));
                result.add(object);
            }
            return result;
        }
        catch (JSONException e)
        {
            throw new RuntimeException("Malformed JSON file %s. Format: %s".formatted(T.getExpectedJSONFormat()));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error creating a json object from %s \n%s".formatted(path, e.getMessage()));
        }
    }

    private static JSONArray readJSONFile(String path)
    {
        try(var reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));)
        {
            var file = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                file.append(line + "\n");
            }
            return new JSONArray(file.toString());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error while reading JSON file " + path);
        }
    }
}

abstract class JSONReadable
{
    abstract void fromJSON(JSONObject json);

    static String getExpectedJSONFormat()
    {
        return "No format specified";
    }
}

class Address extends JSONReadable
{
    private String address;

    Address()
    {}

    void fromJSON(JSONObject json)
    {
        address = json.getString("address");
        if (!address.contains("@") || !address.contains("."))
        {
            throw new RuntimeException("Invalid email address: %s".formatted(address));
        }
    }

    public String toString()
    {
        return address;
    }

    static String getExpectedJSONFormat()
    {
        return "[{\"address\": <address1 (sender)>}, {\"address\": <victim1>}, {\"address\": <victim2>}, ...]\"";
    }

    static Address[] readJSONFile(String path)
    {
        return JSONReader.readJSON(path, Address.class).toArray(new Address[0]);
    }
}

class Message extends JSONReadable
{
    private String subject;
    private String body;

    private static final String END_OF_MESSAGE = Client.NEWLINE + "." + Client.NEWLINE;

    Message()
    {
    }

    void fromJSON(JSONObject json)
    {
        subject = json.getString("subject");
        body = json.getString("body");
    }

    static String getExpectedJSONFormat()
    {
        return "[{\"subject\": <subject>, \"body\": <body>}, ...]";
    }

    static Message[] readJSONFile(String path)
    {
        return JSONReader.readJSON(path, Message.class).toArray(new Message[0]);
    }

    private String getSMTPDate()
    {
        return "Date: %s".formatted(ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    private String getSMTPSubject()
    {
        return "Subject: %s".formatted(subject);
    }

    private String getSMTPBody()
    {
        return body.replace(END_OF_MESSAGE, "%s..%s".formatted(Client.NEWLINE, Client.NEWLINE));
    }

    String toSMTPMessage()
    {   
        var result = new StringBuilder();
        result.append(getSMTPDate() + Client.NEWLINE);
        result.append(getSMTPSubject() + Client.NEWLINE.repeat(2));
        result.append(getSMTPBody() + END_OF_MESSAGE);

        return result.toString();
    }
}

class Group 
{
    Address[] addresses;
    Message message;

    Group(Address[] anAddresses, Message aMessage)
    {
        if (anAddresses.length < 2 || anAddresses.length > 5)
        {
            throw new RuntimeException("Groups must contain between 2 and 5 addresses");
        }
        addresses = anAddresses;
        message = aMessage;
    }

    public Address getSender()
    {
        return addresses[0];
    }

    public Address[] getVictims()
    {
        var result = new Address[addresses.length - 1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = addresses[i + 1];
        }
        return result;
    }
    
    private String getSMTPSender()
    {
        return "From: %s".formatted(getSender());
    }

    private String getSMTPRecipients()
    {
        StringBuilder result = new StringBuilder("To: ");
        for (var address : getVictims())
        {
            result.append(address + ", ");
        }
        return result.toString();
    }

    String toSMTPData()
    {
        var result = new StringBuilder();
        result.append(getSMTPSender() + Client.NEWLINE);
        result.append(getSMTPRecipients() + Client.NEWLINE);
        result.append(message.toSMTPMessage() + Client.NEWLINE);

        return result.toString();
    }
}