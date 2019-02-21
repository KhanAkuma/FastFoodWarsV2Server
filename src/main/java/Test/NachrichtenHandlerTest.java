package Test;


import Server.BusinessLogic.NachrichtenHandler;
import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Falko on 02.08.2016.
 */
public class NachrichtenHandlerTest {
    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /**
     * Testet, ob Nachrichten korrekt erstellt und gespeichert werden.
     * TestID 37
     */
    @Test
    public void nachrichtenTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Falko1", "blub", "1","D");
        int spielerID2 = db.erstelleBenutzer("Falko2", "blub", "1","D");
        NachrichtenHandler nachrichtenHandler = new NachrichtenHandler();
        String[] testString = {"NACHRICHT", spielerID1+"", spielerID2+"", "MuchWow,SoSkill"};

        String[] actual = nachrichtenHandler.handle(testString);
        String[] expected = {"TRUE", db.gibVersendeteNachrichten(spielerID1)};

        assertArrayEquals(expected,actual);
    }

}
