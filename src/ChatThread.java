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

    ArrayList<Question> questions;
    int currentQuestionNumber;

    public ChatThread(Socket socket, ArrayList<Socket> al, ArrayList<String> users,
                      ArrayList<User> usersObjList,
                      ArrayList<Question> questions,
                      int currentQuestionNumber) {
        this.socket = socket;
        this.al = al;
        this.users = users;
        this.usersObjList = usersObjList;
        this.questions = questions;
        this.currentQuestionNumber = currentQuestionNumber;

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
                s1 = dis.readUTF();

                String correctAnswer =  String.valueOf(questions.get(currentQuestionNumber).getIndexCorrectAnswer() + 1);

                if(s1.toLowerCase().contains(ChatServer.LOGOUT_MESSAGE)) {
                    break;
                };
                if(s1.equals("1")){
                    increasePoints();
                }

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



                //usuario digita /mostrarPergunta
                if(s1.trim().toLowerCase().equals("/mostrarPergunta")){
                    tellEveryone(questions.get(currentQuestionNumber).getQuestion());
                    for(int i=0;i<questions.get(currentQuestionNumber).getOptions().length ; i++){
                        tellEveryone(questions.get(currentQuestionNumber).getOptions()[currentQuestionNumber]);
                    }
                }
                //mostra a pergunta atual com as opções usando o tellEveryone
                //verifica se o que o usuario digitou é igual à posição da resposta correta (usuários digitam de 1 a 4)
                //atualizar pergunta atual
                //verifica se a pergunta atual é igual a ultima pergunta
                //se for igual, depois de responder, mostra fim de jogo


                if(s1.equals(correctAnswer)){
                    increasePoints();
                    currentQuestionNumber++;
                }
                //

                tellEveryone(username+" said: " + s1);
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

    }
}
