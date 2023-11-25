package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.json.*;

public class Client {
    static final private int MIN_GROUP_ADDRESSES = 2;
    static final private int MAX_GROUP_ADDRESSES = 5;

    static final private String NEWLINE = "\n";
    static final private String CARRIAGE_RETURN = "\r";
    static final private String SMTP_EOL = CARRIAGE_RETURN + NEWLINE;

    static final private int PORT = 1025;
    static final private String SERVER_ADDRESS = "localhost";
    public static void main(String[] args) {
        if (args.length != 3)
        {
            throw new RuntimeException("Required args: <nGroups> <addresses_list_path> <messages_list_path>");
        }

        Client client = new Client();
        client.run(args);
    }

    private void run(String[] args)
    {
        var addresses = readAddresses(args[1]);
        var messages = readMessages(args[2]);

        int nGroups = Integer.parseInt(args[0]);
        var groups = createGroups(nGroups, addresses, messages);

        for (var group : groups)
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
                sendSMTPMessage(SMTPSender(group.getSender()), out, in);
                for (var recipient : group.getVictims())
                {
                    sendSMTPMessage(SMTPRecipient(recipient), out, in);
                }
                sendSMTPMessage(SMTPData(), out, in);
                sendSMTPMessage(group.toSMTPData(), out, in);

                // This creates a "command not recognized" error on MailDev.
                // sendSMTPMessage(SMTPQuit(), out, in);
            }
            catch (IOException e)
            {

            }

        }
    }

    private static JSONArray readJsonArray(String path)
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

    private static JSONReadable[] readJSONArray(String path, String expectedFormat, JSONReader reader)
    {
        try
        {
            var json = readJsonArray(path);
            JSONReadable[] result = new JSONReadable[json.length()];
            for (int i = 0; i < json.length(); ++i)
            {
                result[i] = reader.read(json.getJSONObject(i));
            }
            return result;
        }
        catch (JSONException e)
        {
            throw new RuntimeException(String.format("Malformed JSON file %s. Format: %s", path, expectedFormat));
        }
    }
    // private static <T extends JSONReadable> T[] parseJsonArray(String path, JSONReader<T> reader, String expectedFormat)
    // {
    //     try
    //     {
    //         var json = readJsonArray(path);
    //         Object[] result = new Object[json.length()];
    //         for (int i = 0; i < json.length(); ++i)
    //         {
    //             result[i] = new reader.read(json.getJSONObject(i));
    //         }
    //         return (T[]) Arrays.copyOf(result, result.length);
    //     }
    //     catch (JSONException e)
    //     {
    //         throw new RuntimeException(String.format("Malformed JSON file %s. Format: %s[<address1 (sender)>, <victim1>, <victim2>, ...]", path, expectedFormat));
    //     }
    // }

    private static String[] readAddresses(String path)
    {
        var json = readJsonArray(path);
        try
        {
            String[] result = new String[json.length()];
            for (int i = 0; i < json.length(); ++i)
            {
                result[i] = json.getJSONObject(i).getString("address");
            }
            return result;
        }
        catch (JSONException e)
        {
            throw new RuntimeException(String.format("Malformed JSON file %s. Format: [<address1 (sender)>, <victim1>, <victim2>, ...]", path));
        }
    }

    private static Message[] readMessages(String path)
    {
        var json = readJsonArray(path);
        // final var READER = new JSONReader<Message>();
        try
        {
            var result = new Message[json.length()];
            for (int i = 0; i < json.length(); ++i)
            {
                var object = json.getJSONObject(i);
                result[i] = new Message(object.getString("subject"), object.getString("body"));
            }
            return result;
        }
        catch (JSONException e)
        {
            throw new RuntimeException(String.format("Malformed JSON file %s. Format: [{\"subject\": <subject>, \"body\": <body>}, ...]", path));
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

    private static String[] getRandomAddresses(String[] addresses)
    {
        var elements = getRandomElements(addresses, MIN_GROUP_ADDRESSES, MAX_GROUP_ADDRESSES);
        return Arrays.copyOf(elements, elements.length, String[].class);
    }

    private static Message getRandomMessage(Message[] messages)
    {
        return (Message) getRandomElements(messages, 1, 1)[0];
    }

    private static Group[] createGroups(int nGroups, String[] addresses, Message[] messages)
    {
        var groups = new Group[nGroups];
        for (int i = 0; i < nGroups; ++i)
        {
            var selectedAddresses = getRandomAddresses(addresses);
            Message msg = getRandomMessage(messages);
            groups[i] = new Group(selectedAddresses, msg);
        }

        return groups;
    }

    private static String SMTPHello(String server)
    {
        return String.format("HELO %s", server);
    }

    private static String SMTPSender(String sender)
    {
        return String.format("MAIL FROM:<%s>", sender);
    }

    private static String SMTPRecipient(String recipient)
    {
        return String.format("RCPT TO:<%s>", recipient);
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
                throw new RuntimeException(String.format("SMTP error: %s", answer));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(String.format("IO error: Could not send message %s"));
        }
    }
}

