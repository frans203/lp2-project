import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    ArrayList<Socket> al = new ArrayList<Socket>();
    ArrayList<String> users = new ArrayList<String>();

    ArrayList<User> usersObjList = new ArrayList<>();
    ServerSocket serverSocket;
    Socket socket;

    ArrayList<Question> questions = new ArrayList<>();

    public final static int PORT = 1024;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="@@logout@@:";
    public final static String UPDATE_SCORE="@@score@@:";


    public ChatServer() {
        String statement;
        String[] options;
        statement = "What is the capital of Canada?";
        options = new String[]{"Ottawa", "Sidney", "Toronto", "Vancouver"};
        questions.add(new Question(statement, options, 1));

        statement = "Who is the current Brazil's president?";
        options = new String[]{"Jair Bolsonaro", "Lula", "Dilma Rouseff", "Michel Temer"};
        questions.add(new Question(statement, options, 1));

        statement = "What is the currently most requested programming language?";
        options = new String[]{"Python", "Java", "C++", "PHP"};
        questions.add(new Question(statement, options, 2));

        statement = "Who is the best professor in all of the CI?";
        options = new String[]{"Glêdson S2", "Maelso (??? nah bro)", "Paulo Cézar (no way)", "Liliane (nope)"};
        questions.add(new Question(statement, options, 0));
        statement = "What is the biggest country in the world?";
        options = new String[]{"USA", "Brasil", "China", "Russia"};
        questions.add(new Question(statement, options, 3));

        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started: " + serverSocket);
            while (true) {
                    socket = serverSocket.accept();
                    Runnable runnable = new ChatThread(socket, al, users, usersObjList, questions);
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
