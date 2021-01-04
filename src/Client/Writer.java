package Client;

import java.io.*;
import java.net.Socket;

public class Writer implements Runnable {
    private BufferedReader in;
    private DataOutputStream out;
    private Socket socket;

    public Writer(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
    }

    public void run() {
        try {
            String line;
            while((line = in.readLine())!= null) {
                out.writeUTF(line);
                out.flush();
            }

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
