package Server;

import Exceptions.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que controla o Cliente
 */
public class ClientHandler implements Runnable {
    private static final int N = 10;
    private final Socket socket;
    private final UserMap users;
    private final DataInputStream in;
    private final DataOutputStream out;
    private User user;

    /**
     * Construtor da Classe
     *
     * @param users         Mapa de Utilizadores
     * @param socket        Socket que conecta ao Cliente
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    public ClientHandler(UserMap users, Socket socket) throws IOException {
        this.users = users;
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.out = new DataOutputStream(this.socket.getOutputStream());
        this.user = null;
    }

    /**
     * Define o comportamento de uma Thread
     */
    public void run() {
        try {
            int flag = interpreter_initial();

            if(flag==1) {
                Thread contact = new Thread(new ContactHandler(user, users));
                Thread danger = new Thread(new DangerHandler(user, out));
                contact.start();
                danger.start();

                users.getWriteLock().lock();
                try {
                    update_contacts();
                } finally {
                    users.getWriteLock().unlock();
                }

                interpreter_menu();
            }

            in.close();
            out.close();
            socket.close();

        } catch (IOException ignored) {}
    }

    /**
     * Interpretador do menu
     *
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    private void interpreter_menu() throws IOException {
        boolean admin, covid, flag = true;
        int option, localX, localY;

        user.getLock().lock();
        try {
            admin = user.isAdmin();
            covid = user.isCovid();
            localX = user.getLocalx();
            localY = user.getLocaly();
        } finally {
            user.getLock().unlock();
        }

        while(flag && !covid) {
            option = lerInt(admin ? 5 : 4, getMenu(admin, localX, localY));

            switch(option) {
                case 1:
                    try {
                        interpreter_1();
                        printClient("Atualizado com sucesso");

                        user.getLock().lock();
                        try {
                            localX = user.getLocalx();
                            localY = user.getLocaly();
                        } finally {
                            user.getLock().unlock();
                        }
                    } catch (CurrentLocationException e) {
                        printClient(e.getMessage());
                    }
                    break;
                case 2:
                    int res = interpreter_2();
                    printClient("Existem " + res + " pessoas nesse local");
                    break;
                case 3:
                    try {
                        interpreter_3();
                        printClient("Será informado logo que o espaço esteja livre");
                    } catch (CurrentLocationException e) {
                        printClient(e.getMessage());
                    }
                    break;
                case 4:
                    try {
                        interpreter_4();
                        covid = true;
                    } catch (SameStateException e) {
                        printClient(e.getMessage());
                    }
                    break;
                case 5:
                    interpreter_5();
                    break;
                case 0:
                    flag = false;
                    break;
            }
        }

        if (covid) {
            printClient("\n-------------------------------------------------" +
                        "\n Utilizador com covid. Mantenha-se em isolamento" +
                        "\n-------------------------------------------------");
        }

        user.getLock().lock();
        try {
            user.setOnline(false);
        } finally {
            user.getLock().unlock();
        }
    }

    /**
     * Implementa a primeira funcionalidade do menu
     *
     * @throws IOException              Exceção lançada quando algo inesperado ocorre
     * @throws CurrentLocationException Exceção lançada quando a nova localização é igual à anterior
     */
    private void interpreter_1() throws IOException, CurrentLocationException {
        int localX = lerInt(N-1, "Introduza a sua coordenada latitudinal (0 a " +(N-1)+ "): ");
        int localY = lerInt(N-1, "Introduza a sua coordenada longitudinal (0 a " +(N-1)+ "): ");
        int oldLocalX;
        int oldLocalY;

        user.getLock().lock();
        try {
            oldLocalX = user.getLocalx();
            oldLocalY = user.getLocaly();

            if (oldLocalX == localX && oldLocalY == localY)
                throw new CurrentLocationException("Esta é a sua localização atual");

            user.setLocal(localX, localY);
        } finally {
            user.getLock().unlock();
        }

        users.getWriteLock().lock();
        try {
            if (users.emptyLocal(oldLocalX, oldLocalY))
                users.getNotEmptyCon().signalAll();

            update_contacts();
        } finally {
            users.getWriteLock().unlock();
        }
    }

    /**
     * Implementa a segunda funcionalidade do menu
     *
     * @return              Inteiro que representa o número de Utilizadores na localização introduzida
     * @throws IOException  Excaeção lançada quando algo inesperado ocorre
     */
    private int interpreter_2() throws IOException {
        int localX = lerInt(N-1, "Introduza a coordenada latitudinal desejada (0 a " +(N-1)+ "): ");
        int localY = lerInt(N-1, "Introduza a coordenada longitudinal desejada (0 a " +(N-1)+ "): ");

        users.getReadLock().lock();
        try {
            return users.peopleInLocation(localX, localY);
        } finally {
            users.getReadLock().unlock();
        }
    }

