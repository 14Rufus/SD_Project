package Client;

import java.io.*;
import java.net.Socket;

public class Reader implements Runnable {
    private DataInputStream in;

    public Reader(Socket socket) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void run() {
        try {
            String line;
            while ((line = in.readUTF()) != null)
                System.out.println(line);

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
