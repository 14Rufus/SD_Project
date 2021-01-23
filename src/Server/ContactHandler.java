package Server;

import java.util.Set;

public class ContactHandler implements Runnable {
    private final User user;
    private final UserMap users;

    public ContactHandler(User user, UserMap users) {
        this.user = user;
        this.users = users;
    }

    public void run() {
        user.getLock().lock();
        try {
            while(true) {
                boolean onHold = true;

                while (onHold) {
                    user.getContactCon().await();
                    onHold = false;
                }

                Set<String> contacts = users.peopleInLocationSet(user.getLocalx(), user.getLocaly());
                contacts.remove(user.getUsername());

                for (String u : contacts)
                    user.addContact(u);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            user.getLock().unlock();
        }
    }
}
