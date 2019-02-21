package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 20.07.2016.
 * <p>
 * Klasse die sich um die Forschungsbaeume der Spieler kuemmert
 */
public class BaumHandler {

    public BaumHandler() {
    }

    /**
     * Behandelt die UPDATEBAUM Anfrage des Spieler. Es wird eine aktualisierung eines Baumstrings in der Datenbank in Auftrag gegeben
     *
     * @param strings {"UPDATEBAUM", spielerId, baumArt, baum, knoten};
     * @return String[] mit {"FASLE"} oder {"TRUE", auftragsId, fertigstellungsZeitpunkt}
     */
    public String[] handle(String[] strings) {

        int id = Integer.parseInt(strings[1]);
        String forschungsBaum = strings[3];
        Database db = Database.gibInstanz();
        String[] antwort = {"FALSE", "", ""};
        int auftragsId;
        char knoten;
        long fertigstellung;
        String baumArt;
        switch (strings[2].substring(0, 1)) {
            case "F":
                db.aktualisiereFraktionsBaum(id, forschungsBaum);
                knoten = strings[4].charAt(0);
                baumArt = strings[2];
                fertigstellung = System.currentTimeMillis() / 1000 + db.gibForschungsdauer(baumArt, knoten);
                auftragsId = db.erforscheKnoten(id, baumArt, knoten, fertigstellung);
                antwort[0] = "TRUE";
                antwort[1] = Integer.toString(auftragsId);
                antwort[2] = Long.toString(fertigstellung);
                return antwort;

            case "C":
                db.aktualisiereCharakterbaum(id, forschungsBaum);
                knoten = strings[4].charAt(0);
                baumArt = strings[2];
                fertigstellung = System.currentTimeMillis() / 1000 + db.gibForschungsdauer(baumArt, knoten);
                db.aktualisiereFranchiseBaum(id, forschungsBaum);
                auftragsId = db.erforscheKnoten(id, baumArt, knoten, fertigstellung);
                antwort[0] = "TRUE";
                antwort[1] = Integer.toString(auftragsId);
                antwort[2] = Long.toString(fertigstellung);
                return antwort;

            case "G":
                knoten = strings[4].charAt(0);
                baumArt = strings[2];
                fertigstellung = System.currentTimeMillis() / 1000 + db.gibForschungsdauer(baumArt, knoten);
                db.aktualisiereFranchiseBaum(id, forschungsBaum);
                auftragsId = db.erforscheKnoten(id, baumArt, knoten, fertigstellung);
                antwort[0] = "TRUE";
                antwort[1] = Integer.toString(auftragsId);
                antwort[2] = Long.toString(fertigstellung);
                return antwort;

            default:
                return antwort;

        }


    }
}
