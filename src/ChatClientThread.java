import java.io.DataInputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class ChatClientThread implements Runnable{
    DataInputStream dis;
    ChatClient chatClient;

    String username;

    public ChatClientThread(DataInputStream dis, ChatClient chatClient, String username) {
        this.dis = dis;
        this.chatClient = chatClient;
        this.username = username;
    }

    public void run(){
        String s2 = "";
        do{
            try{
                s2=dis.readUTF();
                if(s2.startsWith(ChatServer.UPDATE_USERS)){
                    updateUsersList(s2);
                }
                else if (s2.equals(ChatServer.LOGOUT_MESSAGE)){
                    break;
                }
                else{
                    chatClient.txtBroadcast.append("\n"+s2);
                }
                int lineOffset = chatClient.txtBroadcast.getLineStartOffset(chatClient.txtBroadcast.getLineCount() - 1);
                chatClient.txtBroadcast.setCaretPosition(lineOffset);

            }catch (Exception e){
                chatClient.txtBroadcast.append("\n ClientThread run error: "+ e);
            }

        }while(true);
    }

    public void updateUsersList(String ul) {
        Vector ulist = new Vector();
        ul=ul.replace("[", "");
        ul=ul.replace("]", "");
        ul = ul.replace(ChatServer.UPDATE_USERS, "");
        StringTokenizer stringTokenizer = new StringTokenizer(ul, ",");

        while (stringTokenizer.hasMoreTokens()){
            String temp = stringTokenizer.nextToken();
            ulist.add(temp);
        }
        chatClient.usersList.setListData(ulist);
    }



}
