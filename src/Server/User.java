package Server;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    private final String username;
    private final String password;
    private final boolean admin;
    private boolean online;
    private boolean covid;
    private int localx;
    private int localy;
    private final boolean[][] locals;
    private final ReentrantLock lock;
    private final Condition contactCon;
    private final Condition dangerCon;
    private final Set<String> contacts;

    public User(String username, String password, boolean admin, int localx, int localy, int N) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.online = false;
        this.localx = localx;
        this.localy = localy;
        this.covid = false;
        this.contacts = new TreeSet<>();
        this.locals = new boolean[N][N];
        this.locals[localx][localy] = true;
        this.lock = new ReentrantLock();
        this.contactCon = lock.newCondition();
        this.dangerCon = lock.newCondition();
    }

    public User(String username, String password, boolean admin, int localx, int localy, int N, List<String> contacts) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.online = false;
        this.localx = localx;
        this.localy = localy;
        this.covid = false;
        this.contacts = new TreeSet<>();
        this.contacts.addAll(contacts);
        this.locals = new boolean[N][N];
        this.locals[localx][localy] = true;
        this.lock = new ReentrantLock();
        this.contactCon = lock.newCondition();
        this.dangerCon = lock.newCondition();
    }

    public String getUsername() {
        return username;
    }

    public int getLocalx() {
        return localx;
    }

    public int getLocaly() {
        return localy;
    }

    public void setLocal(int localx, int localy) {
        this.localx = localx;
        this.localy = localy;

        this.locals[localx][localy] = true;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setCovid(boolean covid) {
        this.covid = covid;
    }

    public boolean isCovid() {
        return covid;
    }

    public boolean validateCredentials(String password) {
        return this.password.equals(password);
    }

    public void addContact(String contact) {
        contacts.add(contact);
    }

    public Set<String> getContacts() {
        return contacts;
    }

    public boolean getLocal(int localx, int localy) {
        return locals[localx][localy];
    }

    public Condition getContactCon() {
        return contactCon;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Condition getDangerCon() {
        return dangerCon;
    }
}
