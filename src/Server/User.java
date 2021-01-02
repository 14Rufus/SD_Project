package Server;

public class User {
    private String username;
    private String password;
    private boolean admin;
    private boolean covid;
    private int localx;
    private int localy;

    public User(String username, String password, boolean admin, int localx, int localy) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.localx = localx;
        this.localy = localy;
        this.covid = false;
    }

    public String getUsername() {
        return username;
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

    public boolean isCovid() {
        return covid;
    }

    public void setCovid(boolean covid) {
        this.covid = covid;
    }

    public boolean validateCredentials(String password) {
        return this.password.equals(password);
    }
}
