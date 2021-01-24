package Server;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que representa um Utilizador
 */
public class User {
    private final String username;
    private final String password;
    private final boolean admin;
    private boolean online;
    private boolean covid;
    private int localx;
    private int localy;
    private final boolean[][] locals;
    private final ReentrantLock lock;
    private final Condition contactCon;
    private final Condition dangerCon;
    private final Set<String> contacts;

    /**
     * Construtor da Classe
     *
     * @param username  Username do Utilizador
     * @param password  Password do Utilizador
     * @param admin     Admin usado no contrutor
     * @param localx    Coordenada x do Utilizador
     * @param localy    Coordenada y do Utilizador
     * @param N         Tamanho do mapa usado no Contrutor
     */
    public User(String username, String password, boolean admin, int localx, int localy, int N) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.online = false;
        this.localx = localx;
        this.localy = localy;
        this.covid = false;
        this.contacts = new TreeSet<>();
        this.locals = new boolean[N][N];
        this.locals[localx][localy] = true;
        this.lock = new ReentrantLock();
        this.contactCon = lock.newCondition();
        this.dangerCon = lock.newCondition();
    }

    /**
     * Contrutor da Classe
     *
     * @param username  Username do Utilizador
     * @param password  Password do Utilizador
     * @param admin     Admin usado no Construtor
     * @param localx    Coordenada x do Utilizador
     * @param localy    Coordenada y do Utilizador
     * @param N         Tamanho do mapa de localizações
     * @param contacts  Lista de contactos de um Utilizador
     */
    public User(String username, String password, boolean admin, int localx, int localy, int N, List<String> contacts) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.online = false;
        this.localx = localx;
        this.localy = localy;
        this.covid = false;
        this.contacts = new TreeSet<>();
        this.contacts.addAll(contacts);
        this.locals = new boolean[N][N];
        this.locals[localx][localy] = true;
        this.lock = new ReentrantLock();
        this.contactCon = lock.newCondition();
        this.dangerCon = lock.newCondition();
    }

    /**
     * Obtém o username
     *
     * @return String que representa o username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Obtém a coordenada x do Utilizador
     *
     * @return Inteiro que representa a coordenada x do Utilizador
     */
    public int getLocalx() {
        return localx;
    }

    /**
     * Obtém a coordenada y do Utilizador
     *
     * @return Inteiro que representa a coordenada y do Utilizador
     */
    public int getLocaly() {
        return localy;
    }

    /**
     * Altera a localização do Utilizador
     *
     * @param localx    Nova localização x
     * @param localy    Nova localização y
     */
    public void setLocal(int localx, int localy) {
        this.localx = localx;
        this.localy = localy;

        this.locals[localx][localy] = true;
    }

    /**
     * Verifica se Utilizador é Admin
     *
     * @return  Booleano que representa se o Utilizador é Admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Verifica se Utilizador está Online
     *
     * @return  Booleano que representa se o Utilizador está Online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Altera o estado Online do Utilizador
     *
     * @param online    Novo estado Online
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Altera o estado de infeção do Utilizador
     *
     * @param covid Novo estado de infeção
     */
    public void setCovid(boolean covid) {
        this.covid = covid;
    }

    /**
     * Verifica se o Utilizador está infetado
     *
     * @return Estado de infeção do Utilizador
     */
    public boolean isCovid() {
        return covid;
    }

    /**
     * Valida as credenciais do Utilizador
     *
     * @param password  Password a ser validada
     * @return          Booleano que representa a validação das credenciais
     */
    public boolean validateCredentials(String password) {
        return this.password.equals(password);
    }

    /**
     * Adiciona um contacto aos contactos do Utilizador
     *
     * @param contact Contacto a ser adicionado
     */
    public void addContact(String contact) {
        contacts.add(contact);
    }

    /**
     * Obtém os contactos do Utilizador
     *
     * @return Conjunto com os contactos do Utilizador
     */
    public Set<String> getContacts() {
        return contacts;
    }

    /**
     * Verifica se o Utilizador já esteve numa dada localização
     *
     * @param localx    Coordenada x da localização pretendida
     * @param localy    Coordenada y da localização pretendida
     * @return          Booleano que indica se o Utilizador já esteve na localização dada
     */
    public boolean getLocal(int localx, int localy) {
        return locals[localx][localy];
    }

    /**
     * Obtém a condition de contacto
     *
     * @return  Condition de contacto
     */
    public Condition getContactCon() {
        return contactCon;
    }

    /**
     * Obtém o lock do Utilizador
     *
     * @return  Devolve o lock do Utilizador
     */
    public ReentrantLock getLock() {
        return lock;
    }

    /**
     * Obtém a condition de Danger
     *
     * @return  COndition de danger
     */
    public Condition getDangerCon() {
        return dangerCon;
    }
}
