package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Reader implements Runnable {
    private Socket socket;
    private Log log;
    private BufferedReader in;

    public Reader(Socket socket, Log log) throws IOException {
        this.socket = socket;
        this.log = log;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void decode_login(String line) {
        String[] codes = line.split(":");

        if(codes[1].equals("approved")) {
            log.setLogin(true);
            log.setWaitFalse();
        } else {
            log.setLogin(false);
            log.setWaitFalse();
        }
    }

    private void decode(String line) {
        String[] codes = line.split(":");

        switch(codes[0]) {
            case("1"):
                decode_login(line);
                break;
        }
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
