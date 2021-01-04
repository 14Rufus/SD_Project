package Server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ContactHandler implements Runnable {
    private User user;
    private Map<String, User> users;
    private ReentrantLock l;
    private Condition update;

    public ContactHandler(User user, Map<String, User> users, ReentrantLock l) {
        this.user = user;
        this.users = users;
        this.l = l;
        this.update = user.getContactCon();
    }

    public void run() {
        l.lock();
        try {
            while(true) {
                boolean onHold = true;

                while (onHold) {
                    update.await();
                    onHold = false;
                }

                List<String> contacts = users.values().stream().filter(us -> us.getLocalx() == user.getLocalx() && us.getLocaly() == user.getLocalx() && !us.getUsername().equals(user.getUsername()))
                        .map(u -> u.getUsername()).collect(Collectors.toList());

                for (String u : contacts)
                    user.addContact(u);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            l.unlock();
        }
    }
}
