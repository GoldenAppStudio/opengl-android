package studio.goldenapp.imagefilters.others;

import android.content.Context;
import android.content.res.Resources;
import android.media.ResourceBusyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {
    public static String readTextFileFromResources(
            Context context, int resourceID) {
        StringBuilder body = new StringBuilder();

        try {

            InputStream inputStream =
                    context.getResources().openRawResource(resourceID);

            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);

            BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append("\n");
            }
        } catch (IOException e) {
            try {
                throw new IOException("Could not open resource: " + resourceID, e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (Resources.NotFoundException e) {
            throw new RuntimeException("Resourse not found: " + resourceID, e);
        }

        return body.toString();
    }
}
