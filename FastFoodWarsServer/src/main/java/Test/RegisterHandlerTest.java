package Test;

import Server.BusinessLogic.RegisterHandler;
import Server.Persistence.Database;
import Server.Exceptions.KomException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Falko on 15.07.2016.
 */
public class RegisterHandlerTest {

    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }


    /**
     * Testet, ob bei korrekter Eingabe, Registrierung korrekte Werte liefert.
     * TestID 38
     */
    @Test
    public void registerTestTrue() throws KomException {
        RegisterHandler testHandler = new RegisterHandler();
        String[] testString = {"REGISTRIEREN", "Falko", "blub" ,"1", "D"};

        Database db = Database.gibInstanz();

        assertSame("TRUE",testHandler.handle(testString)[0]);

        int spielerID = db.gibSpielerId("Falko");
        int standortID = Integer.parseInt(db.gibStandortIds(spielerID));
        int spielerGeld = db.gibGeld(spielerID);
        String fraktion = db.gibFraktion(spielerID);
        String charakter = db.gibCharakter(spielerID);

        assertEquals("1",fraktion);
        assertEquals("D",charakter);
        assertEquals("1000",spielerGeld+"");
        assertEquals(1,standortID);

    }

    /**
     * Testet, ob bei falscher Eingabe korrektes Verhalten auftritt.
     * TestID 39
     */
    @Test(expected=KomException.class)
    public void registerTestException() throws KomException {
        RegisterHandler testHandler = new RegisterHandler();
        String[] testString = {"Falsch", "Falko", "wambo" ,"KFC", "D"};
        testHandler.handle(testString);
    }
}
