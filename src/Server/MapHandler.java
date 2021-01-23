package Server;

import java.util.concurrent.locks.ReentrantLock;

public class MapHandler implements Runnable{
    private final int[][] users;
    private final int[][] contaminated;
    private final ReentrantLock lock;
    private final User user;
    private final int N;

    public MapHandler(int[][] users, int[][] contaminated, ReentrantLock lock, User user, int N) {
        this.users = users;
        this.contaminated = contaminated;
        this.lock = lock;
        this.user = user;
        this.N = N;
    }

    public void run(){
        boolean local;
        boolean isCovid;
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++) {
                user.getLock().lock();
                try {
                    local = user.getLocal(i, j);
                    isCovid = user.isCovid();
                } finally { user.getLock().unlock();}

                lock.lock();
                try {
                    if (local) {
                        users[i][j]++;
                        if (isCovid)
                            contaminated[i][j]++;
                    }
                } finally {
                    lock.unlock();
                }
            }
    }
}
