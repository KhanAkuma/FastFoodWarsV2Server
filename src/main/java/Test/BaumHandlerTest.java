package Test;

import Server.BusinessLogic.BaumHandler;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Falko on 22.07.2016.
 */
public class BaumHandlerTest {

    BaumHandler testHandler = new BaumHandler();

    @BeforeClass
    public static void prepareTest(){
    }


    /**
     * Testet, ob CharakterBaum erfolgreich abgespeichert wird
     * TestID 10
     */
    @Test
    public void handleTestC(){
        String[] testStringC = {"UPDATEBAUM", "333", "C", "Bom"};
        assertSame("TRUE", testHandler.handle(testStringC)[0]);

    }

    /**
     * Testet, ob FraktionsBaum erfolgreich abgespeichert wird
     * TestID 11
     */
    @Test
    public void handleTestF(){
        String[] testStringF = {"UPDATEBAUM", "333", "F", "Bom"};
        assertSame("TRUE", testHandler.handle(testStringF)[0]);
    }

    /**
     * Testet, ob FranchiseBaum erfolgreich abgespeichert wird
     * TestID 12
     */
    @Test
    public void handleTestG(){
        String[] testStringF = {"UPDATEBAUM", "333", "G", "Bom"};
        assertSame("TRUE", testHandler.handle(testStringF)[0]);
    }

    /**
     * Testet, ob bei falscher Eingabe Baum nicht abgespeichert wird.
     * TestID 13
     */
    @Test
    public void handleTestFail(){
        String[] testStringFail = {"UPDATEBAUM", "333", "FALSE", "Bom"};
        assertSame("FALSE", testHandler.handle(testStringFail)[0]);

    }
}
