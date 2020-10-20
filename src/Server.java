import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server extends SocketAddress {
    final int PORT = 3443;
    Server server = null;

    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    private HashMap<String, String> userPass =  new HashMap<>();
    private HashMap<String, String> onlineUsers =  new HashMap<>();
    public Register reg = new Register();

    public HashMap<String, String> getUserPass() {
        return userPass;
    }

    public HashMap<String, String> getOnlineUsers() {
        return onlineUsers;
    }

    public void addOnlineUsers(String userName, String pass) {
        this.onlineUsers.put(userName, pass);
    }

    public void delOnlineUsers(String userName) {

        for(Map.Entry<String, String> entry : onlineUsers.entrySet()){
            if(entry.getKey().equals(userName)){
                this.onlineUsers.remove(entry.getKey());
                break;
            }
        }
    }

    public Server(String useless){
        server = new Server();
    }

    public Server() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                    serverFrame();
            }
        }).start();

        File filePath = new File("FilesFromUsers");//создание папки для файлов пользователей
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");
            File file = new File("userPass.ser");//проверка на существование файла юзеров и паролей
            if (file.exists()) {
                userPass = reg.Desirializ();
            }

            while (true) {
                //if(close == true) break;
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this, filePath);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Сервер остановлен");
        }
    }


        public void addelement(String userName, String pass){
            this.userPass.put(userName, pass);
            reg.Serializ(this.userPass);
        }

        public String checkUserOn(String user){
            for(Map.Entry<String, String> entry : onlineUsers.entrySet()){
                if(entry.getKey().equals(user)){
                    System.out.println(user + "<-OK");
                    return entry.getKey();
                }
            }
            return "Net";
        }

        public ClientHandler clientHandl(String userName){
            for(int i = 0; i < clients.size(); i++){
                if(clients.get(i).getNameUser().equals(userName)){
                    return clients.get(i);
                }
            }
            return null;
        }

    public void sendToClient(ClientHandler me, String userName, String msg) {//чат без сохранения

        String okUser = checkUserOn(userName);
        ClientHandler client = clientHandl(okUser);
            if (!okUser.equals("Net")) {
                me.sendMsg(msg);
                client.sendMsg(msg);
            } else
                me.sendMsg("Net\n");
    }

        public void sendMessageToAllClients(String msg) {
            for (ClientHandler o : clients) {
                if(o.getClientMes().equals("Chat")) {
                    o.msgFromWhileInChat.add(msg);
                    continue;
                }
                o.sendMsg(msg);
            }
        }

        public void serverFrame(){
            JFrame servFr = new JFrame("Server");
            servFr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            servFr.setBounds(400, 300, 100,100);

            JPanel btnPannel = new JPanel();

            JButton openBtn = new JButton("Run server");
            openBtn.setEnabled(false);

            JButton closeBtn = new JButton("Close server");
            closeBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    servFr.setVisible(false);
                    System.exit(0);
                }
            });

            btnPannel.add(openBtn);
            btnPannel.add(closeBtn);

            servFr.add(btnPannel, BorderLayout.CENTER);

            servFr.pack();
            servFr.setResizable(false);
            servFr.setVisible(true);
        }
    }
