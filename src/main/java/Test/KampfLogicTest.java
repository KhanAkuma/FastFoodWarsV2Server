package Test;

import Server.Persistence.Database;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import org.junit.BeforeClass;
import org.junit.Test;
import Server.BusinessLogic.KampfLogic;
import static org.junit.Assert.*;

/**
 * Created by Falko on 03.08.2016.
 */
public class KampfLogicTest {

    @BeforeClass
    public static void prepareTest() {
        Database db = Database.gibInstanz();
        db.loescheTabellen();
        db.initialisiere();
    }

    @Test
    public void testOrder(){
        kaempfeTest();
        uebernehmeTest();
        verteidigerGewinntTest();
        ermittleStandortWertTest();
    }

    /**
     * Testet, ob die korrekten Werte fuer den
     * Kampf erstellt werden.
     * TestID 40
     */
    public void kaempfeTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Flako1", "blub","K","D");
        int spielerID2 = db.erstelleBenutzer("Flako2", "blub","K","D");
        int standortID1 = db.erzeugeNeuenStandort(spielerID1,"Test1",3,4);
        int standortID2 = db.erzeugeNeuenStandort(spielerID2,"Test1",4,3);
        String fraktion = db.gibFraktion(spielerID1);
        String angreiferArmee = "L1:2/L2:2/M1:2/M2:2/S1:5/S2:5/T1:3/T2:1/X1:0";
        String[][] verteidigerArmee = {{"KL1","4"},{"KL2","5"},{"KM1","2"}, {"KM2","2"}, {"KS1","0"}, {"KS2","0"}, {"T1","0"},{"T2","0"}, {"X1","0"}};
        KampfLogic kampfLogic = new KampfLogic();
        assertEquals(100,db.gibGemuese(standortID2));
        assertEquals(100,db.gibGemuese(standortID1));

        kampfLogic.kaempfe(spielerID1, angreiferArmee, verteidigerArmee, spielerID2, standortID1, spielerID2);

        //Pluendern erfolgreich
        assertEquals(92,db.gibGemuese(standortID2));
        assertEquals(108,db.gibGemuese(standortID1));

    }


    /**
     * Testet, ob ein Standort erfolgreich
     * von einem anderen Spieler uebernommen
     * werden kann.
     * TestID 41
     */
    public void uebernehmeTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Flako1", "blub","K","D");
        int spielerID2 = db.erstelleBenutzer("Flako2", "blub","K","D");
        int standortID1 = db.erzeugeNeuenStandort(spielerID1,"Test1",3,4);
        int standortID2 = db.erzeugeNeuenStandort(spielerID2,"Test1",4,3);
        String fraktion = db.gibFraktion(spielerID1);
        String angreiferArmee = "L1:2/L2:2/M1:2/M2:2/S1:5/S2:5/T1:3/T2:1/X1:0";
        String[][] verteidigerArmee = {{"KL1","4"},{"KL2","5"},{"KM1","2"}, {"KM2","2"}, {"KS1","0"}, {"KS2","0"}, {"T1","0"},{"T2","0"}, {"X1","0"}};
        KampfLogic kampfLogic = new KampfLogic();
        kampfLogic.uebernehme(spielerID1, angreiferArmee, verteidigerArmee, spielerID2, standortID1, spielerID2);

        assertEquals("3;4",db.gibStandortIds(standortID1));
        assertEquals("",db.gibStandortIds(standortID2));
        System.out.println();



    }

    /**
     * Testet, ob die Truppen nach einem
     * Kampf korrekt aktualisiert werden.
     * TestID 52
     */
    @Test
    public void aktualisiereTruppenTest(){}

    /**
     * Testet, ob das korrekte Verhalten eintritt,
     * sollte der verteidigende Spieler gewinnen.
     * TestID 53
     */
    public void verteidigerGewinntTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Flako1", "blub","K","D");
        int spielerID2 = db.erstelleBenutzer("Flako2", "blub","K","D");
        int standortID1 = db.erzeugeNeuenStandort(spielerID1,"Test1",3,4);
        int standortID2 = db.erzeugeNeuenStandort(spielerID2,"Test1",4,3);
        String fraktion = db.gibFraktion(spielerID1);
        String angreiferArmee = "L1:2/L2:2/M1:2/M2:2/S1:0/S2:0/T1:3/T2:0/X1:0";
        String[][] verteidigerArmee = {{"KL1","4"},{"KL2","5"},{"KM1","2"}, {"KM2","2"}, {"KS1","5"}, {"KS2","5"}, {"T1","0"},{"T2","0"}, {"X1","0"}};
        KampfLogic kampfLogic = new KampfLogic();
        kampfLogic.kaempfe(spielerID1, angreiferArmee, verteidigerArmee, spielerID2, standortID1, spielerID2);

    }

    /**
     * Testet, ob der Wert eines Standortes korrekt
     * zurueckgegeben wird
     * TestID 27
     */
    public void ermittleStandortWertTest(){
        Database db = Database.gibInstanz();
        int spielerID1 = db.erstelleBenutzer("Flako1", "blub","K","D");
        int standortID1 = db.erzeugeNeuenStandort(spielerID1,"Test1",3,4);
        db.baueGebaeude(standortID1,2,"KM", Integer.toUnsignedLong(2000));
        db.baueGebaeude(standortID1,3,"KM", Integer.toUnsignedLong(2000));
        db.baueGebaeude(standortID1,4,"KM", Integer.toUnsignedLong(2000));
        db.baueGebaeude(standortID1,5,"KM", Integer.toUnsignedLong(2000));


        KampfLogic kampfLogic = new KampfLogic();
        assertEquals(600,kampfLogic.ermittleStandortWert(standortID1));
    }

    /**
     * Testet, ob es moeglich ist einen Gutschein
     * zu erhalten.
     * TestID 28
     */
    @Test
    public void bekommeGutscheinTest(){}



}
