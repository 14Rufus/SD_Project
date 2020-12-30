package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientHandler implements Runnable {
    private Map<String, User> users;
    private Socket socket;
    private ReentrantReadWriteLock l;
    private Lock rl;
    private Lock wl;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Map<String, User> users, Socket socket) throws IOException {
        this.users = users;
        this.socket = socket;
        this.l = new ReentrantReadWriteLock();
        this.rl = l.readLock();
        this.wl = l.writeLock();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
    }

    private void decode_login(String line) {
        String[] codes = line.split(":");
        String message;

        rl.lock();
        try {
            if (users.get(codes[1]) == null)
                message = "1:denied";
            else {
                if (users.get(codes[1]).validateCredentials(codes[2]))
                    message = "1:approved";
                else
                    message = "1:denied";
            }
        } finally {
            rl.unlock();
        }

        respond_client(message);
    }

    private void decode(String line) {
        String[] codes = line.split(":");

        switch(codes[0]) {
            case("1"):
                decode_login(line);
                break;
        }
    }

    private void respond_client(String message) {
        out.println(message);
    }

    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                decode(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
