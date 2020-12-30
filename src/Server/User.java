package Server;

public class User {
    private String userName;
    private String password;
    private boolean admin;
    private int localx;
    private int localy;

    public User(String userName, String password, int localx, int localy) {
        this.userName = userName;
        this.password = password;
        this.localx = localx;
        this.localy = localy;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLocalx() {
        return localx;
    }

    public void setLocalx(int localx) {
        this.localx = localx;
    }

    public int getLocaly() {
        return localy;
    }

    public void setLocaly(int localy) {
        this.localy = localy;
    }

    public boolean validateCredentials(String password) {
        return this.password.equals(password);
    }
}
