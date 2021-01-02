package Server;

import Exceptions.UserAlreadyExistsException;
import Exceptions.UserDoesntExistException;
import Exceptions.WrongPasswordException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private Map<String, User> users;
    private ReentrantLock l;
    private Condition notEmpty;
    private Condition contact;
    private BufferedReader in;
    private PrintWriter out;
    private String user;

    public ClientHandler(Map<String, User> users, Socket socket, ReentrantLock l, Condition notEmpty, Condition contact) throws IOException {
        this.users = users;
        this.l = l;
        this.notEmpty = notEmpty;
        this.contact = contact;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
        this.user = null;
    }

    public void run() {
        try {
            Runnable contactHandler = () -> {
                l.lock();
                try {
                    while(true) {
                        contact.await();
                        printClient("Esteve em contacto com um doente contaminado com covid-19");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    l.unlock();
                }
            };
            new Thread(contactHandler).start();

            interpreter_initial();

            interpreter_menu();

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void interpreter_menu() {
        boolean flag = true;
        boolean admin = users.get(user).isAdmin();
        String options;
        if(admin) {
            options = "\n 1 | Atualizar Localização" +
                             "\n 2 | Numero de pessoas numa localização" +
                             "\n 3 | Pedir para informar sobre um local" +
                             "\n 4 | Comunicar que está doente" +
                             "\n 5 | Mapa de Localizações" +
                             "\n 0 | Sair" +
                             "\nEscolha uma opção: ";
        } else {
            options = "\n 1 | Atualizar Localização" +
                    "\n 2 | Numero de pessoas numa localização" +
                    "\n 3 | Pedir para informar sobre um local" +
                    "\n 4 | Comunicar que está doente" +
                    "\n 0 | Sair" +
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
                    interpreter_1();
                    printClient("Atualizado com sucesso");
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

    private void interpreter_1() {
        int localX = lerInt(0, 10, "Introduza a sua coordenada latitudinal (0 a 10): ");
        int localY = lerInt(0, 10, "Introduza a sua coordenada longitudinal (0 a 10): ");

        l.lock();
        try {
            User u = users.get(user);
            int oldLocalX = u.getLocalx();
            int oldLocalY = u.getLocaly();

            u.setLocal(localX, localY);

            update_contacts(localX, localY);

            if(users.values().stream().noneMatch(us -> us.getLocalx() == oldLocalX && us.getLocaly() == oldLocalY))
                notEmpty.signalAll();
        } finally {
            l.unlock();
        }
    }

    private void update_contacts(int localX, int localY) {
        List<String> contacts = users.values().stream().filter(us -> us.getLocalx() == localX && us.getLocaly() == localY)
                                                       .map(u -> u.getUsername()).collect(Collectors.toList());

        for(String u : contacts) {
            users.get(u).addContact(user);
            users.get(user).addContact(u);
        }
    }

    private int interpreter_2() {
        int localX = lerInt(0, 10, "Introduza a coordenada latitudinal desejada (0 a 10): ");
        int localY = lerInt(0, 10, "Introduza a coordenada longitudinal desejada (0 a 10): ");

        l.lock();
        try {
            return (int) users.values().stream().filter(u -> u.getLocalx() == localX && u.getLocaly() == localY).count();
        } finally {
            l.unlock();
        }
    }

    private void interpreter_3() {
        int localX = lerInt(0, 9, "Introduza a coordenada latitudinal desejada (0 a 9): ");
        int localY = lerInt(0, 9, "Introduza a coordenada longitudinal desejada (0 a 9): ");

        Runnable emptyPlaceHandler = () -> {
            l.lock();
            try {
                while(users.values().stream().anyMatch(u -> u.getLocalx() == localX && u.getLocaly() == localY))
                    notEmpty.await();

                printClient("** O LOCAL " + localX + " " + localY + " ESTÁ VAZIO **");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                l.unlock();
            }
        };

        new Thread(emptyPlaceHandler).start();
    }

    private void interpreter_4() {
        int res = lerInt(0, 1, "Está com Covid19? (0-Não/ 1-Sim)");

        l.lock();
        try {
            if (res == 1) {
                users.get(user).setCovid(true);
                //contact.signalAll();
            }
        } finally {
            l.unlock();
        }
    }

    private void interpreter_5() {
        int[][] usrs = new int[10][10];
        int[][] contaminated = new int[10][10];

        l.lock();
        try {
            for(User u: users.values())
                for(int i=0; i<10; i++)
                    for(int j=0; j<10; j++)
                        if(u.getLocal(i, j)) {
                            usrs[i][j]++;
                            if(u.isCovid())
                                contaminated[i][j]++;
                        }
        } finally {
            l.unlock();
        }

        for(int i=0; i<10; i++)
            for(int j=0; j<10; j++)
                printClient("Localização " + i + " " + j + ": " + contaminated[i][j] + "/" + usrs[i][j]  + "(Contaminated/Users)");
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

        l.lock();
        try {
            if (users.get(username) == null)
                throw new UserDoesntExistException("O utilizador não existe");
            else if(!users.get(username).validateCredentials(password))
                throw new WrongPasswordException("Palavra Pass errada");

            this.user = username;
        } finally {
            l.unlock();
        }
    }

    private void interpreter_register() throws IOException, UserAlreadyExistsException {
        String username = lerString("Introduza o nome de utilizador: ");
        String password = lerString("Introduza a palavra pass: ");
        int localX = lerInt(0, 10, "Introduza a sua coordenada latitudinal (0 a 10): ");
        int localY = lerInt(0, 10, "Introduza a sua coordenada longitudinal (0 a 10): ");

        l.lock();
        try {
            if (users.get(username) != null)
                throw new UserAlreadyExistsException("O utilizador já existe");

            users.put(username, new User(username, password,false, localX, localY));
            update_contacts(localX, localY);
        } finally {
            l.unlock();
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
