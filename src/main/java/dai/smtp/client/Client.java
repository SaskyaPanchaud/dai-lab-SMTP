package dai.smtp.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Client {
    static final private int MIN_VICTIMS = 2;
    static final private int MAX_VICTIMS = 5;
    public static void main(String[] args) {
        if (args.length != 3)
        {
            throw new RuntimeException("Required args: <nGroups> <victims_list_path> <messages_list_path>");
        }

        Client client = new Client();
        client.run(args);
    }

    private void printArray(Object[] arr)
    {
        for (var el : arr)
        {
            System.out.println(el + ", ");
        }
    }

    private void run(String[] args)
    {
        // var victims = readVictims(args[1]);
        // var messages = readMessages(args[2]);
        String[] victims = {"alessio.giuliano@heig-vd.ch", "test", "test2"};
        Message[] messages = {new Message("S1", "Body 1"), new Message("S2", "Body 2")};
        int nGroups = Integer.parseInt(args[0]);

        var groups = createGroups(nGroups, victims, messages);

        printArray(victims);
        printArray(messages);

        // try(Socket socket = new Socket();
        //     var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        //     var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        //     )
        // {

        // }
        // catch (IOException e)
        // {

        // }
    }

    private static String[][] readCsv(String path)
    {
        var result = new ArrayList<String[]>();
        try(var reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));)
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                var l = new ArrayList<String>();
                for (var el : line.split(","))
                {
                    l.add(el);
                }
                result.add((String[]) l.toArray());
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException("Error while reading CSV file " + path);
        }

        return (String[][]) result.toArray();
    }

    private static String[] readVictims(String path)
    {
        var csv = readCsv(path);
        if (csv.length != 1)
        {
            throw new RuntimeException("Victims must be placed in a single line");
        }

        for (var victim : csv[0])
        {
            if (!victim.contains("@"))
            {
                throw new RuntimeException(String.format("Email address %s is not valid", victim));
            }
        }

        return csv[0];
    }

    private static Message[] readMessages(String path)
    {
        var csv = readCsv(path);
        var result = new ArrayList<Message>();
        for (var message : csv)
        {
            if (message.length != 2)
            {
                throw new RuntimeException("Messages must be placed on separate lines");
            }
            result.add(new Message(message[0], message[1]));
        }

        return (Message[]) result.toArray();
    }

    private static Object[] getRandomElements(Object[] elements, int minAmount, int maxAmount)
    {
        if (elements.length < minAmount)
        {
            throw new RuntimeException("There are not enough elements to select in the input array");
        }
        final Random RAND = new Random();
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

    private static String[] getRandomVictims(String[] victims)
    {
        return (String[]) getRandomElements(victims, MIN_VICTIMS, MAX_VICTIMS);
    }

    private static Message getRandomMessage(Message[] messages)
    {
        return (Message) getRandomElements(messages, 1, 1)[0];
    }

    private static Group[] createGroups(int nGroups, String[] victims, Message[] messages)
    {
        var groups = new Group[nGroups];
        for (int i = 0; i < nGroups; ++i)
        {
            var selectedVictims = getRandomVictims(victims);
            Message msg = getRandomMessage(messages);
            groups[i] = new Group(selectedVictims, msg);
        }

        return groups;
    }
}

class Message
{
    private String subject;
    private String body;

    Message(String aSubject, String aBody)
    {
        subject = aSubject;
        body = aBody;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getBody()
    {
        return body;
    }

    public String toString()
    {
        return subject + "\n\n" + body + "\n"; 
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

    public String[] getAddresses()
    {
        return addresses;
    }

    public Message getMessage()
    {
        return message;
    }
}