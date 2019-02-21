package Test;

import Server.BusinessLogic.AuktionHandler;
import Server.Persistence.Database;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Falko on 03.08.2016.
 */
public class AuktionHandlerTest {

    @BeforeClass
    public static void prepareTest(){
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
        db.erzeugeNeuenStandort(db.erstelleBenutzer("Falko", "blub" , "K", "D"),"Test1", 3,3);
        db.erzeugeNeuenStandort(db.erstelleBenutzer("AlterEgo", "blub", "K", "K"),"Test2", 3,4);
    }

    @Test
    public void testOrder(){
        erstellenTest();
        abschliessenTest();
        loescheTest();
    }


    /**
     * Testet, ob Auktionen erfolgreich geladen werden
     * TestID 7
     */
    public void erstellenTest(){
        String[] expected = {"TRUE"};
        Database db = Database.gibInstanz();
        String verkaeuferID = db.gibSpielerId("Falko")+"";
        int verkaeuferOrt = Integer.parseInt(db.gibStandortIds(db.gibSpielerId("Falko")));
        String[] testString = {"AUKTION", "ERSTELLE", verkaeuferID, verkaeuferOrt+"", "Mehl", "300", "Fleisch", "300"};
        AuktionHandler auktionHandler = new AuktionHandler();

        assertArrayEquals(expected,auktionHandler.handle(testString));
    }


    /**
     * Testet, ob Auktionen erfolgreich abgeschlossen werden koennen
     * TestID 8
     */
    public void abschliessenTest(){
        String[] expected = {"TRUE"};
        Database db = Database.gibInstanz();

        String kaeuferID = db.gibSpielerId("AlterEgo")+"";
        String kaeuferOrt = db.gibStandortIds(Integer.parseInt(kaeuferID));
        String[] testString = {"AUKTION", "ABSCHLIESSEN", kaeuferID, db.gibAuktionIds(), kaeuferOrt};
        AuktionHandler auktionHandler = new AuktionHandler();
        assertArrayEquals(expected, auktionHandler.handle(testString));
    }


    /**
     * Testet, ob Auktionen erfolgreich geloescht werden koennen
     * TestID 9
     */
    public void loescheTest(){
        String[] expected = {"TRUE"};

        Database db = Database.gibInstanz();
        String verkaeuferID = db.gibSpielerId("Falko")+"";
        int verkaeuferOrt = Integer.parseInt(db.gibStandortIds(db.gibSpielerId("Falko")));

        String[] testString = {"AUKTION", "LOESCHE", (db.erstelleAuktion(verkaeuferOrt, "Mehl", 300, "Fleisch", 300))+"" ,db.gibSpielerId("Falko")+""};
        AuktionHandler auktionHandler = new AuktionHandler();
        System.out.println(auktionHandler.handle(testString));
        assertArrayEquals(expected,auktionHandler.handle(testString));

    }



}
