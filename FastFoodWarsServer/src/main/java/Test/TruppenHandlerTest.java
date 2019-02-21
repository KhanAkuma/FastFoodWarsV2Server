package Test;

import Server.BusinessLogic.TruppenHandler;
import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Falko on 03.08.2016.
 */
public class TruppenHandlerTest {
    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    @Test
    public void testOrder(){
        ausbildungTest();
        verschiebenTest();
    }

    /**
     * Tested, ob Truppen korrekt in Ausbildung gesetzt sind.
     * TestID 44
     */
    public void ausbildungTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Flako1", "blub","K","D");
        int standOrtID = db.erzeugeNeuenStandort(spielerID1,"Test1",3,4);

        String[] testString = {"TRUPPEN", "AUSBILDUNG", standOrtID+"", "K", "T1", "5"};

        TruppenHandler truppenHandler = new TruppenHandler();
        String[] actual = truppenHandler.handle(testString);

        int anzahl = db.gibEinheitAnzahl(standOrtID,"T1");

        //assertEquals(5,anzahl);
    }


    /**
     * Testet, ob das Verschieben von Truppen zu einem anderen Standort korrekt funktioniert.
     * TestID 45
     */
    public void verschiebenTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Flako1", "blub","K","D");
        int spielerID2 = db.erstelleBenutzer("Flako2", "blub","K","D");
        int standOrtID1 = db.erzeugeNeuenStandort(spielerID1,"Test1",3,4);
        int standOrtID2 = db.erzeugeNeuenStandort(spielerID2,"Test2",4,3);
        String[] testString = {"TRUPPEN", "VERSCHIEBEN", standOrtID1+"", standOrtID2+"", "L1:2/L2:2/M1:2/M2:2/S1:5/S2:5/T1:3/T2:1/X1:0", "UEBERFALL"};

        TruppenHandler truppenHandler = new TruppenHandler();
        String[] actual = truppenHandler.handle(testString);

        assertEquals("TRUE", actual[0]);
        assertEquals("1", actual[1]);
    }

    /**
     * Testet, ob der Ueberfall eines Standortes korrekt funktioniert.
     * TestID 46
     */
    @Test
    public void ueberfallTest(){}

    /**
     * Testet, ob die Uebernahme eines Standortes korrekt funktioniert.
     * TestID 47
     */
    @Test
    public void uebernahmeTest(){}
}