// class JSONReader<T extends JSONReadable>
// {
//     JSONReadable read(JSONObject json)
//     {
//         return T.fromJSONObject(json);
//     }    
// }

interface JSONReader
{
    JSONReadable read(JSONObject json);
}

abstract class JSONReadable
{
    static JSONReadable fromJSONObject(JSONObject json)
    {
        return null;
    }
}

// class JSONReader <T>
// {
//     static Readable read(String path)
//     {
//         return new T();
//     }
// }

// abstract class Readable
// {
//     static Readable fromJSONObject(JSONObject json)
//     {
//         return null;
//     }
// }


class Address extends JSONReadable
{
    private String address;
    Address(String anAddress)
    {
        address = anAddress;
    }

    public String toString()
    {
        return address;
    }

    static JSONReader getReader()
    {
        return (json) -> new Address(json.getString("address"));
    }

    static String getExpectedJSONFormat()
    {
        return "[{\"address\": <address1 (sender)>}, {\"address\": <victim1>}, {\"address\": <victim2>}, ...]\"";
    }

    static Address fromJSONObject(JSONObject json)
    {
        return new Address(json.getString("address"));
    }
}

class Message extends JSONReadable
{
    private String subject;
    private String body;

    Message(String aSubject, String aBody)
    {
        subject = aSubject;
        body = aBody;
    }

    static JSONReader getReader()
    {
        return (json) -> new Message(json.getString("subject"), json.getString("body"));
    }

    static String getExpectedJSONFormat()
    {
        return "";
    }

    String getSubject()
    {
        return subject;
    }

    String getBody()
    {
        return body;
    }

    private String getSMTPDate()
    {
        return String.format("Date: %s", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    private String getSMTPSubject()
    {
        return String.format("Subject: %s", subject);
    }

    private String getSMTPBody()
    {
        return body.replace("\n.\n", "\n..\n");
    }

    String toSMTPMessage()
    {   
        
        return String.format("%s\n%s\n\n%s\n.\n", getSMTPDate(), getSMTPSubject(), getSMTPBody());
    }
}

class Group 
{
    String[] addresses;
    Message message;

    Group(String[] anAddresses, Message aMessage)
    {
        if (anAddresses.length < 2 || anAddresses.length > 5)
        {
            throw new RuntimeException("Groups must contain between 2 and 5 addresses");
        }
        addresses = anAddresses;
        message = aMessage;
    }

    public String getSender()
    {
        return addresses[0];
    }

    public String[] getVictims()
    {
        return Arrays.copyOfRange(addresses, 1, addresses.length);
    }

    public Message getMessage()
    {
        return message;
    }

    private String getSMTPSender()
    {
        return String.format("From: %s", getSender());
    }

    private String getSMTPRecipients()
    {
        return String.format("To: %s", String.join(", ", getVictims()));
    }

    String toSMTPData()
    {
        return String.format("%s\n%s\n%s\n", getSMTPSender(), getSMTPRecipients(), message.toSMTPMessage());
    }
}