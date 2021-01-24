package Client;

import java.io.*;
import java.net.Socket;

/**
 * Classe que implementa a leitura por parte do Cliente
 */
public class Reader implements Runnable {
    private final Socket socket;
    private final DataInputStream in;

    /**
     * Contrutor da Classe
     *
     * @param socket        Socket que liga ao servidor
     * @throws IOException  Exceção lançada caso algo inesperado ocorra
     */
    public Reader(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    /**
     *  Define comportamento de uma Thread
     */
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