    /**
     * Implementa a terceira funcionalidade do menu
     *
     * @throws IOException              Exceção lançada quando algo inesperado ocorre
     * @throws CurrentLocationException Exceção lançada quando a nova localização é igual à anterior
     */
    private void interpreter_3() throws IOException, CurrentLocationException {
        int localX = lerInt(N-1, "Introduza a coordenada latitudinal desejada (0 a " +(N-1)+ "): ");
        int localY = lerInt(N-1, "Introduza a coordenada longitudinal desejada (0 a " +(N-1)+ "): ");
        int oldLocalX;
        int oldLocalY;

        user.getLock().lock();
        try {
            oldLocalX = user.getLocalx();
            oldLocalY = user.getLocaly();
        } finally {
            user.getLock().unlock();
        }

        if (oldLocalX == localX && oldLocalY == localY)
            throw new CurrentLocationException("Esta é a sua localização atual");

        Thread t = new Thread(new EmptyPlaceHandler(users, out, localX ,localY));
        t.start();
    }

    /**
     * Implementa a quarta funcionalidade do menu
     *
     * @throws IOException          Exceção lançada quando algo inesperado ocorre
     * @throws SameStateException   Exceção lançada quando o estado de infeção introduzido é o mesmo que o anterior
     */
    private void interpreter_4() throws IOException, SameStateException {
        Set<String> contacts;
        int res = lerInt(1, "Está com Covid19? (0-Não/ 1-Sim)");

        if (res == 0)
            throw new SameStateException("O seu estado já estava guardado.");

        user.getLock().lock();
        try {
            contacts = user.getContacts();
            user.setCovid(true);
        } finally {
            user.getLock().unlock();
        }

        users.getWriteLock().lock();
        try {
            for (String u: contacts) {
                User usr = users.get(u);

                usr.getLock().lock();
                try {
                    users.get(u).getDangerCon().signal();
                } finally {
                    usr.getLock().unlock();
                }
            }
        } finally {
            users.getWriteLock().unlock();
        }
    }

    /**
     * Implementa a quinta funcionalidade do menu
     *
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    private void interpreter_5() throws IOException {
        int[][] usrs = new int[N][N];
        int[][] contaminated = new int[N][N];
        StringBuilder line;
        ReentrantLock mapLock = new ReentrantLock();

        users.getReadLock().lock();
        try {
            int size = users.userNumber();
            List<User> userList = users.userList();
            Thread[] threadUser = new Thread[size];

            for (int i = 0; i<size; i++) {
                threadUser[i] = new Thread(new MapHandler(usrs, contaminated, mapLock, userList.get(i),N));
                threadUser[i].start();
            }

            for (int i = 0; i<size; i++) {
                threadUser[i].join();
            }
        } catch (InterruptedException ignored) {

        } finally {
            users.getReadLock().unlock();
        }

        printClient("Mapa de Localizações (Contaminado|Total)");

        line = new StringBuilder("   ");
        for(int i=0; i<N; i++)
            line.append(i).append("   ");
        printClient(line.toString());

        for(int i=0; i<N; i++) {
            line = new StringBuilder(i + " ");
            for (int j = 0; j < N; j++)
                line.append(contaminated[i][j]).append("|").append(usrs[i][j]).append(" ");
            printClient(line.toString());
        }
    }

    /**
     * Interpretador do menu inicial
     *
     * @return              Inteiro que indica se uma opção foi escolhida
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    private int interpreter_initial() throws IOException {
        int flag = 0;

        while(flag==0) {
            int option = lerInt(2, getMenuLogin());

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

    /**
     * Interpretador da opção login
     *
     * @throws IOException              Exceção lançada quando algo inesperado ocorre
     * @throws UserDoesntExistException Exceção lançada quando o Utilizador é inexistente
     * @throws WrongPasswordException   Exceção lançada quando a password introduzida é inválida
     */
    private void interpreter_login() throws IOException, UserDoesntExistException, WrongPasswordException {
        String username = lerString("Introduza o nome de utilizador: ");
        String password = lerString("Introduza a palavra pass: ");
        User u;

        users.getReadLock().lock();
        try {
            u = users.get(username);
            if (u == null)
                throw new UserDoesntExistException("O utilizador não existe");

            u.getLock().lock();
        } finally {
            users.getReadLock().unlock();
        }

        try {
            if(!u.validateCredentials(password))
                throw new WrongPasswordException("Palavra Pass errada");

            this.user = u;

            u.setOnline(true);
        } finally {
            u.getLock().unlock();
        }
    }

