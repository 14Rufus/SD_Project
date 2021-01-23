package Server;

import java.io.DataOutputStream;
import java.io.IOException;

public class EmptyPlaceHandler implements Runnable {
    private final UserMap users;
    private final DataOutputStream out;
    private final int localX;
    private final int localY;

    public EmptyPlaceHandler(UserMap users, DataOutputStream out, int localX, int localY) {
        this.users = users;
        this.out = out;
        this.localX = localX;
        this.localY = localY;
    }

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
