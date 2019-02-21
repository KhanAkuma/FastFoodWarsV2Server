package Test;

import Server.BusinessLogic.FranchiseHandler;
import Server.Persistence.Database;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Falko on 02.08.2016.
 */
public class FranchiseHandlerTest {
    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /*@Before
    public void prepare(){
        Database db = Database.gibInstanz();
        db.loescheTabellen();
    }*/

    @Test
    public void testOrder(){
        erstellenTest();
        beitretenTest();
        bekommeInfosTest();
        bekommeNamenTest();
        einzahlenTest();
        bezahlenTest();
        ankuendigungTest();
        bekommeAnkuendigungTest();
    }


    /**
     * Testet, ob Franchise korrekt erstellt wird.
     * TestID 19
     */
    public void erstellenTest(){
        Database db = Database.gibInstanz();

        int spielerID = db.erstelleBenutzer("Falko", "blub", "1", "D");

        String[] expected = {"TRUE", "1"};
        String[] testString = {"FRANCHISE", "ERSTELLEN", spielerID+"", "FORTHEHORDE"};

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob Beitritt zu Franchise korrekt.
     * TestID 20
     */
    public void beitretenTest(){
        String expected1 = "3;4";
        String[] expected2 = {"TRUE", "2"};
        Database db = Database.gibInstanz();

        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int spielerID2 = db.erstelleBenutzer("Falko2", "blub", "1", "D");
        int spielerID3 = db.erstelleBenutzer("Falko3", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORTHEALLIANCE");

        String[] testString1 = {"FRANCHISE", "BEITRETEN", spielerID2+"", "FORTHEALLIANCE"};
        String[] testString2 = {"FRANCHISE", "BEITRETEN", spielerID3+"", "FORTHEALLIANCE"};

        FranchiseHandler franchiseHandler = new FranchiseHandler();

        assertArrayEquals(expected2,franchiseHandler.handle(testString1));
        assertArrayEquals(expected2,franchiseHandler.handle(testString2));
        assertEquals(expected1,db.gibFranchiseMitglieder(franchiseID));
    }

    /**
     * Testet, ob FranchiseInfos korrekt gegeben werden.
     * TestID 21
     */

    public void bekommeInfosTest(){
        Database db = Database.gibInstanz();

        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int spielerID2 = db.erstelleBenutzer("Falko2", "blub", "1", "D");
        int spielerID3 = db.erstelleBenutzer("Falko3", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORGREYSKULL");

        String[] expected = {"FORGREYSKULL", "Falko1", "0", "A0/B0/C0/D0/E0/F0/G0/H0/I0/J0/K0/L0/M0/N0/O0/P0/Q0/R0/S0", "6;7"};

        db.hinzufuegeSpielerZuFranchise(spielerID2,franchiseID);
        db.hinzufuegeSpielerZuFranchise(spielerID3,franchiseID);

        String[] testString = {"FRANCHISE", "BEKOMMEINFOS", franchiseID+""};

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob FranchiseMitlgieder korrekt zurueckgegeben werden.
     * TestID 22
     */

    public void bekommeNamenTest(){
        Database db = Database.gibInstanz();

        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int spielerID2 = db.erstelleBenutzer("Falko2", "blub", "1", "D");
        int spielerID3 = db.erstelleBenutzer("Falko3", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORROHAN");

        // Der CEO ist nicht vorhanden, weil dieser gesondert in der erstelleFranchise-Methode im FranchiseHandler hinzugefuegt wird.
        // Dieses Verhalten ist gewuenscht.
        String[] expected = {"Falko2", "Falko3"};

        db.hinzufuegeSpielerZuFranchise(spielerID2,franchiseID);
        db.hinzufuegeSpielerZuFranchise(spielerID3,franchiseID);

        String[] testString = {"FRANCHISE", "BEKOMMENAMEN", franchiseID+""};

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob Franchisekasse korrekt akutalisiert wird.
     * TestID 23
     */

    public void einzahlenTest(){
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORGONDOR");

        String[] testString = {"FRANCHISE", "EINZAHLEN", franchiseID+"", spielerID+"","750"};
        String[] expected = {"TRUE", "750", "250"};

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob Bezahlen im Franchise korrekte Ergebnisse liefert.
     * TestID 24
     */

    public void bezahlenTest(){
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORMORDOR");
        db.aktualisiereFranchiseGeld(franchiseID,5000);

        String[] testString = {"FRANCHISE", "BEZAHLEN", franchiseID+"","2500"};
        String[] expected = {"TRUE", "2500"};

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob FranchiseNachrichten korrekt erstellt werden.
     * TestID 25
     */

    public void ankuendigungTest(){
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORTHEWHITEHAND");

        String[] testString = {"FRANCHISE", "ANKUENDIGUNG", franchiseID+"", "DreckigeHobbits", "Garstige Kartoffel"};
        String[] expected = {"TRUE"};

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob FranchiseNachrichten korrekt zurueckgegeben werden.
     * TestID 26
     */
    public void bekommeAnkuendigungTest(){
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int franchiseID = db.erstelleFranchise(spielerID,"FORTHESHIRE");

        String[] testString = {"FRANCHISE", "BEKOMMEANKUENDIGUNG", franchiseID+""};
        String[] expected = {"DreckigeHobbits", "Garstige Kartoffel"};
        db.erstelleFranchiseNachricht(franchiseID, "DreckigeHobbits");
        db.erstelleFranchiseNachricht(franchiseID, "Garstige Kartoffel");

        FranchiseHandler franchiseHandler = new FranchiseHandler();
        String[] actual = franchiseHandler.handle(testString);

        assertEquals(expected[0],actual[0]);
        assertEquals(expected[1],actual[1]);
    }
}
