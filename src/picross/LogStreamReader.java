package picross;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogStreamReader implements Runnable {

    private BufferedReader reader;
    public static List<String> output;

    public LogStreamReader(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
        output = new ArrayList<String>();
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                output.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}