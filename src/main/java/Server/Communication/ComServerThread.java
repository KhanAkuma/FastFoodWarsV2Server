package Server.Communication;

/**
 * Created by wohle on 04.08.2016.
 *
 * Thread des ComThread der vom ComServerThreadHandler initialisiert wird
 */
public class ComServerThread extends Thread {

    /**
     * Startet den ComServer als Thread
     */

    private ComServer comServer;

    public ComServerThread(int port){
        super("ComServerThread");
        comServer = ComServer.getInstance(port);
    }



    @Override
    public void run(){
        comServer.initialize();
    }

    public ComServer getComServer() {
        return comServer;
    }

}
