package Server;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe que controla o aviso de uma localização vazia
 */
public class EmptyPlaceHandler implements Runnable {
    private final UserMap users;
    private final DataOutputStream out;
    private final int localX;
    private final int localY;

    /**
     * Construtor da classe
     *
     * @param users     Mapa de Utilizadores
     * @param out       Output
     * @param localX    Coordenada x da localização
     * @param localY    Coordenada y da localização
     */
    public EmptyPlaceHandler(UserMap users, DataOutputStream out, int localX, int localY) {
        this.users = users;
        this.out = out;
        this.localX = localX;
        this.localY = localY;
    }

    /**
     * Define o comportamento de uma thread na classe
     */
    public void run() {
        users.getWriteLock().lock();
        try {
            while(!users.emptyLocal(localX, localY))
                users.getNotEmptyCon().await();

            out.writeUTF("\n------------------------" +
                             "\n O local " + localX + " " + localY + " está vazio" +
                             "\n------------------------");
            out.flush();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            users.getWriteLock().unlock();
        }
    }
}
