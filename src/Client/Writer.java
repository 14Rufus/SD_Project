package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Writer implements Runnable {
    private BufferedReader in;
    private PrintWriter out;

    public Writer(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new PrintWriter((socket.getOutputStream()));
    }

    public void run() {
        try {
            String line;
            while((line = in.readLine())!= null) {
                out.println(line);
                out.flush();
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
