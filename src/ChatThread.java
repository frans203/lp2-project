import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ChatThread implements Runnable{
    Socket socket;
    ArrayList<Socket> al;
    ArrayList<String> users;
    ArrayList<User> usersObjList;
    String username;

    User userObj;
    ArrayList<Question> questions;
    static int currentQuestionNumber = 0;
    static boolean questionWasShown = false, quizStarted = false;


    DataOutputStream dos;

    public ChatThread(Socket socket, ArrayList<Socket> al, ArrayList<String> users,
                      ArrayList<User> usersObjList,
                      ArrayList<Question> questions
                      ) throws IOException {
        this.socket = socket;
        this.al = al;
        this.users = users;
        this.usersObjList = usersObjList;
        this.questions = questions;
        this.dos = new DataOutputStream(socket.getOutputStream());
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            username = dis.readUTF();
            al.add(socket);
            users.add(username);
            this.userObj = new User(username);
            usersObjList.add(userObj);
            tellEveryone("****** "+ username+" Logged in at "+(new Date())+" ******");
            dos.writeUTF("Welcome to the quiz game!\nUse /ready to get ready for the quiz");
            dos.writeUTF("(use /commands to see a list of all the commands)");
            dos.flush();
            sendNewUsersList();
        }catch(Exception e){
            System.err.println("ChatThread Constructor error: " + e);
        }
    }


    public void run(){
        String s1;
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            do{
                if (quizStarted){
                    if (!questionWasShown){
                        questionWasShown = true;
                            Thread.sleep(3000);
                            showQuestion();
                    }
                }


                s1 = dis.readUTF();

                if(s1.toLowerCase().contains(ChatServer.LOGOUT_MESSAGE)) {
                    break;
                }
                tellEveryone(username + " said: " + s1);

                if(s1.equals(String.valueOf(questions.get(currentQuestionNumber).getIndexCorrectAnswer())) && quizStarted){
                        increasePoints();
                        tellEveryone(username + " has Scored!");
                        currentQuestionNumber += 1;
                        questionWasShown = false;
                    if (currentQuestionNumber >= questions.size()){
                        User winner = usersObjList.get(0);
                        boolean isDraw = false;
                        for (User value : usersObjList) {
                            if (winner.getPoints() == value.getPoints() && !winner.getUsername().equals(value.getUsername())) {
                                isDraw = true;
                                break;
                            }
                        }
                        if(isDraw){
                            tellEveryone("Is a Draw!");
                        }else {
                            tellEveryone(winner.getUsername() + " is the winner!");
                        }
                        quizStarted = false;
                        currentQuestionNumber = 0;
                        for (User user : usersObjList) {
                            user.restartPoints();
                            user.setReady(false);
                        }
                        Thread.sleep(1000);
                        tellEveryone("Use /ready to play again");
                    }

                }

                if(s1.trim().equalsIgnoreCase("/question") && userObj.getReady()){
                    if (!quizStarted){
                        tellEveryone("Can't show question yet, quiz hasn't started!");
                    } else {
                        showQuestion();
                    }
                }else if(s1.trim().equalsIgnoreCase("/users")){
                    showScore();
                }else if(s1.trim().equalsIgnoreCase("/commands")) {
                    dos.writeUTF("/users (see all users that are playing)");
                    dos.writeUTF("/question (show the question for the current playing user)");
                    dos.writeUTF("/ready (Ready the user to the game)");
                }


                if(s1.trim().equalsIgnoreCase("/ready") && !quizStarted){
                    if (usersObjList.size() == 1){
                        dos.writeUTF("Quiz can't start with only one player!\nWait for other players to enter and then use /ready again");
                    } else {
                        for (User value : this.usersObjList) {
                            if (Objects.equals(value.getUsername(), username) && Objects.equals(value.getId(), userObj.getId())) {
                                value.setReady(true);
                                tellEveryone(username + " is ready!");
                            }
                        }
                        if (!quizStarted) {
                            int readyUsers = 0;
                            for (User user : this.usersObjList) {
                                if (user.getReady()) {
                                    readyUsers += 1;
                                }
                            }
                            if (readyUsers == usersObjList.size()) {
                                tellEveryone("Starting the quiz...\n");
                                quizStarted = true;
                                questionWasShown = false;
                            }
                        }
                    }

                }else if(s1.trim().equalsIgnoreCase("/ready") && quizStarted){
                    dos.writeUTF("Quiz already has Started, wait for the game to end");
                }



            }while(true);
            dos.writeUTF(ChatServer.LOGOUT_MESSAGE);
            dos.flush();
            users.remove(username);
            usersObjList.removeIf(item -> Objects.equals(item.getUsername(), username));
            tellEveryone("****** "+username+" Logged out at "+(new Date())+" ******");
            sendNewUsersList();
            al.remove(socket);
            socket.close();
            if(usersObjList.size() == 0){
                quizStarted = false;
                currentQuestionNumber = 0;
            }
        }catch (Exception e){
            System.err.println("ChatThread run: " + e);
        }
    }

    public void sendNewUsersList() {
        tellEveryone(ChatServer.UPDATE_USERS+users.toString());
    }



    public void tellEveryone(String s1) {
        Iterator<Socket> iterator = al.iterator();
        while(iterator.hasNext()){
            try{
                Socket socket= (Socket)iterator.next();
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(s1);
                dos.flush();
            }catch(Exception e) {
                System.err.println("tellEveryone: " + e);
            }

        }
    }

    public void showQuestion() {
        tellEveryone("Question " + (currentQuestionNumber + 1) + ":");
        tellEveryone(questions.get(currentQuestionNumber).getQuestion());
        for(int i=0;i<questions.get(currentQuestionNumber).getOptions().length ; i++){
            tellEveryone((i + 1) + " - " + questions.get(currentQuestionNumber).getOption(i));
        }
    }

    public void increasePoints() {
        for (User user : this.usersObjList) {
            if (user.getUsername().equals(username) && (user.getId() == userObj.getId())) {
                user.setPoints();
            }
        }

        showScore();

    }

    public void showScore(){
        usersObjList.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.getPoints() != o2.getPoints()) {
                    return o2.getPoints() - o1.getPoints();
                }
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });
        tellEveryone("\nSCOREBOARD");
        for (User user : this.usersObjList) {
            if(user.getReady()){
                tellEveryone("Username: " + user.getUsername() + ", points: " + user.getPoints() + "(id: " + user.getId() + ")");
            }
        }

    }
}
