package Test;

import java.io.*;
import java.net.Socket;

/**
 * Created by Falko on 22.07.2016.
 */
public class TestClient {

    private int port;
    private String host;

    private Socket socket;

    private String[] testInput = {"BAUEN", "123", "321", "4", "Mehl"};



    public void  initializeTestClient() throws Exception{

        host = "localhost";
        port = 4444;
        String[] fromServer;
        String[] serverantwort;
        socket = new Socket(host, port);

        try (
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {
            oos.writeObject(testInput);

            while ((fromServer = (String[]) ois.readObject()) != null) {

                System.out.println("Server: " + fromServer[0]);

                serverantwort = fromServer;
                oos.writeObject(testInput);

            }

        }
    catch (Exception e) {
        e.printStackTrace();
    }

    }


}





