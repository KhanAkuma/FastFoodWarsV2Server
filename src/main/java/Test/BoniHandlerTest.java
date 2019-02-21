package Test;

import Server.BusinessLogic.BezahlenHandler;
import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Falko on 05.08.2016.
 */
public class BoniHandlerTest {
    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /**
     * Testet, ob ein Booster in der Datenbank korrekt
     * aktiviert wird, sprich mitsamt aller Auswirkungen
     * auf Produktion, Forschung und Einheiten.
     * TestID 52
     */
    @Test
    public void boosterTest(){}

}
