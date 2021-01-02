package Server;

import Exceptions.UserAlreadyExistsException;
import Exceptions.UserDoesntExistException;
import Exceptions.WrongPasswordException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientHandler implements Runnable {
    private Map<String, User> users;
    private Socket socket;
    private ReentrantReadWriteLock l;
    private Lock rl;
    private Lock wl;
    private BufferedReader in;
    private PrintWriter out;
    private String user;

    public ClientHandler(Map<String, User> users, Socket socket) throws IOException {
        this.users = users;
        this.socket = socket;
        this.l = new ReentrantReadWriteLock();
        this.rl = l.readLock();
        this.wl = l.writeLock();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
        this.user = null;
    }

    public void run() {
        try {
            interpreter_initial();

            interpreter_menu();

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void interpreter_menu() {
        boolean flag=true;
        String options = "\n 1 | Atualizar Localização" +
                         "\n 2 | Numero de pessoas numa localização" +
                         "\n 3 | Pedir para informar sobre um local" +
                         "\n 4 | Comunicar que está doente" +
                         "\n 0 | Sair" +
                         "\nEscolha uma opção: ";

        while(flag) {
            int option = lerInt(0, 4, options);

            switch(option) {
                case 1:
                    interpreter_1();
                    printClient("Atualizado com sucesso");
                    break;
                case 2:
                    int res = interpreter_2();
                    printClient("Existem " + res + " pessoas nesse local");
                    break;
                case 3:
                    interpreter_3();
                    break;
                case 4:
                    interpreter_4();
                    printClient("Informação atualizada com sucesso");
                    break;
                case 0:
                    flag = false;
                    break;
            }
        }
    }

    private void interpreter_1() {
        int localX = lerInt(0, 10, "Introduza a sua coordenada latitudinal (0 a 10): ");
        int localY = lerInt(0, 10, "Introduza a sua coordenada longitudinal (0 a 10): ");

        wl.lock();
        try {
            User u = users.get(user);
            u.setLocalx(localX);
            u.setLocaly(localY);
        } finally {
            wl.unlock();
        }
    }

    private int interpreter_2() {
        int localX = lerInt(0, 10, "Introduza a coordenada latitudinal desejada (0 a 10): ");
        int localY = lerInt(0, 10, "Introduza a coordenada longitudinal desejada (0 a 10): ");

        rl.lock();
        try {
            return (int) users.values().stream().filter(u -> u.getLocalx() == localX && u.getLocaly() == localY && !u.getUsername().equals(user)).count();
        } finally {
            rl.unlock();
        }
    }

    private void interpreter_3() {

    }

    private void interpreter_4() {
        int res = lerInt(0, 1, "Está com Covid19? (0-Não/ 1-Sim)");

        wl.lock();
        try {
            if (res == 1)
                users.get(user).setCovid(true);
        } finally {
            wl.unlock();
        }
    }

    private void interpreter_initial() throws IOException {
        boolean flag = true;
        String options = "\n 1 | Login" +
                         "\n 2 | Registar" +
                         "\nEscolha uma opção: ";

        while(flag) {
            int option = lerInt(1, 2, options);

            switch (option) {
                case 1:
                    try {
                        interpreter_login();
                        flag = false;
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
            }
        }
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

            this.user = username;
        } finally {
            rl.unlock();
        }
    }

    private void interpreter_register() throws IOException, UserAlreadyExistsException {
        String username = lerString("Introduza o nome de utilizador: ");
        String password = lerString("Introduza a palavra pass: ");
        int localX = lerInt(0, 10, "Introduza a sua coordenada latitudinal (0 a 10): ");
        int localY = lerInt(0, 10, "Introduza a sua coordenada longitudinal (0 a 10): ");

        wl.lock();
        try {
            if (users.get(username) != null)
                throw new UserAlreadyExistsException("O utilizador já existe");

            users.put(username, new User(username, password, localX, localY));
        } finally {
            wl.unlock();
        }
    }

    private String lerString(String message) throws IOException {
        String line;

        do{
            printClient(message);

            line = in.readLine();
        } while (line == null);

        return line;
    }

    private int lerInt(int min, int max, String message){
        int n;

        do{
            printClient(message);

            try {
                String line = in.readLine();
                n = Integer.parseInt(line);
            } catch (NumberFormatException | IOException nfe) {
                n = -1;
            }
        } while (n < min || n > max);

        return n;
    }

    private void printClient(String message) {
        out.println(message);
        out.flush();
    }
}
