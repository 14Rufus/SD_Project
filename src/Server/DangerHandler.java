package Server;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe que controla o aviso de contacto
 */
public class DangerHandler implements Runnable {
    private final User user;
    private final DataOutputStream out;

    /**
     * Construtor da Classe
     *
     * @param user  Utilizador
     * @param out   Output
     */
    public DangerHandler(User user, DataOutputStream out) {
        this.user = user;
        this.out = out;
    }

    /**
     * Define o comportamento de uma thread na classe
     */
    public void run() {
        user.getLock().lock();
        try {
            while(true) {
                boolean onHold = true;

                while (onHold) {
                    user.getDangerCon().await();
                    onHold= false;
                }

                out.writeUTF("\n-------------------------------------------" +
                        "\nEsteve em contacto com um doente de Covid19" +
                        "\n-------------------------------------------");
                out.flush();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            user.getLock().unlock();
        }
    }
}
