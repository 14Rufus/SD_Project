package Client;

import java.io.IOException;
import java.net.Socket;

/**
 * Classe que implementa o Cliente
 */
public class Client {

    /**
     * Método que conecta ao Servidor e inicializa os sockets para comunicação
     *
     * @param args          Argumentos que poderão ser passados aquando da inicialização
     * @throws IOException  Exceção lançada caso algo inesperado ocorra
     */
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        Thread writer = new Thread(new Writer(socket));
        Thread reader = new Thread(new Reader(socket));

        writer.start();
        reader.start();
    }
}
