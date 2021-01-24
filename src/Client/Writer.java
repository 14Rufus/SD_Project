package Client;

import java.io.*;
import java.net.Socket;

/**
 * Classe que implementa a escrita por parte do Cliente
 */
public class Writer implements Runnable {
    private BufferedReader in;
    private DataOutputStream out;
    private Socket socket;

    /**
     * Construtor da Classe
     *
     * @param socket        Socket que liga ao Servidor
     * @throws IOException  Exceção lançada caso algo inesperado ocorra
     */
    public Writer(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
    }

    /**
     * Define o comportamento de uma Thread
     */
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
        } catch (IOException ignored) { }
    }
}
