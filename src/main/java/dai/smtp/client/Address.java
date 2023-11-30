package dai.smtp.client;

import org.json.JSONObject;

public class Address extends JSONReadable {
    String address;

    Address() {}

    void fromJSON(JSONObject json) {
        address = json.getString("address");
        if (!address.contains("@") || !address.contains(".")) {
            throw new RuntimeException("Invalid email address: %s".formatted(address));
        }
    }

    static String getExpectedJSONFormat() {
        return "[{\"address\": <address1>}, {\"address\": <address2>}, ...]\"";
    }
}
