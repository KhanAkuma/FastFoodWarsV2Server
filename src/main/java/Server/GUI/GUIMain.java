package Server.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autor: Christoph Wohlers
 */
public class GUIMain extends Application{

    /**
     * Startet die GUI.
     * @param primaryStage
     * @throws Exception
     */

    /**
     * Die GUI.fxml wird geladen und in der Stage angezeigt.
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {


        try {
            Parent rootLogin = FXMLLoader.load(getClass().getClassLoader().getResource("Ansichten/GUI.fxml"));
            Scene sceneLogin = new Scene(rootLogin);
            primaryStage.setScene(sceneLogin);
            primaryStage.setTitle("FastFoodWars");
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(1);
            });

        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

}
