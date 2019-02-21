package Server.Communication;

import java.net.*;
import java.io.*;


/**
 * Created by Falko on 09.06.2016.
 *
 * Thread der bei Verbindung mit einem Client erstellt wird und die Kommunikation zwischen Client und Server organisiert
 */
public class ComThread extends Thread {
    private Socket socket = null;

    public ComThread(Socket socket) {
        super("ComThread");
        this.socket = socket;
    }

    /**
     * Startet den ComThread.
     */
    @Override
    public void run() {
        try {
            System.out.println("Incoming connection on local port: " + socket.getLocalPort());
            CommandInterpreter parser = new CommandInterpreter();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            String[] outputObject;
            String[] inputObject;

            while ((inputObject= (String[])ois.readObject())!=null) {

                String input = "";
                for(int i = 0; i <inputObject.length; i++ ){
                    input += inputObject[i] + ";";
                }
                System.out.println("Incoming input from client: " + input);
                outputObject=parser.interpret(inputObject);
                String output = "";
                for(int i = 0; i <outputObject.length; i++ ){
                    output += outputObject[i] + ";";
                }
                System.out.println("Sending information: " + output);
                oos.writeObject(outputObject);
            }
        } catch (Exception e){e.printStackTrace();}
    }
}

