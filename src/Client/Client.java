package Client;

import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        Thread writer = new Thread(new Writer(socket));
        Thread reader = new Thread(new Reader(socket));

        writer.start();
        reader.start();
    }
}
