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

    public ChatThread(Socket socket, ArrayList<Socket> al, ArrayList<String> users, ArrayList<User> usersObjList) {
        this.socket = socket;
        this.al = al;
        this.users = users;
        this.usersObjList = usersObjList;

        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            System.out.println(dis);
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
                if(s1.toLowerCase().contains(ChatServer.LOGOUT_MESSAGE)) {
                    break;
                };
                if(s1.equals("1")){
                    increasePoints();
                }
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
