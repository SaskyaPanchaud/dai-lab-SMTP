package dai.smtp.client;

import org.json.JSONObject;

public class Message extends JSONReadable {
    String subject;
    String body;

    Message() {}

    void fromJSON(JSONObject json) {
        subject = json.getString("subject");
        body = json.getString("body");
    }

    static String getExpectedJSONFormat() {
        return "[{\"subject\": <subject>, \"body\": <body>}, ...]";
    }
}
