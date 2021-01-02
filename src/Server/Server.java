package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    public static void main(String[] args) throws IOException {
        Map<String, User> users = new HashMap<>();
        ServerSocket ss = new ServerSocket(12345);
        ReentrantLock l = new ReentrantLock();
        Condition notEmpty = l.newCondition();
        Condition contact = l.newCondition();

        users.put("user1", new User("user1", "user1", false, 0, 0, 10));
        users.put("user2", new User("user2", "user2", false, 0, 0, 10));
        users.put("user3", new User("user3", "user3", false, 1, 1, 10));
        users.put("user4", new User("user4", "user4", false, 1, 1, 10));
        users.put("user5", new User("user5", "user5", false, 2, 2, 10));
        users.put("admin", new User("admin", "admin", true, 0, 0, 10));

        while(true) {
            Socket socket = ss.accept();

            Thread clientHandler = new Thread(new ClientHandler(users, socket, l, notEmpty, contact));

            clientHandler.start();
        }
    }
}
