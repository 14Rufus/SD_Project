package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Reader implements Runnable {
    private BufferedReader in;

    public Reader(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null)
                System.out.println(line);

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
