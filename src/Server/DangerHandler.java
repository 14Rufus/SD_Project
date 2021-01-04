package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DangerHandler implements Runnable {
    private User user;
    private Map<String, User> users;
    private Lock l;
    private Condition danger;
    private DataOutputStream out;

    public DangerHandler(User user, Map<String, User> users, Lock l, Condition danger, DataOutputStream out) {
        this.user = user;
        this.users = users;
        this.l = l;
        this.danger = danger;
        this.out = out;
    }

    public void run() {
        l.lock();
        try {
            while(true) {
                boolean onHold = true;

                while (onHold) {
                    danger.await();
                    onHold = false;
                }

                for(String u : user.getContacts())
                    if(users.get(u).isCovid()) {
                        user.removeContact(u);

                        out.writeUTF("\n-------------------------------------------" +
                                    "\nEsteve em contacto com um doente de Covid19" +
                                    "\n-------------------------------------------");
                        out.flush();
                    }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            l.unlock();
        }
    }
}
