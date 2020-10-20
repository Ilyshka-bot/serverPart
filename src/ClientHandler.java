import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {

    private Server server;
    private PrintWriter outMessage;
    private Scanner inMessage;
    public File filePath = null;
    private Socket clientSocket = null;
    private String clientMes = "";
    private String nameUser;
    public ArrayList<String> msgFromWhileInChat = new ArrayList<>();

    public String getNameUser() {
        return nameUser;
    }

    public String getClientMes(){
        return this.clientMes;
    }

    public PrintWriter getOutMessage(){
        return this.outMessage;
    }

    public ClientHandler(Socket socket, Server server, File file) {
        try {
            this.server = server;
            this.filePath = file;
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            while (true) {
                if (inMessage.hasNext()) {
                    clientMes = inMessage.nextLine();
                    if (clientMes.equals("OK")) {
                        System.out.println(clientMes);
                        Thread.sleep(100);
                        if (server.getOnlineUsers().size() != 0) {
                            sendMsg("usersOnline");
                            HashMap<String, String> newUser = new HashMap<>();

                            for (Map.Entry<String, String> entry : server.getUserPass().entrySet()) {
                                for (Map.Entry<String, String> entry2 : server.getOnlineUsers().entrySet()) {
                                    if (entry2.getKey().equals(entry.getKey())) {
                                        newUser.put(entry.getKey(), entry.getValue());
                                    }
                                }
                            }
                            String newUs = serialize(newUser);
                            sendMsg(newUs);
                            String users = serialize(server.getUserPass());
                            sendMsg(users);
                        } else {
                            String userPas = serialize(server.getUserPass());
                            sendMsg("First");
                            sendMsg(userPas);
                        }
                    }
                    if (clientMes.equals("Reg")) {
                        System.out.println(clientMes);
                        String userName = inMessage.nextLine();
                        String pass = inMessage.nextLine();
                        server.addelement(userName, pass);
                        server.sendMessageToAllClients("newRegInf");
                        server.sendMessageToAllClients(userName);
                    }
                    if (clientMes.equals("Auth")) {
                        System.out.println(clientMes);
                        clientMes = inMessage.nextLine();

                        for (Map.Entry<String, String> entry : server.getUserPass().entrySet()) {
                            if (clientMes.equals(entry.getKey())) {
                                clientMes = entry.getKey();
                                server.addOnlineUsers(entry.getKey(), entry.getValue());
                                this.nameUser = clientMes;
                                break;
                            }
                        }
                        System.out.println(nameUser);
                        server.sendMessageToAllClients("newAuthInf");
                        server.sendMessageToAllClients(clientMes);
                    }
                    if (clientMes.equals("Exit")) {
                        System.out.println(clientMes);
                        String userName = inMessage.nextLine();
                        server.delOnlineUsers(userName);
                        this.nameUser = "";
                        server.sendMessageToAllClients("exitInf");
                        server.sendMessageToAllClients(userName);
                    }
                    if(clientMes.equals("Poisk")){
                        System.out.println(clientMes);
                        String fileName = inMessage.nextLine();
                        String dirToSerch = filePath.getPath();
                        File fName = new File(fileName);
                        ArrayList<String> usersWhichHaveFiles = new ArrayList<>();
                        recursionSearc(dirToSerch, fName, usersWhichHaveFiles);
                        String[] usersWithParent;
                        String[] users = new String[usersWhichHaveFiles.size()];

                        for(int i = 0; i < usersWhichHaveFiles.size(); i++){
                            usersWithParent = usersWhichHaveFiles.get(i).split("\\\\");
                            users[i] = usersWithParent[1];
                        }
                        sendMsg("ResultPoisk");
                        outMessage.println(users.length);
                        outMessage.flush();
                        Thread.sleep(100);

                        for(int i = 0; i < users.length; i++){
                            System.out.println(i);
                            sendMsg(users[i]);
                        }
                    }
                    if (clientMes.equals("Publication")) {
                        System.out.println(clientMes);
                        String fileName = inMessage.nextLine();
                        int filesize = inMessage.nextInt();
                        File fileUser = new File(filePath.getPath(), nameUser);
                        if (!fileUser.exists()) {
                            fileUser.mkdir();
                        }
                        File copyFileName = new File(fileUser.getPath(), fileName);
                        copyFileName.createNewFile();

                        byte[] mybytearray = new byte[1024];
                        InputStream is = clientSocket.getInputStream();
                        FileOutputStream fos = new FileOutputStream(copyFileName);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        int bytesRead = is.read(mybytearray, 0, mybytearray.length);
                        bos.write(mybytearray, 0, bytesRead);
                        bos.close();
                        System.out.println("Peredan");
                    }
                    if(clientMes.equals("GetFilesList")){
                        String dirToSerch = filePath.getPath();
                        ArrayList<String> user = new ArrayList<>();
                        ArrayList<String> files = new ArrayList<>();
                        recursionGetFiles(dirToSerch, files, user);
                        ArrayList<String> userAndFiles = new ArrayList<>();

                        for(int i = 0; i < user.size(); i++){
                            userAndFiles.add("Username: " + user.get(i) + " - " + files.get(i));
                        }
                        sendMsg("ListFiles");
                        outMessage.println(userAndFiles.size());
                        outMessage.flush();

                        for(int i = 0 ;i < userAndFiles.size(); i++){
                            sendMsg(userAndFiles.get(i));
                        }
                    }
                    if(clientMes.equals("DownloadFile")){
                        System.out.println(clientMes);
                        String userName = inMessage.nextLine();
                        String fileName = inMessage.nextLine();
                        File dir = new File(filePath.getPath(), userName);

                        if(dir.exists()){
                            System.out.println(dir.getName() + "Est user");
                            File[] files = dir.listFiles();
                            boolean est = false;

                            for(File file : files){
                                if(file.getName().equals(fileName)){
                                    sendMsg("FileEst");
                                    sendMsg(userName);
                                    sendMsg(fileName);

                                    byte[] mybytearray = new byte[(int) file.length()];
                                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                                    bis.read(mybytearray, 0, mybytearray.length);
                                    OutputStream os = clientSocket.getOutputStream();
                                    os.write(mybytearray, 0, mybytearray.length);
                                    os.flush();
                                    est = true;
                                    break;
                                }
                            }
                            if(est == false) {
                                sendMsg("FileEst");
                                sendMsg("FailaNet");
                                sendMsg(fileName);
                            }
                        }else {
                            sendMsg("FileEst");
                            sendMsg("FailaNet");
                            sendMsg(fileName);
                        }
                    }
                    if (clientMes.equals("Chat")) {
                        System.out.println(clientMes);
                        sendMsg("ChatOpen");
                        String user = null;
                        String chatUser = null;
                        boolean close = false;

                        while (true) {
                            if (inMessage.hasNext()) {
                                user = inMessage.nextLine();
                                chatUser = server.checkUserOn(user);
                                if (!chatUser.equals("Net")) {
                                    server.sendToClient(this, chatUser,"User " + this.nameUser + " connect!");
                                    break;
                                } else if (chatUser.equals("Net") ) {
                                    sendMsg("Close");
                                    close = true;
                                    break;
                                }
                            }
                        }
                        if (close == false) {
                            while (true) {
                                if (inMessage.hasNext()) {
                                    String clientMessage = inMessage.nextLine();
                                    if (clientMessage.equals("##session##end##")) {
                                        server.sendToClient(this, chatUser, "User: " + this.nameUser + " exit!");
                                        outMessage.println("Close");
                                        outMessage.flush();
                                        for(int i = 0; i < this.msgFromWhileInChat.size(); i++){
                                            sendMsg(msgFromWhileInChat.get(i));
                                            Thread.sleep(10);
                                        }
                                        for(int i = 0; i < this.msgFromWhileInChat.size(); i++){
                                            msgFromWhileInChat.remove(i);
                                        }
                                        break;
                                    }
                                    System.out.println(clientMessage);
                                    server.sendToClient(this, chatUser, clientMessage);
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recursionSearc(String path, File fileSerch, ArrayList<String> userNames) {//поиск файла в каталоге и подкаталогах
        File file = new File(path);
        File[] s = file.listFiles();
        for (int j = 0; j < s.length; j++) {
            if(s[j].isDirectory()) {
                recursionSearc(s[j].getPath(), fileSerch, userNames);
            }
            else if(!s[j].isDirectory())
            {
                if(s[j].getName().equals(fileSerch.getName())){
                    userNames.add(s[j].getParent());
                }
            }
        }
    }

    public void recursionGetFiles(String path, ArrayList<String> FileUser, ArrayList<String> users) {//поиск файла в каталоге и подкаталогах
        File file = new File(path);
        File[] s = file.listFiles();
        String files = "";

        for (int j = 0; j < s.length; j++) {
            if(s[j].isDirectory()) {
                recursionGetFiles(s[j].getPath(), FileUser,users);
                users.add(s[j].getName());
            }
            else if(!s[j].isDirectory())
            {
                files = files + s[j].getName() + " ";
            }
        }
        FileUser.add(files);
    }
    public void sendMsg(String msg) {
        try {
            outMessage.println(msg);
            outMessage.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public String serialize(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public void sendMsgToCLient(String msg, ClientHandler client){
        client.getOutMessage().println(msg);
        client.getOutMessage().flush();
    }
}