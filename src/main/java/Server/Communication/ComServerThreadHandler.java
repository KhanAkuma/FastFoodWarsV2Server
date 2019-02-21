package Server.Communication;

/**
 * Autor: Christoph Wohlers
 *
 *
 */
public class ComServerThreadHandler {

    /**
     * Startet den ComServerThread
     */

    private static ComServerThreadHandler ncsth = null;

    private Thread comServerThread;

    public static ComServerThreadHandler getInstance(){
        if(ncsth == null) {
            ncsth = new ComServerThreadHandler();
        }
        return ncsth;
    }

    public void startComServerThread(int port){
        if (comServerThread == null) {
            comServerThread = new Thread(new ComServerThread(port));
        }
        if (!comServerThread.isAlive()) {
            comServerThread.start();
        }
    }

}
