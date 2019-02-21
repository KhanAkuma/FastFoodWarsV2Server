package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 02.08.2016.
 * <p>
 * Thread, der beim Start des Servers gestartet wird und der laufend alle Bewegungen in der Datenbank aktualisiert
 */
public class BewegenThread extends Thread {
    private boolean sollLaufen;
    private Database db = Database.gibInstanz();

    public BewegenThread(boolean sollLaufen) {
        super("BewegenThread");
        this.sollLaufen = sollLaufen;
    }

    /**
     * Wenn Thread gestart wird, prueft er laufen, ob eine in der Datenbank eingetragen Bewegung ihren Ankunftszeitpunkt
     * erreicht hat. Wenn, dann wird die Bewegung ausgefuehrt.
     */
    public void run() {

        while (sollLaufen) {
            String fertigeBewegungen = db.gibAlleAngekommenenBewegungen();
            if (!fertigeBewegungen.equals("")) {
                String[] fertigeBewegungenArray = fertigeBewegungen.split(";");

                for (int i = 0; i < fertigeBewegungenArray.length; i++) {
                    String[] eigenschaften = db.gibBewegungEigenschaften(Integer.parseInt(fertigeBewegungenArray[i])).split(";");
                    int startId = Integer.parseInt(eigenschaften[0]);
                    int zielId = Integer.parseInt(eigenschaften[1]);
                    int startSpieler = db.gibSpielerVonStandort(startId);
                    int zielSpieler = db.gibSpielerVonStandort(zielId);
                    String[] truppen = eigenschaften[2].split("/");
                    String fraktion = db.gibFraktion(startSpieler);
                    boolean verbuendet = db.isVerbuendeter(startSpieler, zielSpieler);
                    if (eigenschaften[4].equals("TRANSFER")) {
                        if (verbuendet) {
                            aktualisiereTruppen(truppen, fraktion, zielId);
                        } else {
                            aktualisiereTruppen(truppen, fraktion, startId);
                        }
                        if (Math.random() < 0.1) {
                            db.aktualisiereGutscheine(startSpieler, db.gibGutscheine(startSpieler) + 1);
                        }
                        db.loescheBewegung(Integer.parseInt(fertigeBewegungenArray[i]));
                    } else if (eigenschaften[4].equals("UEBERFALL")) {
                        if (verbuendet) {
                            aktualisiereTruppen(truppen, fraktion, startId);
                        } else {
                            String angreifer = eigenschaften[2];
                            String[][] verteidiger = db.gibAlleEinheiten(zielId);
                            KampfLogic kampfLogic = new KampfLogic();
                            kampfLogic.kaempfe(startSpieler, angreifer, verteidiger, zielSpieler, startId, zielId);
                        }
                        db.loescheBewegung(Integer.parseInt(fertigeBewegungenArray[i]));
                    } else if (eigenschaften[4].equals("UEBERNAHME")) {
                        if (verbuendet) {
                            aktualisiereTruppen(truppen, fraktion, startId);
                        } else {
                            String angreifer = eigenschaften[2];
                            String[][] verteidiger = db.gibAlleEinheiten(zielId);
                            KampfLogic kampfLogic = new KampfLogic();
                            kampfLogic.uebernehme(startSpieler, angreifer, verteidiger, zielSpieler, startId, zielId);
                        }
                        db.loescheBewegung(Integer.parseInt(fertigeBewegungenArray[i]));
                    }
                }
            }
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Methode die alle Truppen eines Standorts aktualisiert, wird aufgerufen, wenn eine Bewegung und deren Konsequenzen berechnet sind
     *
     * @param truppen    String[] mit allen Truppenarten und deren Anzahl in Form {art1:anzahl, art2:anzahl, ... , artn:anzahl}
     * @param fraktion   String fuer die Fraktion "K", "M", "P"
     * @param standortId int Id des Standorts an dem die Truppen aktualisiert werden
     */
    private void aktualisiereTruppen(String[] truppen, String fraktion, int standortId) {
        int l1 = Integer.parseInt(truppen[0].split(":")[1]);
        int l2 = Integer.parseInt(truppen[1].split(":")[1]);
        int m1 = Integer.parseInt(truppen[2].split(":")[1]);
        int m2 = Integer.parseInt(truppen[3].split(":")[1]);
        int s1 = Integer.parseInt(truppen[4].split(":")[1]);
        int s2 = Integer.parseInt(truppen[5].split(":")[1]);
        int t1 = Integer.parseInt(truppen[6].split(":")[1]);
        int t2 = Integer.parseInt(truppen[7].split(":")[1]);
        db.aktualisiereEinheit(standortId, fraktion + "L1", l1);
        db.aktualisiereEinheit(standortId, fraktion + "L2", l2);
        db.aktualisiereEinheit(standortId, fraktion + "M1", m1);
        db.aktualisiereEinheit(standortId, fraktion + "M2", m2);
        db.aktualisiereEinheit(standortId, fraktion + "S1", s1);
        db.aktualisiereEinheit(standortId, fraktion + "S2", s2);
        db.aktualisiereEinheit(standortId, "T1", t1);
        db.aktualisiereEinheit(standortId, "T2", t2);
    }
}
