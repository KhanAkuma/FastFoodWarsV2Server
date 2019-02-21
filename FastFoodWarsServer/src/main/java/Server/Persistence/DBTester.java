package Server.Persistence;

import Server.Persistence.Database;

/**
 * Created by malip on 28.06.2016.
 */
public class DBTester {
    public static void main(String args[]) {
        Database db = Database.gibInstanz();
//        db.loescheTabellen();
        db.initialisiere();
    }
}