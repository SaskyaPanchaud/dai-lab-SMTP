package dai.smtp.client;

import java.util.Arrays;

public class Group {
    Address[] addresses;
    Message message;

    private Group(Address[] anAddresses, Message aMessage) {
        if (anAddresses.length < Client.MIN_GROUP_ADDRESSES || anAddresses.length > Client.MAX_GROUP_ADDRESSES) {
            throw new RuntimeException("Groups must contain between %d and %d addresses".formatted(Client.MIN_GROUP_ADDRESSES, Client.MAX_GROUP_ADDRESSES));
        }
        addresses = anAddresses;
        message = aMessage;
    }

    public Address getSender() {
        return addresses[0];
    }

    public Address[] getVictims() {
        var result = new Address[addresses.length - 1];
        for (int i = 0; i < result.length; ++i) {
            result[i] = addresses[i + 1];
        }
        return result;
    }

    static Group[] createGroups(int nGroups, Address[] addresses, Message[] messages) {
        var groups = new Group[nGroups];
        for (int i = 0; i < nGroups; ++i) {
            var addressesGeneric = Util.getRandomElements(addresses, Client.MIN_GROUP_ADDRESSES, Client.MAX_GROUP_ADDRESSES);
            var selectedAddresses = Arrays.copyOf(addressesGeneric, addressesGeneric.length, Address[].class);

            Message msg = (Message) Util.getRandomElements(messages, 1, 1)[0];
            
            groups[i] = new Group(selectedAddresses, msg);
        }

        return groups;
    }
}
