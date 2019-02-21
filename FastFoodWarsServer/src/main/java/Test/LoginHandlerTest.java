package Test;


import Server.BusinessLogic.LoginHandler;
import Server.Exceptions.KomException;

import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Falko on 15.07.2016.
 */
public class LoginHandlerTest {

    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    @Test
    public void testOrder() throws KomException{
        loginTestSucc();
        loginTestFail();
    }


    /**
     * Testet, ob Login bei korrekter Eingbe, korrekte Werte liefert.
     * TestID 35
     */
    public void loginTestSucc() throws KomException{
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Falko1", "blub", "1","D");
        int spielerID2 = db.erstelleBenutzer("Falko2", "blub", "1","D");
        int standortID = db.erzeugeNeuenStandort(spielerID1,"Test", 3,4);
        int franchiseID = db.erstelleFranchise(spielerID1, "FORDOOMHAMMER");
        db.aktualisiereFranchise(spielerID1,franchiseID);
        int versendeteNachrichtID = db.erstelleNachricht(spielerID1,spielerID2, "Run to the hills");
        int erhalteneNachrichtID = db.erstelleNachricht(spielerID2,spielerID1, "Run for your life");
        boolean darfForschen = db.setzeDarfForschen(spielerID1);
        int auktionsID1 = db.erstelleAuktion(standortID, "Mehl", 80, "Fleisch", 10);
        int auktionsID2 = db.erstelleAuktion(standortID, "Gemuese", 20, "Gemuese", 20);

        String[] expected = {"TRUE", "SPIELER", "1", "Falko1", "BOM",
                "FRAKTION", "1", "BOM", "FRANCHISE", "1", "GELD",
                "1000", "GUTSCHEINE", "10", "STANDORTE", "1", "CHARAKTER", "D", "BONI", "1", "VNACHRICHTEN", "1",
                "ENACHRICHTEN", "2", "AUKTIONEN", "1;2", "MILISECONDS", "true"};


        String[] testString = {"EINLOGGEN", "Falko1", "blub"};
        LoginHandler loginHandler = new LoginHandler();
        String[] actual = loginHandler.handle(testString);

        assertEquals(expected[0],actual[0]);
        assertEquals(expected[2],actual[2]);
        assertEquals(expected[3],actual[3]);
        assertEquals(expected[6],actual[6]);
        assertEquals(expected[9],actual[9]);
        assertEquals(expected[11],actual[11]);
        assertEquals(expected[13],actual[13]);
        assertEquals(expected[15],actual[15]);
        assertEquals(expected[17],actual[17]);
        assertEquals(expected[19],actual[19]);
        assertEquals(expected[21],actual[21]);
        assertEquals(expected[23],actual[23]);
        assertEquals(expected[25],actual[25]);
        assertEquals(expected[27],actual[27]);
    }

    /**
     * Testet, ob Login bei falscher Eingabe, korrekt arbeitet.
     * TestID 36
     */
    public void loginTestFail() throws  KomException{
        LoginHandler loginHandler = new LoginHandler();
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Falko3", "blub", "1","D");
        String[] testString = {"EINLOGGEN", "Falko3", "bla"};
        String[] expected = {"FALSE", "Benutzername oder Passwort falsch."};
        String[] actual = loginHandler.handle(testString);
        assertArrayEquals(expected,actual);
    }



}
