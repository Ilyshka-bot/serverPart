import java.io.*;
import java.util.HashMap;

public class Register  {

    public Register(){}

    public void Serializ(HashMap <String, String> users){
        try
        {
            FileOutputStream fos = new FileOutputStream("userPass.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(users);
            oos.close();
            fos.close();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


    public HashMap Desirializ() {
        HashMap<String, String> fileUserPass = null;
        try
        {
            FileInputStream fis = new FileInputStream("userPass.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            fileUserPass = (HashMap<String, String>) ois.readObject();
            ois.close();
            fis.close();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }catch(ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
        }
        System.out.println("Deserialized HashMap");
        return fileUserPass;
    }
}
