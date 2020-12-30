package Client;

public class Log {
    boolean login;
    boolean wait;

    public Log() {
        this.login = false;
        this.wait = true;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public void setWaitFalse() {
        this.wait = false;
        notifyAll();
    }

    public void setWaitTrue() throws InterruptedException {
        this.wait = true;
        while(this.wait == true)
            wait();
    }
}
