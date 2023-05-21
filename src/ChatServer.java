import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    ArrayList al = new ArrayList();
    ArrayList users = new ArrayList();
    ServerSocket serverSocket;
    Socket socket;

    public final static int PORT = 10;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="@@logout@@:";

    public ChatServer() {
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started: " + serverSocket);
            while (true) {
                socket = serverSocket.accept();
                Runnable runnable = new ChatThread(socket, al, users);
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
