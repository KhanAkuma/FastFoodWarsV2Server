package Test;

/**
 * Created by Falko on 22.07.2016.
 */
public class TestMain {

    public static void main(String[] args){
        TestClient testClient = new TestClient();
        try {
            testClient.initializeTestClient();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
