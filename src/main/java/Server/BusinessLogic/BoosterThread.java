package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 31.07.2016.
 * <p>
 * Thread der laufend alle Boster aktualisiert
 */
public class BoosterThread extends Thread {

    private boolean sollLaufen;

    public BoosterThread(boolean sollLaufen) {
        this.sollLaufen = sollLaufen;
    }

    /**
     * Wenn Thread gestartet wird, prueft er laufend, ob Booster in der Datenbank abgelaufen sind, wenn dann loescht er
     * sie und entfernt dessen Effekt
     */
    public void run() {
        Database db = Database.gibInstanz();
        while (sollLaufen) {
            String boosterIds = db.gibAlleFertigenBooster();

            if (!boosterIds.equals("")) {
                String[] ids = boosterIds.split(";");
                for (String id : ids) {
                    int boosterId = Integer.parseInt(id);
                    String[] eigenschaften = db.gibAktiveBoosterEigenschaft(boosterId).split(";");
                    int spielerId = Integer.parseInt(eigenschaften[0]);
                    String bonus = eigenschaften[1];
                    double boost = Double.parseDouble(eigenschaften[2]);
                    double wert = db.gibBonus(spielerId, bonus) - boost;
                    db.aktualisiereBonus(spielerId, bonus, wert);
                    db.loescheBooster(boosterId);
                }
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
