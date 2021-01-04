package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ContactHandler implements Runnable{
    private ReentrantLock l;
    private Condition con;
    private DataOutputStream out;

    public ContactHandler(ReentrantLock l, Condition con, DataOutputStream out) {
        this.l = l;
        this.con = con;
        this.out = out;
    }

    public void run() {
        l.lock();
        try {
            while(true) {
                con.await();

                out.writeUTF("Esteve em contacto com um doente contaminado com covid-19");
                out.flush();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            l.unlock();
        }
    }
}
