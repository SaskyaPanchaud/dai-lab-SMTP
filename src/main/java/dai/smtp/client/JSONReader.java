package dai.smtp.client;

import java.util.*;
import java.io.*;

import org.json.*;

public class JSONReader {
    static <T extends JSONReadable> List<T> readJSON(String path, Class<T> clazz) {   
        var json = readJSONArray(path);
        try {
            var result = new ArrayList<T>();

            for (int i = 0; i < json.length(); ++i) {
                var object = clazz.getDeclaredConstructor().newInstance();
                object.fromJSON(json.getJSONObject(i));
                result.add(object);
            }
            return result;
        }
        catch (JSONException e) {
            throw new RuntimeException("Malformed JSON file %s. Format: %s".formatted(T.getExpectedJSONFormat()));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error creating a json object from %s \n%s".formatted(path, e.getMessage()));
        }
    }

    private static JSONArray readJSONArray(String path) {
        try(var reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));) {
            var file = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                file.append(line + "\n");
            }
            return new JSONArray(file.toString());
        }
        catch (IOException e) {
            throw new RuntimeException("Error while reading JSON file " + path);
        }
    }
}