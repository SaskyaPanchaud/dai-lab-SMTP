package dai.smtp.client;

import org.json.JSONObject;

public abstract class JSONReadable
{
    abstract void fromJSON(JSONObject json);

    static String getExpectedJSONFormat()
    {
        return "No format specified";
    }
}

