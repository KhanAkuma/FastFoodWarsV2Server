package Server.GUI;

/**
 * Autor: Christoph Wohlers
 */
public class Main {

    /**
     * Fuehrt die GUIMain Klasse aus.
     * @param args
     */

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                javafx.application.Application.launch(GUIMain.class);
            }
        }.start();

    }

}
