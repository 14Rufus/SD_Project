package Server;

import java.io.DataOutputStream;
import java.io.IOException;

public class DangerHandler implements Runnable {
    private final User user;
    private final DataOutputStream out;

    public DangerHandler(User user, DataOutputStream out) {
        this.user = user;
        this.out = out;
    }

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
