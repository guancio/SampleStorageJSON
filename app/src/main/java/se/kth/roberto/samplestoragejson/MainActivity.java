package se.kth.roberto.samplestoragejson;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


// https://developer.android.com/guide/topics/data/data-storage.html
// https://developer.android.com/reference/org/json/JSONTokener.html
// https://developer.android.com/reference/android/util/JsonReader.html

public class MainActivity extends AppCompatActivity {
    String FILENAME = "hello_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());

        String string = " [\n" +
                "   {\n" +
                "     \"id\": 912345678901,\n" +
                "     \"text\": \"How do I read JSON on Android?\",\n" +
                "     \"geo\": null,\n" +
                "     \"user\": {\n" +
                "       \"name\": \"android_newb\",\n" +
                "       \"followers_count\": 41\n" +
                "      }\n" +
                "   },\n" +
                "   {\n" +
                "     \"id\": 912345678902,\n" +
                "     \"text\": \"@android_newb just use android.util.JsonReader!\",\n" +
                "     \"geo\": [50.454722, -104.606667],\n" +
                "     \"user\": {\n" +
                "       \"name\": \"jesse\",\n" +
                "       \"followers_count\": 2\n" +
                "     }\n" +
                "   }\n" +
                " ]";

        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public String readFullyAsString(InputStream inputStream, String encoding)
            throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    public byte[] readFullyAsBytes(InputStream inputStream)
            throws IOException {
        return readFully(inputStream).toByteArray();
    }

    private ByteArrayOutputStream readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }


    public void read(View view) {
        FileInputStream file = null;
        try {
            file = openFileInput(FILENAME);
            String content = readFullyAsString(file, "UTF-8");
            TextView input = (TextView) findViewById(R.id.text);
            input.setText(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private JSONArray parse1() throws JSONException, IOException {
        FileInputStream file = openFileInput(FILENAME);
        String content = readFullyAsString(file, "UTF-8");
        JSONArray messages = (JSONArray) new JSONTokener(content).nextValue();
        file.close();
        return messages;
    }

    public void readJSON1(View view) {
        try {
            JSONArray messages = parse1();
            JSONObject message = messages.getJSONObject(1);
            JSONObject user = message.getJSONObject("user");

            Toast t = Toast.makeText(this, user.getString("name"), Toast.LENGTH_SHORT);
            t.show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void readJSON2(View view) {
        try {
            List<Message> messages = parse2();
            Toast t = Toast.makeText(this, messages.get(1).user.name, Toast.LENGTH_SHORT);
            t.show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<Message> parse2() throws IOException {
        FileInputStream file = openFileInput(FILENAME);
        JsonReader reader = new JsonReader(new InputStreamReader(file, "UTF-8"));

        List<Message> messages = new ArrayList<Message>();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readMessage(reader));
        }
        reader.endArray();
        reader.close();
        file.close();;
        return messages;
    }

    private Message readMessage(JsonReader reader) throws IOException {
        Message message = new Message();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                message.id = reader.nextLong();
            } else if (name.equals("text")) {
                message.text = reader.nextString();
            } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
                message.geo = readDoublesArray(reader);
            } else if (name.equals("user")) {
                message.user = readUser(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return message;
    }

    private double[] readDoublesArray(JsonReader reader) throws IOException {
        List<Double> doubles = new ArrayList<Double>();

        reader.beginArray();
        while (reader.hasNext()) {
            doubles.add(reader.nextDouble());
        }
        reader.endArray();
        double[] res = new double[doubles.size()];
        for (int i=0; i<doubles.size(); i++) {
            res[i] = doubles.get(i);
        }
        return res;
    }
    private User readUser(JsonReader reader) throws IOException {
        User user = new User();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                user.name = reader.nextString();
            } else if (name.equals("followers_count")) {
                user.followers_count = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return user;
    }

    public void addAndPrint1(View view) {
        try {
            JSONArray messages = parse1();
            JSONObject message = new JSONObject();
            message.put("id", 123456);

            JSONObject user = new JSONObject();
            user.put("name", "roberto");

            message.put("user", user);
            messages.put(message);

            TextView input = (TextView) findViewById(R.id.text);
            input.setText(messages.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAndPrint2(View view) {
        try {
            List<Message> messages = parse2();

            Message message = new Message();
            message.id = 123456;

            User user = new User();
            user.name = "Roberto";

            message.user = user;
            messages.add(message);

            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.setIndent("  ");
            writeMessagesArray(writer, messages);
            writer.close();


            TextView input = (TextView) findViewById(R.id.text);
            input.setText(stringWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
        writer.beginArray();
        for (Message message : messages) {
            writeMessage(writer, message);
        }
        writer.endArray();
    }

    private void writeMessage(JsonWriter writer, Message message) throws IOException {
        writer.beginObject();
        writer.name("id").value(message.id);
        writer.name("text").value(message.text);
        if (message.geo != null) {
            writer.name("geo");
            writeDoublesArray(writer, message.geo);
        } else {
            writer.name("geo").nullValue();
        }
        writer.name("user");
        writeUser(writer, message.user);
        writer.endObject();
    }

    private void writeUser(JsonWriter writer, User user) throws IOException {
        writer.beginObject();
        writer.name("name").value(user.name);
        writer.name("followers_count").value(user.followers_count);
        writer.endObject();
    }

    private void writeDoublesArray(JsonWriter writer, double[] geo) throws IOException {
        writer.beginArray();
        for (double value : geo) {
            writer.value(value);
        }
        writer.endArray();
    }

}
