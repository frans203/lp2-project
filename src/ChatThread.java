import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.Random;

public class ChatThread implements Runnable{
    Socket socket;
    ArrayList<Socket> al;
    ArrayList<String> users;

    ArrayList<User> usersObjList;

    String username;

    ArrayList<Question> questions, chosenQuestions;
    static int currentQuestionNumber = 0;

    final Object lock;

    static boolean questionWasShown = false, quizStarted = false;
    public ChatThread(Socket socket, ArrayList<Socket> al, ArrayList<String> users,
                      ArrayList<User> usersObjList,
                      ArrayList<Question> questions
                      ) {
        this.socket = socket;
        this.al = al;
        this.users = users;
        this.usersObjList = usersObjList;
        this.questions = questions;
        lock = new Object();
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            username = dis.readUTF();
            al.add(socket);
            users.add(username);
            usersObjList.add(new User(username));
            tellEveryone("****** "+ username+" Logged in at "+(new Date())+" ******");
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
                //Começar o quiz
                if (quizStarted){
                    //Mostrar a pergunta atual uma única vez (ainda não foi exibida)
                    if (!questionWasShown){
                        questionWasShown = true;
                        //Esperar 3s até mostrar a questão
                            Thread.sleep(3000);
                            showQuestion();
                            //Define que a pergunta já foi exibida
                    }
                }


                s1 = dis.readUTF();

                if(s1.toLowerCase().contains(ChatServer.LOGOUT_MESSAGE)) {
                    break;
                }
                //tellEveryone("" + currentQuestionNumber);
                tellEveryone(username + " said: " + s1);

                if(s1.equals(String.valueOf(chosenQuestions.get(currentQuestionNumber).getIndexCorrectAnswer())) && quizStarted){
                        increasePoints();
                        currentQuestionNumber += 1;
                        questionWasShown = false;


                    //Verificar se já acabaram as perguntas
                    if (currentQuestionNumber >= chosenQuestions.size()){
                        //Game over
                        //Pegar o primeiro da lista de usuários e exibir o ganhador
                        User winner = usersObjList.get(0);
                        boolean isDraw = false;
                        for(int i=0;i<usersObjList.size();i++){
                            if(winner.getPoints() == usersObjList.get(i).getPoints() && !winner.getUsername().equals(usersObjList.get(i).getUsername())){
                                isDraw=true;
                            }
                        }
                        if(isDraw){
                            tellEveryone("Is a Draw!");
                        }else {
                            tellEveryone(winner.getUsername() + " is the winner!");
                        }
                        //Terminar o quiz
                        quizStarted = false;
                        currentQuestionNumber = 0;
                    }

                }

                //Usuário digita /question
                if(s1.trim().toLowerCase().equals("/question")){
                    if (!quizStarted){
                        tellEveryone("Can't show question yet, quiz hasn't started!");
                    } else {
                        showQuestion();
                    }
                }

                //Usuário digita /seeusers
                if(s1.trim().toLowerCase().equals("/seeusers")){
                    for (int i = 0; i < this.usersObjList.size(); i++) {
                       tellEveryone(this.usersObjList.get(i).getUsername());
                    }
                }

                //Usuário digita /ready
                if(s1.trim().toLowerCase().equals("/ready") && !quizStarted){
                    //Verifica se há apenas um jogador
                    if (usersObjList.size() == 1){
                        tellEveryone("Quiz can't start with only one player!");
                    } else {
                        //Varre a lista de usuários
                        for (int i = 0; i < this.usersObjList.size(); i++) {
                            if (this.usersObjList.get(i).getUsername() == username) {
                                //Define que está pronto
                                this.usersObjList.get(i).setReady(true);
                                tellEveryone(username + " is ready!");
                            }
                        }
                        //Checar se todos os usuários estão prontos
                        if (!quizStarted) {
                            int readyUsers = 0;
                            for (int i = 0; i < this.usersObjList.size(); i++) {
                                //Conta os usuários prontos
                                if (this.usersObjList.get(i).getReady()) {
                                    readyUsers += 1;
                                }
                            }
                            //Checar o número de prontos
                            if (readyUsers == usersObjList.size()) {
                                tellEveryone("Starting the quiz...\n");
                                //Definir o array de questões aleatórias
                                chooseQuestions();
                                quizStarted = true;
                            }
                        }
                    }
                }

            }while(true);




            DataOutputStream threadDos = new DataOutputStream(socket.getOutputStream());
            threadDos.writeUTF(ChatServer.LOGOUT_MESSAGE);
            threadDos.flush();
            users.remove(username);
            usersObjList.removeIf(item -> item.getUsername() == username);
            tellEveryone("****** "+username+" Logged out at "+(new Date())+" ******");
            sendNewUsersList();
            al.remove(socket);
            socket.close();
        }catch (Exception e){
            System.err.println("ChatThread run: " + e);
        }
    }

    public void sendNewUsersList() {
        tellEveryone(ChatServer.UPDATE_USERS+users.toString());
    }

    public void chooseQuestions(){
        chosenQuestions = new ArrayList<>();
        Random random = new Random();
        int pick, count = 0;
        int[] pastPicks = new int[5];
        boolean found = false;
        Question question;

        //Escolher todas as questões do quiz
        while (chosenQuestions.size() < 3){
            //Escolher aleatoriamente uma pergunta do array de questões
            do {
                pick = random.nextInt(questions.size());
                found = false;
                //Verificar se essa pergunta já foi escolhida
                for (int i = 0; i < pastPicks.length; i++) {
                    if (pastPicks[i] == pick){
                        found = true;
                        break;
                    }
                }
            } while (found); //Escolher novamente até que essa pergunta não tenha sido escolhida
            pastPicks[count] = pick;
            count++;
            question = questions.get(pick);
            chosenQuestions.add(question);
        }
    }

    public void tellEveryone(String s1) {
        Iterator iterator = al.iterator();
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
        //Número da pergunta
        tellEveryone("Question " + (currentQuestionNumber + 1) + ":");
        tellEveryone(chosenQuestions.get(currentQuestionNumber).getQuestion());
        for(int i=0;i<chosenQuestions.get(currentQuestionNumber).getOptions().length ; i++){
            //Usar método para receber a opção específica
            tellEveryone((i+1) + " - " + chosenQuestions.get(currentQuestionNumber).getOption(i));
        }
    }

    public void increasePoints() {
        for(int i=0;i<this.usersObjList.size() ; i++){
            if(this.usersObjList.get(i).getUsername().equals(username)){
                this.usersObjList.get(i).setPoints();
            }
        }

        Collections.sort(usersObjList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if(o1.getPoints() != o2.getPoints()){
                    return  o2.getPoints() - o1.getPoints();
                }
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });

        for(int i=0;i<this.usersObjList.size() ; i++){
           tellEveryone("Username: " + this.usersObjList.get(i).getUsername() + ", points: " + this.usersObjList.get(i).getPoints());
        }

        //Pular uma linha
        tellEveryone("");

    }
}
