package Server.Communication;

import Server.BusinessLogic.BewegenThread;
import Server.BusinessLogic.BoosterThread;
import Server.BusinessLogic.ErstellenThread;
import Server.Persistence.Database;

import java.io.*;
import java.net.*;

/**
 * Autor: Falko Schroeder, Jakob Grosse Boeckmann, Christoph Wohlers
 *
 * Singleton Klasse des Servers, der Port wird per Gui gesetzt
 */
public class ComServer {

    /**
     * Der ComServer ist ein Singleton, da er nur einmal ausgefuehrt werden soll.
     * Er kann entweder mit Standard-Port oder mit uebergebenen Port aufgerufen werden.
     */

    private static ComServer cs = null;

    private int port = 4444;

    private ComServer() {

    }

    private ComServer(int port) {
        this.port = port;
    }

    public static ComServer getInstance(){
        if(cs == null) {
            cs = new ComServer();
        }
        return cs;
    }

    public static ComServer getInstance(int port){
        if(cs == null) {
            cs = new ComServer(port);
        }
        return cs;
    }

    /**
     * Initialisiert den Server, der dann auf eingaben der Clienten horcht und dann startet ComServerThreads startet.
     * Startet den erstellenThread, bewegenThread und boosterThread
     */
    public void initialize() {

        Database db = Database.gibInstanz();
        //db.loescheTabellen();
        db.initialisiere();
        int portNumber = port;
        //int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;
        ErstellenThread erstellenThread = new ErstellenThread(true);
        erstellenThread.start();

        BewegenThread bewegenThread = new BewegenThread(true);
        bewegenThread.start();

        BoosterThread boosterThread = new BoosterThread(true);
        boosterThread.start();

        System.out.println("Server started and listening for connections on Port: "+ portNumber);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new ComThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            erstellenThread.setSollLaufen(false);
            System.exit(-1);
        }
    }
}
