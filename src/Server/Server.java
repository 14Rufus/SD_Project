package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {

    public static void main(String[] args) throws IOException {
        Map<String, User> users = new HashMap<>();
        ServerSocket ss = new ServerSocket(12345);

        while(true) {
            Socket socket = ss.accept();
            Thread clientHandler = new Thread(new ClientHandler(users, socket));
            clientHandler.start();
        }
    }
}
