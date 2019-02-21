package Test;


import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;
import Server.Persistence.Database;
import static org.junit.Assert.*;
import Server.BusinessLogic.Buildhandler;
import Server.Exceptions.KomException;




/**
 * Created by Falko on 15.07.2016.
 */
public class BuildHandlerTest {

    @BeforeClass
    public static void prepareTest(){
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /**
     * Testet, ob Gebaeude erfolgreich erstellt werden kann.
     * TestID 16
     * @throws KomException
     */
    @Test
    public void erstellenTest() throws KomException {
        String[] expected = {"TRUE","6", "MILISECONDS"};
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko", "blub", "1", "D");
        int standortID = db.erzeugeNeuenStandort(spielerID, "Test1", 3, 4);

        String[] testString= {"BAUEN", "ERSTELLEN", standortID+"", "2","KS1",spielerID+""};
        Buildhandler buildhandler = new Buildhandler();
        String[] actual = buildhandler.handle(testString);

        assertEquals(expected[0],actual[0]);
        assertEquals(expected[1],actual[1]);
    }

    /**
     * Testet, ob Gebaeude korrekt geupdatet werden.
     * TestID 17
     */
    @Test
    public void updateTest() throws KomException{
        String[] expected = {"TRUE", "MILISECONDS", "2"};
        String[] expected2 = {"1", "2", "KS", "0", "true", "MILISECONDS",};
        Database db = Database.gibInstanz();
        int spielerID = db.erstelleBenutzer("Falko", "blub", "1", "D");
        int standortID = db.erzeugeNeuenStandort(spielerID, "Test1", 3, 4);
        String[] testString1= {"BAUEN", "ERSTELLEN", standortID+"", "2","KS1",spielerID+""};
        Buildhandler buildhandler = new Buildhandler();
        String gebaudeID = buildhandler.handle(testString1)[1];

        String[] testString2 = {"BAUEN", "UPDATE", gebaudeID, "KS2", spielerID+""};
        String[] actual = buildhandler.handle(testString2);
        String actualOutput = db.gibGebaeude(Integer.parseInt(gebaudeID));
        String[] actua2 = actualOutput.split(";");

        assertEquals(expected[0],actual[0]);
        assertEquals(expected[2],actual[2]);
        assertEquals(expected2[0],actua2[0]);
        assertEquals(expected2[1],actua2[1]);
        assertEquals(expected2[2],actua2[2]);
        assertEquals(expected2[3],actua2[3]);
    }
}
