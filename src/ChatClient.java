import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ChatClient implements ActionListener {
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;

    String username;

    JButton sendButton, logoutButton, loginButton, exitButton;
    JFrame chatWindow;
    JTextArea txtBroadcast;
    JTextArea txtMessage;
    JList usersList;

    public void displayGUI(){
        chatWindow = new JFrame();
        txtBroadcast=new JTextArea(5, 30);
        txtBroadcast.setEditable(false);
        txtMessage=new JTextArea(2, 20);
        usersList = new JList();

        sendButton = new JButton("Send");
        logoutButton = new JButton("Log out");
        loginButton = new JButton("Log in");
        exitButton = new JButton("Exit");

        JPanel center1 = new JPanel();
        center1.setLayout(new BorderLayout());
        center1.add(new JLabel("Broadcast messages from all online users", JLabel.CENTER), "North");
        center1.add(new JScrollPane(txtBroadcast), "Center");

        JPanel south1 = new JPanel();
        south1.setLayout(new FlowLayout());
        south1.add(new JScrollPane(txtMessage));
        south1.add(sendButton);

        JPanel south2 = new JPanel();
        south2.setLayout(new FlowLayout());
        south2.add(loginButton);
        south2.add(logoutButton);
        south2.add(exitButton);

        JPanel south = new JPanel();
        south.setLayout(new GridLayout(2, 1));
        south.add(south1);
        south.add(south2);

        JPanel east = new JPanel();
        east.setLayout(new BorderLayout());
        east.add(new JLabel("Online Users", JLabel.CENTER), "East");
        east.add(new JScrollPane(usersList), "South");

        chatWindow.add(east, "East");
        chatWindow.add(center1, "Center");
        chatWindow.add(south, "South");

        chatWindow.pack();
        chatWindow.setTitle("Login for Chat");
        chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        chatWindow.setVisible(true);
        sendButton.addActionListener(this);
        logoutButton.addActionListener(this);
        loginButton.addActionListener(this);
        exitButton.addActionListener(this);
        logoutButton.setEnabled(false);
        loginButton.setEnabled(true);

        txtMessage.addFocusListener(new FocusAdapter()
        {public void focusGained(FocusEvent fe){txtMessage.selectAll();}});

        chatWindow.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event){
                if(socket!=null){
                    JOptionPane.showMessageDialog(chatWindow, "You are logged out right now. ", "Exit", JOptionPane.INFORMATION_MESSAGE);
                    logoutSession();
                }
                System.exit(0);
            }
        });


    }

    public void actionPerformed(ActionEvent event){
        JButton temp = (JButton) event.getSource();
        if(temp==sendButton){
            if(socket == null){
                JOptionPane.showMessageDialog(chatWindow, "Please Login first");
                return;
            }

            try{
                dos.writeUTF(txtMessage.getText());
                txtMessage.setText("");
            }catch (Exception e){
                txtBroadcast.append("\nSend button click: " + e);
            }
        }else if(temp==loginButton){
            String username = JOptionPane.showInputDialog(chatWindow, "Enter a nickname: ");
            this.username = username;
            if(username != null){
                clientChat(username);
            }
        }else if(temp==logoutButton){
            if(socket!=null){
                try{
                    dos.writeUTF(ChatServer.LOGOUT_MESSAGE);
                    System.out.println(ChatServer.LOGOUT_MESSAGE);
                }catch (Exception e){
                    System.out.println("error: " + e);
                }
                logoutSession();
            }
        }else if(temp==exitButton){
            if(socket!=null){
                JOptionPane.showMessageDialog(chatWindow, "Logged out.", "Exit", JOptionPane.INFORMATION_MESSAGE);
                logoutSession();
            }
            System.exit(0);
        }
    }

    public void clientChat(String username){
        try{
            socket=new Socket(InetAddress.getLocalHost(), ChatServer.PORT);
            dis=new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            ChatClientThread chatClientThread = new ChatClientThread(dis, this, username);
            Thread thread1 = new Thread(chatClientThread);
            thread1.start();
            dos.writeUTF(username);
            chatWindow.setTitle(username + " Chat Window");
        }catch (Exception e){
            txtBroadcast.append("\nClient Constructor error: " + e);
        }
        logoutButton.setEnabled(true);
        loginButton.setEnabled(false);
    }

    public void logoutSession(){
        if(socket==null) return;

        try{
            dos.writeUTF(ChatServer.LOGOUT_MESSAGE);
            Thread.sleep(500);
            socket = null;
        }catch(Exception e){
            txtBroadcast.append("\nError on logoutSession: " + e);
        }

        logoutButton.setEnabled(false);
        loginButton.setEnabled(true);
        chatWindow.setTitle("Login for Chat");
    }

    public ChatClient() {
        displayGUI();
    }

    public static void main(String[] args) {
        new ChatClient();
    }


}
