package Server;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class UserMap {
    private final Map<String, User> users;
    private final Lock rl;
    private final Lock wl;
    private final Condition notEmpty;

    public UserMap() {
        this.users = new HashMap<>();
        ReentrantReadWriteLock l = new ReentrantReadWriteLock();
        this.rl = l.readLock();
        this.wl = l.writeLock();
        this.notEmpty = wl.newCondition();

        initUsers();
    }

    private void initUsers() {
        users.put("user1", new User("user1", "user1", false, 0, 0, 10, Arrays.asList("user2", "admin")));
        users.put("user2", new User("user2", "user2", false, 0, 0, 10, Arrays.asList("user1", "admin")));
        users.put("user3", new User("user3", "user3", false, 1, 1, 10, Collections.singletonList("user4")));
        users.put("user4", new User("user4", "user4", false, 1, 1, 10, Collections.singletonList("user3")));
        users.put("user5", new User("user5", "user5", false, 2, 2, 10));
        users.put("admin", new User("admin", "admin", true, 0, 0, 10, Arrays.asList("user1", "user2")));
    }

    public User get(String u) {
        return users.get(u);
    }

    public void put(String u, User user) {
        users.put(u, user);
    }

    public Lock getReadLock() {
        return rl;
    }

    public Lock getWriteLock() {
        return wl;
    }

    public Condition getNotEmptyCon() {
        return notEmpty;
    }

    public List<User> userList() {
        return new ArrayList<>(users.values());
    }

    public int userNumber() {
        return users.size();
    }

    public boolean emptyLocal(int localX, int localY) {
        return users.values().stream().noneMatch(us -> us.getLocalx() == localX && us.getLocaly() == localY && us.isOnline());
    }

    public int peopleInLocation(int localX, int localY) {
        return (int) users.values().stream().filter(u -> u.getLocalx() == localX && u.getLocaly() == localY && u.isOnline()).count();
    }

    public Set<String> peopleInLocationSet(int localX, int localY) {
        return users.values().stream().filter(us -> us.getLocalx() == localX && us.getLocaly() == localY && us.isOnline()).map(u -> u.getUsername()).collect(Collectors.toSet());
    }
}
