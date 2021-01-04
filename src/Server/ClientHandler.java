package Server;

import Exceptions.CurrentLocationException;
import Exceptions.UserAlreadyExistsException;
import Exceptions.UserDoesntExistException;
import Exceptions.WrongPasswordException;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private static final int N = 10;
    private Map<String, User> users;
    private Socket socket;
    private Lock rl;
    private Lock wl;
    private Condition notEmpty;
    private Condition covidDanger;
    private DataInputStream in;
    private DataOutputStream out;
    private User user;

    public ClientHandler(Map<String, User> users, Socket socket, Lock rl, Lock wl, Condition notEmpty, Condition covidDanger) throws IOException {
        this.users = users;
        this.rl = rl;
        this.wl = wl;
        this.socket = socket;
        this.notEmpty = notEmpty;
        this.covidDanger = covidDanger;
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.out = new DataOutputStream(this.socket.getOutputStream());
        this.user = null;
    }

    public void run() {
        try {
            int flag = interpreter_initial();

            if(flag==1) {
                Thread contact = new Thread(new ContactHandler(user, users, wl));
                Thread danger = new Thread(new DangerHandler(user, users, wl, covidDanger, out));
                contact.start();
                danger.start();

                interpreter_menu();
            }
            in.close();
            out.close();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void interpreter_menu() throws IOException {
        boolean flag = true;
        boolean admin = user.isAdmin();
        String options;
        if(admin) {
            options =   "\n----------------------------------------" +
                        "\n               Menu Admin" +
                        "\n----------------------------------------" +
                        "\n 1 | Atualizar Localização" +
                        "\n 2 | Numero de pessoas numa localização" +
                        "\n 3 | Pedir para informar sobre um local" +
                        "\n 4 | Comunicar que está doente" +
                        "\n 5 | Mapa de Localizações" +
                        "\n 0 | Sair" +
                        "\n----------------------------------------" +
                        "\nEscolha uma opção: ";
        } else {
            options =   "\n----------------------------------------" +
                        "\n            Menu Utilizador" +
                        "\n----------------------------------------" +
                        "\n 1 | Atualizar Localização" +
                        "\n 2 | Numero de pessoas numa localização" +
                        "\n 3 | Pedir para informar sobre um local" +
                        "\n 4 | Comunicar que está doente" +
                        "\n 0 | Sair" +
                        "\n----------------------------------------" +
                        "\nEscolha uma opção: ";
        }

        while(flag) {
            int option;
            if(admin)
                option = lerInt(0, 5, options);
            else
                option = lerInt(0, 4, options);

            switch(option) {
                case 1:
                    try {
                        interpreter_1();
                        printClient("Atualizado com sucesso");
                    } catch (CurrentLocationException e) {
                        printClient(e.getMessage());
                    }
                    break;
                case 2:
                    int res = interpreter_2();
                    printClient("Existem " + res + " pessoas nesse local");
                    break;
                case 3:
                    interpreter_3();
                    printClient("Será informado logo que o espaço esteja livre");
                    break;
                case 4:
                    interpreter_4();
                    printClient("Informação atualizada com sucesso");
                    break;
                case 5:
                    interpreter_5();
                    break;
                case 0:
                    flag = false;
                    break;
            }
        }
    }

    private void interpreter_1() throws IOException, CurrentLocationException {
        int localX = lerInt(0, N-1, "Introduza a sua coordenada latitudinal (0 a " +(N-1)+ "): ");
        int localY = lerInt(0, N-1, "Introduza a sua coordenada longitudinal (0 a " +(N-1)+ "): ");

        wl.lock();
        try {
            int oldLocalX = user.getLocalx();
            int oldLocalY = user.getLocaly();

            if(oldLocalX==localX && oldLocalY==localY)
                throw new CurrentLocationException("Esta é a sua localização atual");

            user.setLocal(localX, localY);

            update_contacts();

            if(users.values().stream().noneMatch(us -> us.getLocalx() == oldLocalX && us.getLocaly() == oldLocalY))
                notEmpty.signalAll();
        } finally {
            wl.unlock();
        }
    }

    private int interpreter_2() throws IOException {
        int localX = lerInt(0, N-1, "Introduza a coordenada latitudinal desejada (0 a " +(N-1)+ "): ");
        int localY = lerInt(0, N-1, "Introduza a coordenada longitudinal desejada (0 a " +(N-1)+ "): ");

        rl.lock();
        try {
            return (int) users.values().stream().filter(u -> u.getLocalx() == localX && u.getLocaly() == localY).count();
        } finally {
            rl.unlock();
        }
    }

    private void interpreter_3() throws IOException {
        int localX = lerInt(0, N-1, "Introduza a coordenada latitudinal desejada (0 a " +(N-1)+ "): ");
        int localY = lerInt(0, N-1, "Introduza a coordenada longitudinal desejada (0 a " +(N-1)+ "): ");

        Runnable emptyPlaceHandler = () -> {
            wl.lock();
            try {
                while(users.values().stream().anyMatch(u -> u.getLocalx() == localX && u.getLocaly() == localY))
                    notEmpty.await();

                printClient("\n----------------------" +
                            "\nO local " + localX + " " + localY + " está vazio" +
                            "\n----------------------");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } finally {
                wl.unlock();
            }
        };

        new Thread(emptyPlaceHandler).start();
    }

    private void interpreter_4() throws IOException {
        int res = lerInt(0, 1, "Está com Covid19? (0-Não/ 1-Sim)");

        wl.lock();
        try {
            if (res == 1) {
                user.setCovid(true);
                covidDanger.signalAll();
            }
        } finally {
            wl.unlock();
        }
    }

    private void interpreter_5() throws IOException {
        int[][] usrs = new int[N][N];
        int[][] contaminated = new int[N][N];

        rl.lock();
        try {
            for(User u: users.values())
                for(int i=0; i<N; i++)
                    for(int j=0; j<N; j++)
                        if(u.getLocal(i, j)) {
                            usrs[i][j]++;
                            if(u.isCovid())
                                contaminated[i][j]++;
                        }
        } finally {
            rl.unlock();
        }

        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                printClient("Localização " + i + " " + j + ": " + contaminated[i][j] + "/" + usrs[i][j]  + "(Contaminated/Users)");
    }

    private int interpreter_initial() throws IOException {
        int flag = 0; // 0-not log 1-log 2-exit
        String options =    "\n-------------------" +
                            "\n     Menu Login" +
                            "\n-------------------" +
                            "\n 1 | Login" +
                            "\n 2 | Registar" +
                            "\n 0 | Sair" +
                            "\n-------------------" +
                            "\nEscolha uma opção: ";

        while(flag==0) {
            int option = lerInt(0, 2, options);

            switch (option) {
                case 1:
                    try {
                        interpreter_login();
                        flag = 1;
                        printClient("Autenticado com sucesso");
                    } catch (UserDoesntExistException | WrongPasswordException e) {
                        printClient(e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        interpreter_register();
                        printClient("Registado com sucesso");
                    } catch (UserAlreadyExistsException e) {
                        printClient(e.getMessage());
                    }
                    break;
                case 0:
                    flag=2;
                    break;
            }
        }

        return flag;
    }

    private void interpreter_login() throws IOException, UserDoesntExistException, WrongPasswordException {
        String username = lerString("Introduza o nome de utilizador: ");
        String password = lerString("Introduza a palavra pass: ");

        rl.lock();
        try {
            if (users.get(username) == null)
                throw new UserDoesntExistException("O utilizador não existe");
            else if(!users.get(username).validateCredentials(password))
                throw new WrongPasswordException("Palavra Pass errada");

            this.user = users.get(username);
        } finally {
            rl.unlock();
        }
    }

    private void interpreter_register() throws IOException, UserAlreadyExistsException {
        String username = lerString("Introduza o nome de utilizador: ");
        String password = lerString("Introduza a palavra pass: ");
        int localX = lerInt(0, N-1, "Introduza a sua coordenada latitudinal (0 a " +(N-1)+ "): ");
        int localY = lerInt(0, N-1, "Introduza a sua coordenada longitudinal (0 a " +(N-1)+ "): ");

        wl.lock();
        try {
            if (users.get(username) != null)
                throw new UserAlreadyExistsException("O utilizador já existe");

            users.put(username, new User(username, password,false, localX, localY, N, wl.newCondition()));
            update_contacts();
        } finally {
            wl.unlock();
        }
    }

    private void update_contacts() {
        Set<User> people = users.values().stream().filter(us -> us.getLocalx() == user.getLocalx() && us.getLocaly() == user.getLocalx()).collect(Collectors.toSet());

        for (User us : people) {
            us.getContactCon().signal();
        }
    }

    private String lerString(String message) throws IOException {
        String line;

        printClient(message);

        line = in.readUTF();

        return line;
    }

    private int lerInt(int min, int max, String message) throws IOException {
        int n;

        do{
            printClient(message);

            try {
                n = Integer.parseInt(in.readUTF());
            } catch (NumberFormatException | IOException nfe) {
                n = -1;
            }
        } while (n < min || n > max);

        return n;
    }

    private void printClient(String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }
}
