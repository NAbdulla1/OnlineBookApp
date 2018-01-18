package components;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by nayon on 30-Nov-17.
 */
public class ServerInfo {

    private static String SERVER_IP_ADDRESS = "192.168.0.171";
    //private String SERVER_IP_ADDRESS = "127.0.0.1";
    private static int SERVER_PORT = 4588;

    public static Socket getClientSocket(){
        Socket client = null;
        while(client == null) {
            try {
                client = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
            } catch (IOException e) {
                client = null;
                e.printStackTrace();
            }
        }
        return client;
    }
}
