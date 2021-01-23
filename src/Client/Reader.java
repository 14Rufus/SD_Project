package Client;

import java.io.*;
import java.net.Socket;

public class Reader implements Runnable {
    private final Socket socket;
    private final DataInputStream in;

    public Reader(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void run() {
        try {
            String line;
            while (true) {
                line = in.readUTF();
                System.out.println(line);
            }
        } catch (IOException ignored) {}
    }
}
