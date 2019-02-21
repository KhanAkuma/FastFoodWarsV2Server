package Server.BusinessLogic;

import Server.Persistence.Database;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jakob on 29.07.2016.
 * <p>
 * Klasse die alles zum Thema Truppen behandelt
 */
public class TruppenHandler {

    private Database db = Database.gibInstanz();

    /**
     * Methode die alle "TRUPPEN" Anfragen behandelt: "AUSBILDUNG": bildet die Anzahl der uebergebene Truppenart an
     * einem Standort aus und aktualisiert die Ressourcen.
     * "VERSCHIEBEN": instanziiert eine Truppenbewegung der uebergebenen Art zu dem uebergebenen Ziel
     *
     * @param toHandle {TRUPPEN, AUSBILDUNG, standortid, fraktion, art, anzahl}
     *                 {TRUPPEN, VERSCHIEBEN, startId, zielId, truppen(in Form: L1:0/L2:0/M1:0/M2:0/S1:0/S2:0/T1:0/T2:0/X:0), art}
     * @return String[] {TRUE, ankunftsZeitpunkt}
     */
    public String[] handle(String[] toHandle) {
        db = Database.gibInstanz();
        switch (toHandle[1]) {
            case "AUSBILDUNG":
                int standortId = Integer.parseInt(toHandle[2]);
                String typ = (toHandle[3] + toHandle[4]);
                if (toHandle[4].equals("T1") || toHandle[4].equals("T2") || toHandle[4].equals("X1")) {
                    typ = toHandle[4];
                }
                int boniId = db.gibBoniId(db.gibSpielerVonStandort(standortId));
                int anzahl = Integer.parseInt(toHandle[5]);
                String[] eigenschaften = db.gibEinheitenEigenschaften(typ).split(";");
                double bonus = 1 + db.gibBonus(boniId, "truppenKostenbonus");
                int fleischKosten = (int) ((Integer.parseInt(eigenschaften[1]) * anzahl) / bonus);
                int mehlKosten = (int) ((Integer.parseInt(eigenschaften[0]) * anzahl) / bonus);
                int gemueseKosten = (int) ((Integer.parseInt(eigenschaften[2]) * anzahl) / bonus);
                int fleisch = db.gibFleisch(standortId) - fleischKosten;
                int mehl = db.gibMehl(standortId) - mehlKosten;
                int gemuese = db.gibGemuese(standortId) - gemueseKosten;
                long fertigstellung = System.currentTimeMillis() / 1000 + Long.parseLong(eigenschaften[5]);
                db.aktualisiereRessourcen(standortId, fleisch, mehl, gemuese);
                db.erstelleEinheiten(standortId, typ, anzahl, fertigstellung);
                String[] antwort = {"TRUE"};
                return antwort;

            case "VERSCHIEBEN":
                int startId = Integer.parseInt(toHandle[2]);
                int zielId = Integer.parseInt(toHandle[3]);
                int xStart = db.gibXKoordinate(startId);
                int yStart = db.gibYKoordinate(startId);
                int xZiel = db.gibXKoordinate(zielId);
                int yZiel = db.gibYKoordinate(zielId);
                int spielerId = db.gibSpielerVonStandort(startId);
                String fraktion = db.gibFraktion(spielerId);
                String truppen = toHandle[4];
                String art = toHandle[5];
                String truppenArray[] = truppen.split("/");
                for (int i = 0; i < truppenArray.length; i++) {
                    String[] truppe = truppenArray[i].split(":");
                    if (truppe[0].contains("T") || truppe[0].contains("X")) {
                        db.aktualisiereEinheit(startId, truppe[0], -1 * Integer.parseInt(truppe[1]));
                    } else {
                        db.aktualisiereEinheit(startId, fraktion + truppe[0], -1 * Integer.parseInt(truppe[1]));
                    }
                }
                double geschwindigkeit = 1 + db.gibBonus(spielerId, "truppenGeschwindigkeitsbonus");
                long dauer = (long) ((int) Math.sqrt((xZiel - xStart) * (xZiel - xStart) + (yZiel - yStart) * (yZiel - yStart)) * 10 / geschwindigkeit);
                long ankunft = System.currentTimeMillis() / 1000 + dauer;
                int bewegungsId = db.erstelleBewegung(startId, zielId, truppen, ankunft, art);
                if (art.equals("UEBERNAHME")) {
                    KampfLogic kampfLogic = new KampfLogic();
                    int kosten = kampfLogic.ermittleStandortWert(zielId);
                    db.aktualisiereGeld(spielerId, db.gibGeld(spielerId) - kosten);
                }
                if (art.equals("UEBERNAHME") || art.equals("UEBERFALL")) {
                    NachrichtenHandler nh = new NachrichtenHandler();
                    Date date = new Date(ankunft * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy ");
                    String formattedDate = sdf.format(date);
                    String nachricht = "ACHTUNG " + art + "! Um " + formattedDate + " werd ich die Feuer \n ueber deinem Standort " + db.gibStandortName(zielId) + " leuchten sehen! HAHAHAHAA!";
                    String[] nachrichtInfos = {"NACHRICHT", "" + spielerId, "" + db.gibSpielerVonStandort(zielId), nachricht};
                    nh.handle(nachrichtInfos);
                }
                String[] antwort1 = {"TRUE", "" + bewegungsId, "" + ankunft};
                return antwort1;
        }
        String[] antwort = {"FALSE"};
        return antwort;
    }


}
