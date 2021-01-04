package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    private static void initUsers(Map<String, User> users, Lock l) {
        users.put("user1", new User("user1", "user1", false, 0, 0, 10, Arrays.asList("user2", "admin")));
        users.put("user2", new User("user2", "user2", false, 0, 0, 10, Arrays.asList("user1", "admin")));
        users.put("user3", new User("user3", "user3", false, 1, 1, 10, Collections.singletonList("user4")));
        users.put("user4", new User("user4", "user4", false, 1, 1, 10, Collections.singletonList("user3")));
        users.put("user5", new User("user5", "user5", false, 2, 2, 10));
        users.put("admin", new User("admin", "admin", true, 0, 0, 10, Arrays.asList("user1", "user2")));
    }

    public static void main(String[] args) throws IOException {
        Map<String, User> users = new HashMap<>();
        ServerSocket ss = new ServerSocket(12345);
        ReentrantReadWriteLock l = new ReentrantReadWriteLock();
        Lock rl = l.readLock();
        Lock wl = l.writeLock();
        Condition notEmpty = wl.newCondition();
        Condition covidDanger = wl.newCondition();

        initUsers(users, wl);

        while(true) {
            Socket socket = ss.accept();

            Thread clientHandler = new Thread(new ClientHandler(users, socket, rl, wl, notEmpty, covidDanger));

            clientHandler.start();
        }
    }
}
