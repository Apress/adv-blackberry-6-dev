import java.io.*;
import java.net.*;

public class MediaPusher
{

    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.err.println("Must provide a MIME type and a filename.");
            return;
        }
        String mime = args[0];
        String filename = args[1];
        String pushId = "MediaGrabber" + System.currentTimeMillis();
        try
        {
            // Read in media to send.
            FileInputStream fis = new FileInputStream(filename);
            int length = fis.available();
            byte[] data = new byte[length];
            fis.read(data);

            // Configure push
            URL url = new URL("http", "localhost", 8080,
                    "/push?DESTINATION=2100000A&PORT=4242&REQUESTURI=/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-RIM-PUSH-ID", pushId);

            // Send push payload
            OutputStream out = conn.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeUTF(mime);
            dos.write(data, 0, length);
            dos.close();
            out.close();
            conn.getResponseCode();
            conn.disconnect();
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }
}