    /**
     * Interpretador da opção registar
     *
     * @throws IOException                  Exceção lançada quando algo inesperado ocorre
     * @throws UserAlreadyExistsException   Exceção lançada quando o Utilizador u«introduzido já existe
     */
    private void interpreter_register() throws IOException, UserAlreadyExistsException {
        String username = lerString("Introduza o nome de utilizador: ");
        String password = lerString("Introduza a palavra pass: ");
        int localX = lerInt(N-1, "Introduza a sua coordenada latitudinal (0 a " +(N-1)+ "): ");
        int localY = lerInt(N-1, "Introduza a sua coordenada longitudinal (0 a " +(N-1)+ "): ");

        users.getWriteLock().lock();
        try {
            if (users.get(username) != null)
                throw new UserAlreadyExistsException("O utilizador já existe");

            users.put(username, new User(username, password,false, localX, localY, N));

            user = users.get(username);
        } finally {
            users.getWriteLock().unlock();
        }
    }

    /**
     * Atualiza os contactos do Utilizador
     */
    private void update_contacts() {
        int localX, localY;

        user.getLock().lock();
        try{
            localX = user.getLocalx();
            localY = user.getLocaly();
        } finally {
            user.getLock().unlock();
        }

        Set<String> people = users.peopleInLocationSet(localX, localY);

        for (String u : people) {
            User us = users.get(u);
            us.getLock().lock();
            try {
                us.getContactCon().signal();
            } finally {
                us.getLock().unlock();
            }
        }
    }

    /**
     * Lê uma determinada mensagem
     *
     * @param message       Mensagem a ser lida
     * @return              Mensagem lida
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    private String lerString(String message) throws IOException {
        String line;

        printClient(message);

        line = in.readUTF();

        return line;
    }

    /**
     * Lê um determinado inteiro
     *
     * @param max           Representa o valor máximo da opção a ser selecionada
     * @param message       Mensagem a ser enviada para o Utilizador
     * @return              Inteiro lido
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    private int lerInt(int max, String message) throws IOException {
        int n;

        do{
            printClient(message);

            try {
                n = Integer.parseInt(in.readUTF());
            } catch (NumberFormatException | IOException nfe) {
                n = -1;
            }
        } while (n < 0 || n > max);

        return n;
    }

    /**
     * Imprime mensagem para o Cliente
     *
     * @param message       Mensagem a ser impressa
     * @throws IOException  Exceção lançada quando algo inesperado ocorre
     */
    private void printClient(String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }

    /**
     * Obtém o menu do Utilizador/Admin
     *
     * @param admin     Representa se o Utilizador é admin
     * @param localX    Coordenada x da localização do Utilizador
     * @param localY    coordenada y da localização do Utilizador
     * @return          String que representa o menu do Utilizador/Admin
     */
    private String getMenu(boolean admin, int localX, int localY) {
        if(admin) return "\n----------------------------------------" +
                "\n               Menu Admin" +
                "\n            Coordenadas: " + localX + " " + localY +
                "\n----------------------------------------" +
                "\n 1 | Atualizar Localização" +
                "\n 2 | Numero de pessoas numa localização" +
                "\n 3 | Pedir para informar sobre um local" +
                "\n 4 | Comunicar que está doente" +
                "\n 5 | Mapa de Localizações" +
                "\n 0 | Sair" +
                "\n----------------------------------------" +
                "\nEscolha uma opção: ";

        else return "\n----------------------------------------" +
                "\n            Menu Utilizador" +
                "\n           Coordenadas: " + localX + " " + localY +
                "\n----------------------------------------" +
                "\n 1 | Atualizar Localização" +
                "\n 2 | Numero de pessoas numa localização" +
                "\n 3 | Pedir para informar sobre um local" +
                "\n 4 | Comunicar que está doente" +
                "\n 0 | Sair" +
                "\n----------------------------------------" +
                "\nEscolha uma opção: ";
    }

    /**
     * Obtém o menu de Login
     *
     * @return  String que representa o menu de Login
     */
    private String getMenuLogin() {
        return  "\n-------------------" +
                "\n     Menu Login" +
                "\n-------------------" +
                "\n 1 | Login" +
                "\n 2 | Registar" +
                "\n 0 | Sair" +
                "\n-------------------" +
                "\nEscolha uma opção: ";
    }
}
