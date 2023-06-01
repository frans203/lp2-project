import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class ChatThread implements Runnable{
    Socket socket;
    ArrayList<Socket> al;
    ArrayList<String> users;

    ArrayList<User> usersObjList;

    String username;

    ArrayList<Question> questions;
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
                tellEveryone("" + currentQuestionNumber);
                tellEveryone(username + " said: " + s1);

                //dentro de um loop, numero atual da pergunta
                //roda o loop enquanto o numero da pergunta eh <= numero total de perguntas
                //pegaria a proxima pergunta
                //tellEveryone(pergunta.getQuestion)
                //mostrar opções
                //loop entre as opções da pergunta e pra cada opção mostrar com o tellEveryone
                //currentQuestion = currentQuestion[numeroDaPergunta]
                //espera os usuários digitarem algo
                //verificação de o que o usuário digitou é igual ao numero da resposta correta + 1
                //Se isso acontecer, roda o increasePoints
                //Roda o loop de novo

                if(s1.equals(String.valueOf(questions.get(currentQuestionNumber).getIndexCorrectAnswer() + 1)) && quizStarted){
                        increasePoints();
                        currentQuestionNumber += 1;
                        questionWasShown = false;


                    //Verificar se já acabaram as perguntas
                    if (currentQuestionNumber >= questions.size()){
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

                //usuario digita /question
                if(s1.trim().toLowerCase().equals("/question")){
                    if (!quizStarted){
                        tellEveryone("Can't show question yet, quiz hasn't started!");
                    } else {
                        showQuestion();
                    }
                }

                if(s1.trim().toLowerCase().equals("/seeusers")){
                    for (int i = 0; i < this.usersObjList.size(); i++) {
                       tellEveryone(this.usersObjList.get(i).getUsername());
                    }
                }

                //verifica se o que o usuario digitou é igual à posição da resposta correta (usuários digitam de 1 a 4)
                //atualizar pergunta atual
                //verifica se a pergunta atual é igual a ultima pergunta
                //se for igual, depois de responder, mostra fim de jogo

                //Usuário digita /ready
                if(s1.trim().toLowerCase().equals("/ready") && !quizStarted){
                    // *** TALVEZ SEJA NECESSÁRIO EXCLUSÃO MÚTUA ***
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
        tellEveryone(questions.get(currentQuestionNumber).getQuestion());
        for(int i=0;i<questions.get(currentQuestionNumber).getOptions().length ; i++){
            //Usar método para receber a opção específica
            tellEveryone(questions.get(currentQuestionNumber).getOption(i));
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
