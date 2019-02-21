package Test;

import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Falko on 03.08.2016.
 */
public class UpdateHandlerTest {
    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    /**
     * Testet, ob das update eines Baumes korrekt funktioniert.
     * TestID 48
     */
    @Test
    public void updateBaumTest(){}


    /**
     *Testet ob das Update von Ressourcen korrekt funktioniert.
     * TestID 49
     */
    @Test
    public void updateRessourcenTest(){}


    /**
     * Testet, ob das Update des Standortes korrekt funktioniert.
     */
    @Test
    public void updateStandortTest(){}


    /**
     * Testet, ob das Erzeugen von Einheiten korrekt funktioniert.
     * TestID 51
     */
    @Test
    public void erzeugeEinheitenTest(){}
}
