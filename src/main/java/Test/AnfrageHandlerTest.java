package Test;

import Server.BusinessLogic.AnfrageHandler;
import Server.Persistence.Database;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Created by Falko on 18.07.2016.
 */
public class AnfrageHandlerTest {

    @BeforeClass
    public static void prepareTest(){
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /**
     * Test, ob Baumkosten richtig uebergeben
     * TestID 1
     */
    @Test
    public void infobaumTestF(){
        AnfrageHandler anfrageHandler = new AnfrageHandler();
        String[] testString = {"ANFRAGE", "INFOBAUM", "PREIS" ,"F", "A"};
        String[] expectedOutput = {"TRUE", "100"};

        assertArrayEquals(expectedOutput,anfrageHandler.handle(testString));
    }

    /**
     * Test, ob Baumkosten richtig uebergeben
     * TestID 2
     */
    @Test
    public void infobaumTestG(){
        AnfrageHandler anfrageHandler = new AnfrageHandler();
        String[] testString = {"ANFRAGE", "INFOBAUM", "PREIS" ,"G", "A"};
        String[] expectedOutput = {"TRUE", "1000"};

        assertArrayEquals(expectedOutput,anfrageHandler.handle(testString));
    }

    /**
     * Test, ob Baumkosten richtig uebergeben
     * TestID 3
     */
    @Test
    public void infobaumTestC(){
        AnfrageHandler anfrageHandler = new AnfrageHandler();
        String[] testString = {"ANFRAGE", "INFOBAUM", "PREIS" ,"C", "A"};
        String[] expectedOutput = {"TRUE", "125"};

        assertArrayEquals(expectedOutput,anfrageHandler.handle(testString));
    }

    /**
     * Test, ob NachrichtenIDs korrekt ausgegeben werden
     * TestID 4
     */
   @Test
    public void nachrichtenIDTest(){
       Database db = Database.gibInstanz();
       db.erstelleBenutzer("Falko", "blub" , "K", "D");
       db.erstelleBenutzer("AlterEgo", "blub", "K", "K");
       db.erstelleNachricht(db.gibSpielerId("Falko"), db.gibSpielerId("AlterEgo"), "Test1");
       db.erstelleNachricht(db.gibSpielerId("Falko"), db.gibSpielerId("AlterEgo"), "Test2");
       db.erstelleNachricht(db.gibSpielerId("Falko"), db.gibSpielerId("AlterEgo"), "Test3");
       db.erstelleNachricht(db.gibSpielerId("AlterEgo"), db.gibSpielerId("Falko"), "Test4");
       db.erstelleNachricht(db.gibSpielerId("AlterEgo"), db.gibSpielerId("Falko"), "Test5");
       db.erstelleNachricht(db.gibSpielerId("AlterEgo"), db.gibSpielerId("Falko"), "Test6");
       String[] expected = {"TRUE", "1;2;3", "4;5;6"};
       String[] testString = {"ANFRAGE", "NACHRICHTENIDS", db.gibSpielerId("Falko")+""};
       AnfrageHandler anfrageHandler = new AnfrageHandler();

       assertArrayEquals(expected,anfrageHandler.handle(testString));
   }

    /**
     * Test, ob StandortStrings korrekt erzeugt werden
     * TestID 5
     */
    @Test
    public void generiereKartenTest(){
        Database db = Database.gibInstanz();
        db.erzeugeNeuenStandort(123,"Test3", 2,4);
        db.erzeugeNeuenStandort(1234, "Test2", 3,5);
        db.erzeugeNeuenStandort(12345, "Test1", 5, 7);

        String[] testString = {"ANFRAGE", "KARTE", "4", "6"};
        AnfrageHandler anfrageHandler = new AnfrageHandler();
        String[] toTest = anfrageHandler.handle(testString);

        assertEquals("TRUE",toTest[0]);
        assertEquals("0;0;1;0;0;0;0;0;0", toTest[3]);
        assertEquals("0;0;0;2;0;0;0;0;0", toTest[4]);
        assertEquals("0;0;0;0;0;0;0;0;0", toTest[5]);
        assertEquals("0;0;0;0;0;3;0;0;0", toTest[6]);
    }


    /**
     * Testet, ob Truppeneigenschaften korrekt aus der Datenbank geladen werden.
     * TestID 6
     */
    @Test
    public void truppenEigenschaftenTest(){
        Database db = Database.gibInstanz();
        AnfrageHandler anfrageHandler = new AnfrageHandler();
        String[] testString = {"ANFRAGE", "TRUPPENEIGENSCHAFTEN", "K"};
        String fraktion = testString[2];
        String[] expected = {
                db.gibEinheitenEigenschaften(fraktion+"L1"),
                db.gibEinheitenEigenschaften(fraktion+"L2"),
                db.gibEinheitenEigenschaften(fraktion+"M1"),
                db.gibEinheitenEigenschaften(fraktion+"M2"),
                db.gibEinheitenEigenschaften(fraktion+"S1"),
                db.gibEinheitenEigenschaften(fraktion+"S2"),
                db.gibEinheitenEigenschaften("T1"),
                db.gibEinheitenEigenschaften("T2"),
                db.gibEinheitenEigenschaften("X1"),
                };

        assertArrayEquals(expected,anfrageHandler.handle(testString));
    }
    /*

    @Test
    public void infobaumTestF(){}

    @Test
    public void infobaumTestF(){}

    @Test
    public void infobaumTestF(){}

    @Test
    public void infobaumTestF(){}

    @Test
    public void infobaumTestF(){}

    @Test
    public void infobaumTestF(){}*/
}
