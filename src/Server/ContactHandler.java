package Server;

import java.util.Set;

/**
 * Classe que controla os contactos de um Utilizador
 */
public class ContactHandler implements Runnable {
    private final User user;
    private final UserMap users;

    /**
     * Construtor da Classe
     *
     * @param user  Utilizador
     * @param users Mapa de Utilizadores
     */
    public ContactHandler(User user, UserMap users) {
        this.user = user;
        this.users = users;
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
