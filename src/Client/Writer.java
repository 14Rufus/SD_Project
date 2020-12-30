package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Writer implements Runnable {
    private int status;
    private Socket socket;
    private Log log;
    private PrintWriter out;
    private View v;

    public Writer(Socket socket, Log l) throws IOException {
        this.status = 1;
        this.socket = socket;
        this.log = l;
        this.out = new PrintWriter((socket.getOutputStream()));
        this.v = new View();
    }

    private int lerInt(int min,int max){
        Scanner s = new Scanner(System.in);
        int n = -1;

        do{
            System.out.print("Escolha uma opção: ");
            try {
                String line = s.nextLine();
                n = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                n = -1;
            }
        } while (n < min || n > max);

        return n;
    }

    public String lerString(String message) {
        Scanner s = new Scanner(System.in);
        String line;

        do{
            System.out.print(message);
            line = s.nextLine();
        } while (line == null);

        return line;
    }

    private void reader_menu() {
        switch (this.status) {
            case(1):
                reader_menu_1();
                break;
            case(2):
                reader_menu_2();
                break;
        }
    }

    private void reader_menu_1() {
        int option = lerInt(1, 2);

        switch (option) {
            case 1:
                String user = lerString("Introduza o username: ");
                String pass = lerString("Introduza a password: ");
                respond_server("1:" + user + ":" + pass);
                break;
            case 2:
                respond_server("2");
                break;
        }
    }

    private void reader_menu_2() {
        int option = lerInt(1, 10);
    }

    private void respond_server(String response) {
        out.println(response);
        out.flush();
    }

    public void run() {
        while(true) {
            v.menu_main(this.status);
            reader_menu();

            if(log.isLogin())
                this.status = 2;
        }
    }
}
