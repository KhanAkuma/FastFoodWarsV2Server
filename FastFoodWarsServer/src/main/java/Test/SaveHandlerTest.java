package Test;

import Server.BusinessLogic.SaveHandler;
import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Falko on 15.07.2016.
 */
public class SaveHandlerTest {
    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /**
     * Testet, ob darfForschen Attribut korrekt gesetzt und ausgegeben wird.
     * TestID 42
     */
    @Test
    public void darfForschenTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Falko1", "blub", "1", "D");
        String[] testString = {"SPEICHERN", "DARFFORSCHEN", spielerID1+""};
        SaveHandler saveHandler = new SaveHandler();
        String[] expected = {"TRUE"};
        String[] actual = saveHandler.handle(testString);

        assertArrayEquals(expected,actual);
        assertTrue(db.darfForschen(spielerID1));


    }

    /**
     * Testet, ob standortName korrekt gespeichert wird.
     * TestID 43
     */
    @Test
    public void standortNameTest(){}
}
