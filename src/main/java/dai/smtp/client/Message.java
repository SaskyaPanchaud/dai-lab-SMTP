package dai.smtp.client;

import org.json.JSONObject;

public class Message extends JSONReadable {
    String subject;
    String body;

    Message() {}

    String getSubject() {
        return subject;
    }

    String getBody() {
        return body;
    }

    void fromJSON(JSONObject json) {
        subject = json.getString("subject");
        body = json.getString("body");
    }

    static String getExpectedJSONFormat() {
        return "[{\"subject\": <subject>, \"body\": <body>}, ...]";
    }

    static Message[] readJSONFile(String path) {
        return JSONReader.readJSON(path, Message.class).toArray(new Message[0]);
    }
}
