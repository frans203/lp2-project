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
    String statement;
    String[] options;

    public final static int PORT = 1024;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="@@logout@@:";
    public final static String UPDATE_SCORE="@@score@@:";

    private static int connectedClients = 0;
    private final int maxClients = 2;

    public ChatServer() {
        //*****PERGUNTAS DO QUIZ*****
        statement = "What is the capital of Canada?";
        options = new String[]{"Ottawa", "Sidney", "Toronto", "Vancouver"};
        questions.add(new Question(statement, options, 1));

        statement = "Who is the current Brazil's president?";
        options = new String[]{"Jair Bolsonaro", "Lula", "Dilma Rouseff", "Michel Temer"};
        questions.add(new Question(statement, options, 2));

        statement = "What is the currently most requested programming language?";
        options = new String[]{"Python", "Java", "C++", "PHP"};
        questions.add(new Question(statement, options, 2));

        statement = "Who is the most recent VCT Champions winner?";
        options = new String[]{"NRG", "Furia", "Loud", "Fnatic"};
        questions.add(new Question(statement, options, 3));

        statement = "What's the main character of the \"Spider-Man Across the Spider-Verse\" movie?";
        options = new String[]{"Peter Parker", "Miguel Ohara", "Gwen Stacy", "Miles Morales"};
        questions.add(new Question(statement, options, 4));

        statement = "Who was the winner of Grammy 2023?";
        options = new String[]{"Harry Styles", "Kendrick Lamar", "Beyoncé", "Lizzo"};
        questions.add(new Question(statement, options, 3));

        statement = "Who is the best professor in all of the CI?";
        options = new String[]{"Glêdson S2", "Maelso (??? nah bro)", "Paulo Cézar (no way)", "Liliane (nope)"};
        questions.add(new Question(statement, options, 1));

        statement = "What is the biggest country in the world?";
        options = new String[]{"USA", "Brasil", "China", "Russia"};
        questions.add(new Question(statement, options, 4));

        statement = "The song called \"Whiksy a Go-Go\" belongs to which of these brazilian bands?";
        options = new String[]{"Titãs", "Roupa Nova", "Kid Abelha", "Engenheiros Do Hawaii"};
        questions.add(new Question(statement, options, 2));

        statement = "What game won the \"Game of the year award\" in 2018?";
        options = new String[]{"Red Dead Redemption 2", "Celeste", "God of War", "Assassin's Creed Odyssey"};
        questions.add(new Question(statement, options, 3));
        //***************************

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
