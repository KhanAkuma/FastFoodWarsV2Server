package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 27.07.2016.
 * <p>
 * Ist eigentlich nur ein BoosterHandler geworden. Behandelt das Ereignis, dass ein Spieler einen Booster kauft
 */
public class BoniHandler {
    /**
     * Methode die die Anfrage "BONI" behanldelt. Traegt den uebergebenen Booster in die Datenbank ein und fuegt den Boni
     * das Boosterupdate hinzu
     *
     * @param toHandle String[] in Form: {"BONI", "BOOSTER", spielerId, bonusTyp, bonusWert, gutscheinKosten}
     * @return {"TRUE"} oder {"FALSE"}
     */
    public String[] handle(String[] toHandle) {
        Database db = Database.gibInstanz();
        int spielerId = Integer.parseInt(toHandle[2]);
        String bonustyp = toHandle[3];
        double bonus = Double.parseDouble(toHandle[4]) / 1000;
        switch (toHandle[1]) {
            case "BOOSTER":
                long dauer = System.currentTimeMillis() / 1000 + 10;
                db.erstelleBooster(spielerId, bonustyp, bonus, dauer);
                int gutscheine = db.gibGutscheine(spielerId) - Integer.parseInt(toHandle[5]);
                db.aktualisiereBonus(spielerId, bonustyp, db.gibBonus(db.gibBoniId(spielerId), bonustyp) + bonus);
                db.aktualisiereGutscheine(spielerId, gutscheine);
                String[] antwort = {"TRUE"};
                return antwort;
        }
        String[] antwort = {"FALSE"};
        return antwort;
    }
}
