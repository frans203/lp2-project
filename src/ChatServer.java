import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    ArrayList al = new ArrayList();
    ArrayList users = new ArrayList();

    ArrayList<User> usersObjList = new ArrayList<>();
    ServerSocket serverSocket;
    Socket socket;

    ArrayList<Question> questions = new ArrayList<>();

    public final static int PORT = 1024;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="@@logout@@:";
    public final static String UPDATE_SCORE="@@score@@:";

    private static int connectedClients = 0;
    private final int maxClients = 2;

    public ChatServer() {
        //Guardar as perguntas aqui
        String[] stringsQuestion1 = {"item", "item2", "item3", "item4"};
        String[] stringsQuestion2 = {"item", "item2", "item3", "item4"};
        questions.add(new Question("Question 1", stringsQuestion1, 0));
        questions.add(new Question("Question 2", stringsQuestion2, 1));
        //

        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started: " + serverSocket);
            while (true) {
                if(connectedClients < maxClients){
                    socket = serverSocket.accept();
                    Runnable runnable = new ChatThread(socket, al, users, usersObjList, questions);
                    Thread thread = new Thread(runnable);
                    thread.start();
                    connectedClients += 1;
                }else{
                    System.out.println("Max client limit reached. Rejecting new connection.");
                }

            }
        }catch (Exception exception){
            System.err.println("ChatServer constructor error: " + exception);
        }

    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
