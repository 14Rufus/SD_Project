package Server;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 *  Classe que representa o mapa de localizações dos Utilizadores
 */
public class UserMap {
    private final Map<String, User> users;
    private final Lock rl;
    private final Lock wl;
    private final Condition notEmpty;

    /**
     *  Construtor da Classe
     */
    public UserMap() {
        this.users = new HashMap<>();
        ReentrantReadWriteLock l = new ReentrantReadWriteLock();
        this.rl = l.readLock();
        this.wl = l.writeLock();
        this.notEmpty = wl.newCondition();

        initUsers();
    }

    /**
     * Inicializa os Utilizadores
     */
    private void initUsers() {
        users.put("user1", new User("user1", "user1", false, 0, 0, 10, Arrays.asList("user2", "admin")));
        users.put("user2", new User("user2", "user2", false, 0, 0, 10, Arrays.asList("user1", "admin")));
        users.put("user3", new User("user3", "user3", false, 1, 1, 10, Collections.singletonList("user4")));
        users.put("user4", new User("user4", "user4", false, 1, 1, 10, Collections.singletonList("user3")));
        users.put("user5", new User("user5", "user5", false, 2, 2, 10));
        users.put("admin", new User("admin", "admin", true, 0, 0, 10, Arrays.asList("user1", "user2")));
    }

    /**
     * Obtém um Utilizador do mapa de Utilizadores
     *
     * @param u Utilizador pretendido
     * @return  Utilizador obtido
     */
    public User get(String u) {
        return users.get(u);
    }

    /**
     * Adiciona um Utilizador ao mapa de Utilizadores
     *
     * @param u     Username do Utilizador
     * @param user  Utilizador
     */
    public void put(String u, User user) {
        users.put(u, user);
    }

    /**
     * Obtém o ReadLock do UserMap
     *
     * @return  Lock do UserMap
     */
    public Lock getReadLock() {
        return rl;
    }

    /**
     * Obtém o WriteLock do UserMap
     *
     * @return  Lock do UserMap
     */
    public Lock getWriteLock() {
        return wl;
    }

    /**
     * Obtém a condition notEmpty
     *
     * @return  Condition notEmpty
     */
    public Condition getNotEmptyCon() {
        return notEmpty;
    }

    /**
     * Obtém a lista de Utilizadores
     *
     * @return  Lista com os Utilizadores
     */
    public List<User> userList() {
        return new ArrayList<>(users.values());
    }

    /**
     * Obtém o número de Utilizadores
     *
     * @return  Inteiro que representa o número de Utilizadores
     */
    public int userNumber() {
        return users.size();
    }

    /**
     * Verifica se uma determinada localização está vazia
     *
     * @param localX    Coordenada x da localização
     * @param localY    Coordenada y da Localização
     * @return          Booelano que representa a ocupação da localização
     */
    public boolean emptyLocal(int localX, int localY) {
        return users.values().stream().noneMatch(us -> us.getLocalx() == localX && us.getLocaly() == localY && us.isOnline());
    }

    /**
     * Obtém o número de Utilizadores numa determinada localização
     *
     * @param localX    Coordenada x da localização
     * @param localY    Coordenada y da localização
     * @return          Inteiro que representa o número de Utilizadores na localização
     */
    public int peopleInLocation(int localX, int localY) {
        return (int) users.values().stream().filter(u -> u.getLocalx() == localX && u.getLocaly() == localY && u.isOnline()).count();
    }

    /**
     * Obtém o username de todos os Utilizadores num determinada localização
     *
     * @param localX    Coordenada x da localização
     * @param localY    Coordenada y da localização
     * @return          Conjunto de usernames dos Utilizadores presentes na localização
     */
    public Set<String> peopleInLocationSet(int localX, int localY) {
        return users.values().stream().filter(us -> us.getLocalx() == localX && us.getLocaly() == localY && us.isOnline()).map(u -> u.getUsername()).collect(Collectors.toSet());
    }
}
