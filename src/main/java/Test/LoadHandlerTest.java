package Test;


import Server.BusinessLogic.AuktionHandler;
import Server.BusinessLogic.LoadHandler;
import Server.Exceptions.KomException;

import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * Created by Falko on 15.07.2016.
 */
public class LoadHandlerTest {

    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    @Test
    public void testOrder() throws KomException{
        standortTest();
        gebaeudeTest();
        auktionTest();
        nachrichtTest();
        boniTest();
        bewegungTest();

    }

    /**
     * Testet, ob die Erzeugung des
     * Standort-Arrays korrekt funktioniert.
     * TestID 29
     */
    public void standortTest() throws KomException{
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko", "blub", "1","D");
        int standortID = db.erzeugeNeuenStandort(spielerID,"Test", 3,4);
        String[] testString = {"LADEN", "STANDORT",standortID+""};
        String[] expected = {"TRUE", "NAME", db.gibStandortName(standortID), "RESSOUCEN", "100", "100", "100", "KOORD", "3", "4",
                "HAUPTGEBAEUDE", "1", "VERTEIDIGUNG", "0", "GEBAEUDE", "1","0"};

        LoadHandler loadHandler = new LoadHandler();
        String[] actual = loadHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob die Erzeugung des Gebaude-Arrays
     * korrekt funktioniert.
     * TestID 30
     */
    public void gebaeudeTest() throws KomException{
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko", "blub", "1","D");
        int standortID = db.erzeugeNeuenStandort(spielerID,"Test", 3,4);
        int gebaeudeID = db.baueGebaeude(standortID,5,"KS", Integer.toUnsignedLong(20));
        String[] testString = {"LADEN", "GEBAEUDE", gebaeudeID+""};
        String[] expected = {"TRUE", "TYP", "KS0", "STELLPLATZ", "5", "true"};

        LoadHandler loadHandler = new LoadHandler();
        String[] actual = loadHandler.handle(testString);

        assertArrayEquals(expected,actual);
    }

    /**
     * Testet, ob das Auktion-Array
     * korrekt angelegt.
     * TestID 31
     */
    public void auktionTest() throws KomException {
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko", "blub", "1", "D");
        int standortID = db.erzeugeNeuenStandort(spielerID, "Test", 3, 4);
        int auktionsID = db.erstelleAuktion(standortID, "Mehl", 80, "Fleisch", 10);
        String[] testString = {"LADEN", "AUKTION", auktionsID+""};
        String[] expected = {"TRUE","ANBIETER", "Falko","ANGEBOTENERESSOURCE","Mehl",
                "ANGEBOTSMENGE","80","VERLANGTERESSOURCE", "Fleisch",
                "ANGEBOTSPREIS", "10" };

        LoadHandler loadHandler = new LoadHandler();
        String[] actual = loadHandler.handle(testString);
        assertArrayEquals(expected,actual);

    }

    /**
     * Testet, ob Nachrichten-Arrays
     * korrekt angelegt werden.
     * TestID 32
     */
    public void nachrichtTest() throws KomException{
        Database db = Database.gibInstanz();

        int spielerID1 = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int spielerID2 = db.erstelleBenutzer("Falko2", "blub", "1", "D");
        int nachrichtenID = db.erstelleNachricht(spielerID1, spielerID2, "Zu mir oder zu dir?");

        String[] expected = {"TRUE", "Falko1", "Falko2", "Zu mir oder zu dir?"};
        String[] testString = {"LADEN", "NACHRICHT", nachrichtenID+""};
        LoadHandler loadHandler = new LoadHandler();
        String[] actual = loadHandler.handle(testString);
        //assertArrayEquals(expected,actual);
        assertEquals(expected[0], actual[0]);
        assertEquals(expected[1], actual[1]);
        assertEquals(expected[2], actual[2]);
        assertEquals(expected[3], actual[3]);
    }

    /**
     * Testet, ob die Boniwerte korrekt aus der Datenbank geholt werden und indirekt die private Methode
     * zum parsen eines Stringarrays zum DoubleArray
     * TestID 33
     */
    public void boniTest() throws KomException{
        Database db = Database.gibInstanz();

        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        db.aktualisiereBonus(spielerID,"gemueseProduktionsbonus", 5);
        db.aktualisiereBonus(spielerID,"fleischProduktionsbonus", 0.505);
        db.aktualisiereBonus(spielerID,"mehlProduktionsbonus", 0.25);
        db.aktualisiereBonus(spielerID,"truppenAngriffsbonus", 0.35);
        int boniID = db.gibBoniId(spielerID);


        String[] expected = {"0.0", "0.0", "0.0", "0.25", "0.505", "5.0",
                "0.0", "0.0", "0.35", "0.0", "0.0", "0.0", };

        String[] testString = {"LADEN", "BONI", boniID+""};

        LoadHandler loadHandler = new LoadHandler();
        String[] actual = loadHandler.handle(testString);
        assertArrayEquals(expected,actual);

    }

    /**
     * Testet, ob Truppen-Bewegungs-Arrays korrekt
     * erstellt und abgespeichert werden.
     * TestID 34
     */
    public void bewegungTest() throws KomException{
        Database db = Database.gibInstanz();

        int spielerID = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        int standortID1 = db.erzeugeNeuenStandort(spielerID, "Test1",1,4);
        int standortID2 = db.erzeugeNeuenStandort(spielerID, "Test2",3,5);
        int bewegungsID = db.erstelleBewegung(standortID1,standortID2,"ScheibeToast",Integer.toUnsignedLong(1234),"Besuch");
        String[] testString = {"LADEN", "BEWEGUNG", bewegungsID+""};

        String[] expected = {standortID1+"", standortID2+"", "1234", "Besuch"};

        LoadHandler loadHandler = new LoadHandler();
        String[] actual = loadHandler.handle(testString);
        assertArrayEquals(expected,actual);

    }


}
