package Client;

public class View {
    public void menu_main(int status) {
        switch (status) {
            case(1):
                System.out.println(" 1 | Login");
                System.out.println(" 2 | SignIn");
                break;
            case(2):
                System.out.println(" 1 | Verificar numero de pessoas numa localização");
                System.out.println(" 2 | Comunicar Doença");
                break;
        }
    }
}
