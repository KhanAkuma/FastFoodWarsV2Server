package Server.GUI;

import Server.Communication.ComServerThreadHandler;
import Server.Persistence.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Autor: Christoph Wohlers
 */
public class GUIHandler implements Initializable{

    /**
     * Behandelt die Aktionen in der GUI
     */

    @FXML
    Button starteServerButton;

    @FXML
    TextField serverPortFeld, kartenBreiteFeld;

    ComServerThreadHandler csth = ComServerThreadHandler.getInstance();

    /**
     * Initialisiert beim Aufruf der GUI.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        starteServerButton.setOnAction(this::starteServerAction);
        kartenBreiteFeld.setDisable(true);

    }

    /**
     * Startet den ComserverThread.
     * @param event
     */
    @FXML
    private void starteServerAction(ActionEvent event){
        starteServerButton.setDisable(true);
        serverPortFeld.setDisable(true);
        kartenBreiteFeld.setDisable(true);

        csth.startComServerThread(Integer.parseInt(serverPortFeld.getText()));

        //Database db = Database.gibInstanz();

        //db.setzeServerEinstellungen(Integer.parseInt(kartenBreiteFeld.getText()), Integer.parseInt(serverPortFeld.getText()));
    }

}
