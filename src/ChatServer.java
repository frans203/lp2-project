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
    int currentQuestionNumber = 0;
    //pergunta atual
    //numero da pergunta

    public final static int PORT = 1024;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="@@logout@@:";
    public final static String UPDATE_SCORE="@@score@@:";


    public ChatServer() {
        String[] stringsQuestion1 = {"item", "item2", "item3", "item4"};
        questions.add(new Question("Question 1", stringsQuestion1));
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started: " + serverSocket);
            while (true) {
                socket = serverSocket.accept();
                Runnable runnable = new ChatThread(socket, al, users, usersObjList, questions, currentQuestionNumber);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        }catch (Exception exception){
            System.err.println("ChatServer constructor error: " + exception);
        }

    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
