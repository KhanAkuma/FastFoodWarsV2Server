package Server.BusinessLogic;

import Server.Exceptions.KomException;
import Server.Persistence.Database;

/**
 * Created by Jakob on 06.07.2016.
 * <p>
 * Klasse die sich um alle BAUEN Anfragen kuemmert
 */
public class Buildhandler {
    /**
     * Behandelt eine Anfrage der Kategorie "BAUEN", also wenn man ein Gebaeuude bauen will
     *
     * @param strings die fuer die Datenbank notwendigen Strings ist immer wie folgt aufgebaut: "BAUEN;SPIELERID;STANDORTID;.../
     * @return Gibt "TRUE" zurueck wenn erfolgreich
     * @throws KomException
     */
    public String[] handle(String[] strings) throws KomException {
        if (!strings[0].equals("BAUEN")) {
            throw new KomException("Fehler beim Bauen");
        }

        String[] antwort = {"Fehler in der ersten Abfrage, siehe Doku des BuildHandlers"};

        switch (strings[1]) {
            case "ERSTELLEN":
                return erstelleGebaeude(strings);

            case "UPDATE":
                return updateGebauede(strings);
        }
        return antwort;
    }

    /**
     * Methode die ein Gebaeude erstellt und in die Bauliste einfuegt
     *
     * @param strings String[] in Form: {"BAUEN", "ERSTELLEN", standortId, bauPlatz, gebaeudeTyp, spielerId]
     * @return {"TRUE"} oder {"FALSE"}
     */
    public String[] erstelleGebaeude(String[] strings) {
        String[] antwort = new String[3];
        Database db = Database.gibInstanz();

        try {
            int standortId = Integer.parseInt(strings[2]);
            int platz = Integer.parseInt(strings[3]);
            String typ = strings[4].substring(0, 2);
            int lvl = Integer.parseInt(strings[4].substring(2, 3));
            String[] gebaeude = db.gibGebaeudeEigenschaften(lvl, typ).split(";");
            long bauDauer = Long.parseLong(gebaeude[3]);
            long fertigstellung = System.currentTimeMillis() / 1000 + bauDauer;
            int kosten = Integer.parseInt(gebaeude[0]);
            int spielerId = Integer.parseInt(strings[5]);
            int geld = db.gibGeld(spielerId);
            geld -= kosten;
            db.aktualisiereGeld(spielerId, geld);
            antwort[1] = Integer.toString(db.baueGebaeude(standortId, platz, typ, fertigstellung));
            antwort[2] = Long.toString(fertigstellung);
            antwort[0] = "TRUE";
            return antwort;
        } catch (Exception e) {
            e.printStackTrace();
        }
        antwort[0] = "FALSE";
        return antwort;
    }

    /**
     * Methode die ein bereits bestehendes Gebaeude aufwertet
     *
     * @param strings String[] in Form: {{"BAUEN", "UPDATE", standortId, bauPlatz, gebaeudeTyp, spielerId]}
     * @return {"TRUE"} oder {"FALSE"}
     */
    public String[] updateGebauede(String[] strings) {
        String[] antwort = new String[3];
        Database db = Database.gibInstanz();

        try {
            int spielerId = Integer.parseInt(strings[4]);
            int gebaeudeId = Integer.parseInt(strings[2]);
            int lvl = Integer.parseInt(strings[3].substring(2, 3));
            String typ = strings[3].substring(0, 2);
            if (gebaeudeId == -1) {
                typ = strings[3].substring(1, 2);
            }
            String[] eigenschaften = db.gibGebaeudeEigenschaften(lvl, typ).split(";");
            long fertigstellung = System.currentTimeMillis() / 1000 + Integer.parseInt(eigenschaften[3]);
            if (gebaeudeId == -1) {
                gebaeudeId = db.gibHauptgebaeudeId(Integer.parseInt(strings[5]));
            }
            db.hochstufeGebaeude(gebaeudeId, fertigstellung);
            db.aktualisiereGeld(spielerId, (db.gibGeld(spielerId) - Integer.parseInt(eigenschaften[0])));
            antwort[0] = "TRUE";
            antwort[1] = Long.toString(fertigstellung);
            antwort[2] = "" + lvl;
            return antwort;
        } catch (Exception e) {
            e.printStackTrace();
        }

        antwort[0] = "FALSE";
        return antwort;
    }


}
