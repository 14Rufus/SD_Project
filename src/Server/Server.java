package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe que implementa o Servidor
 */
public class Server {
    /**
     * Método que conecta aos CLientes e inicializa os sockets para comunicação
     *
     * @param args          Argumentos que poderão ser passados aquando da inicialização
     * @throws IOException  Exceção lançada caso algo inesperado ocorra
     */
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);
        UserMap users = new UserMap();

        while(true) {
            Socket socket = ss.accept();

            Thread clientHandler = new Thread(new ClientHandler(users, socket));

            clientHandler.start();
        }
    }
}
