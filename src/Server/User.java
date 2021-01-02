package Server;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class User {
    private String username;
    private String password;
    private boolean admin;
    private boolean covid;
    private int localx;
    private int localy;
    private Set<String> contacts;
    private boolean[][] locals;

    public User(String username, String password, boolean admin, int localx, int localy) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.localx = localx;
        this.localy = localy;
        this.covid = false;
        this.contacts = new TreeSet<>();
        this.locals = new boolean[10][10];
        this.locals[localx][localy] = true;
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

    public boolean getLocal(int localx, int localy) {
        return locals[localx][localy];
    }
}
