package Test;

import Server.BusinessLogic.BezahlenHandler;
import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Falko on 02.08.2016.
 */
public class BezahlenHandlerTest {
    @BeforeClass
    public static void prepareTest(){
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }


    /**
     * Testet, ob Ressourcen erfolgreich bezahlt werden.
     * TestID 14
     */
    @Test
    public void bezahleGeld(){
        String[] expected = {"TRUE"};
        Database db = Database.gibInstanz();
        db.erstelleBenutzer("Falko", "blub", "1", "D");
        String[] testString = {"BEZAHLEN", "GELD", db.gibSpielerId("Falko")+"", "200"};

        BezahlenHandler bezahlenHandler= new BezahlenHandler();

        String[] actual = bezahlenHandler.handle(testString);

        assertEquals(800, db.gibGeld(db.gibSpielerId("Falko")));
        assertArrayEquals(expected, actual);



    }


    /**
     * Testet, ob Geld erfolgreich bezahlt wird
     * TestID 15
     */
    @Test
    public void bezahleRessourcen(){
        String[] expected = {"TRUE"};
        Database db = Database.gibInstanz();
        db.erstelleBenutzer("Falko", "blub", "1", "D");

        int standortID = db.erzeugeNeuenStandort(db.gibSpielerId("Falko"), "Test1", 3, 4);

        String[] testString = {"BEZAHLEN", "RESSOURCEN", db.gibSpielerId("Falko")+"", standortID+"", "100", "50", "25"};

        BezahlenHandler bezahlenHandler = new BezahlenHandler();
        String[] actual = bezahlenHandler.handle(testString);

        assertEquals(0, db.gibFleisch(standortID));
        assertEquals(50, db.gibMehl(standortID));
        assertEquals(75, db.gibGemuese(standortID));
        assertArrayEquals(expected,actual);


    }



}
