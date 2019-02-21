package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Falko on 24.07.2016.
 * <p>
 * Klasse die alle Anfragen zum Thema Franchise bearbeitet
 */
public class FranchiseHandler {

    public FranchiseHandler() {
    }

    /**
     * Das zu uebergebende StringArray definiert an zweiter Stelle, welche Aktion fuer Franchises durchgefuehrt
     * werden soll.
     *
     * @param strings String[] in Form: {"FRANCHISE", aktion, benoetigetInfos[]}
     * @return {"TRUE"} oder {"FALSE"}
     */
    public String[] handle(String[] strings) {

        Database db = Database.gibInstanz();
        int franchiseId;

        switch (strings[1]) {
            case "ERSTELLEN":
                return erstelleFranchise(strings);

            case "BEITRETEN":
                return treteFranchiseBei(strings);

            case "BEKOMMEINFOS":
                return gibFranchiseInfos(strings);

            case "BEKOMMENAMEN":
                return gibMitgliederNamen(strings);

            case "EINZAHLEN":
                return einzahlenFranchise(strings);

            case "BEZAHLEN":
                franchiseId = Integer.parseInt(strings[2]);
                int preis = Integer.parseInt(strings[3]);
                int geld = db.gibFranchiseGeld(franchiseId);
                geld -= preis;
                db.aktualisiereFranchiseGeld(franchiseId, geld);
                String[] antwort = {"TRUE", Integer.toString(geld)};
                return antwort;

            case "ANKUENDIGUNG":
                franchiseId = Integer.parseInt(strings[2]);
                for (int i = 3; i < strings.length; i++) {
                    db.aktualisiereFranchiseNachricht(franchiseId, i - 2, strings[i]);
                }
                String[] antwort1 = {"TRUE"};
                return antwort1;

            case "BEKOMMEANKUENDIGUNG":
                franchiseId = Integer.parseInt(strings[2]);
                String nachrichtenIds = db.gibAlleFranchiseNachrichtenIds(franchiseId);
                if (!nachrichtenIds.equals("")) {
                    String[] ids = nachrichtenIds.split(";");
                    String[] nachrichten = new String[15];
                    for (int i = 0; i < ids.length; i++) {
                        nachrichten[i] = db.gibFranchiseNachricht(Integer.parseInt(ids[i]));
                    }
                    return nachrichten;
                } else {
                    String[] antwort2 = new String[15];
                    antwort2[0] = "Es gibt keine Ankuendigungen.";
                    return antwort2;
                }

            default:
                String[] defaultantwort = {"Krtischer Fehler bei FranchiseAbfrage"};
                return defaultantwort;
        }
    }

    /**
     * Methode die alle Mitgliedernamen einer Franchise als String[] zurueckgibt
     *
     * @param strings String[] in Form: {"FRANCHISE", "BEKOMMENAMEN", franchiseId}
     * @return {namen1, ..., namenN}
     */
    private String[] gibMitgliederNamen(String[] strings) {
        Database db = Database.gibInstanz();

        int franchiseId = Integer.parseInt(strings[2]);
        String[] ids = db.gibFranchiseMitglieder(franchiseId).split(";");
        String[] namen = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            String name = db.gibBenutzerName(Integer.parseInt(ids[i]));
            namen[i] = name;
        }
        return namen;
    }

    /**
     * Erwartet StringArray mit FRANCHISE;ERSTELLEN;SPIELERID;GEWUeNSCHTER-NAME
     *
     * @param strings
     * @return Bestaetigung, ob erfolgreich und FranchiseID
     */
    public String[] erstelleFranchise(String[] strings) {

        Database db = Database.gibInstanz();
        int spielerId = Integer.parseInt(strings[2]);
        String name = strings[3];
        if (db.gibFranchiseId(name) == -1) {
            int franchiseId = db.erstelleFranchise(spielerId, name);
            String franchiseID = Integer.toString(franchiseId);
            db.aktualisiereFranchise(spielerId, franchiseId);
            for (int i = 0; i < 15; i++) {
                db.erstelleFranchiseNachricht(franchiseId, "");
            }
            String[] antwort = {"TRUE", franchiseID};
            return antwort;
        }
        String[] antwort = {"FALSE"};
        return antwort;
    }

    /**
     * Erwartet StringArray mit FRANCHISE;BEITRETEN;SPIELERID;FRANCHISENAME
     *
     * @param strings
     * @return Bestaetigung, ob erfolgreich und FranchiseID
     */
    public String[] treteFranchiseBei(String[] strings) {

        Database db = Database.gibInstanz();
        int franchiseId = db.gibFranchiseId(strings[3]);
        int spielerId = Integer.parseInt(strings[2]);
        if (franchiseId != -1) {
            if (db.gibFraktion(spielerId).equals(db.gibFraktion(db.gibFranchiseCeo(franchiseId)))) {
                db.hinzufuegeSpielerZuFranchise(spielerId, franchiseId);
            } else {
                String[] antwort = {"FALSE", "Du gehoerst der falschen Fraktion an."};
                return antwort;
            }

        } else {
            String[] antwort = {"FALSE", "Es gibt kein Franchise mit dem Name."};
            return antwort;
        }

        String[] antwort = {"TRUE", Integer.toString(franchiseId)};

        return antwort;
    }

    /**
     * Erwartet StringArray mit FRANCHISE;BEKOMMEINFOS;FRANCHISEID
     *
     * @param strings
     * @return Liefert FRANCHISENAME;CEONAME;FRANCHISEGELD;FRANCHISEBAUM;MITGLIEDERIDS
     */
    public String[] gibFranchiseInfos(String[] strings) {
        //TODO: Bekomme Infos aus der DB

        Database db = Database.gibInstanz();

        int franchiseId = Integer.parseInt(strings[2]);
        int ceoId = db.gibFranchiseCeo(franchiseId);
        String ceo = db.gibBenutzerName(ceoId);
        String name = db.gibFranchiseName(franchiseId);
        int geld = db.gibFranchiseGeld(franchiseId);
        String franchiseBaum = db.gibFranchiseBaum(franchiseId);
        String mitgliederListe = db.gibFranchiseMitglieder(franchiseId);

        String[] antwort = new String[5];

        antwort[0] = name;
        antwort[1] = ceo;
        antwort[2] = Integer.toString(geld);
        antwort[3] = franchiseBaum;
        antwort[4] = mitgliederListe;

        return antwort;
    }

    /**
     * Methode, die das uebergebene Geld an die uebergebene Franchise gibt
     *
     * @param strings String[] in Form: {"FRANCHISE", "EINZAHLEN", franchiseId, spielerId, geldMenge}
     * @return {"TRUE", franchiseGeld, spielerGeld} oder {"FALSE"}
     */
    public String[] einzahlenFranchise(String[] strings) {
        Database db = Database.gibInstanz();
        String[] antwort = new String[3];

        try {
            int franchiseId = Integer.parseInt(strings[2]);
            int franchiseGeld = db.gibFranchiseGeld(franchiseId);
            int spielerId = Integer.parseInt(strings[3]);
            int spielerGeld = db.gibGeld(spielerId);
            franchiseGeld = franchiseGeld + Integer.parseInt(strings[4]);
            spielerGeld = spielerGeld - Integer.parseInt(strings[4]);


            db.aktualisiereGeld(spielerId, spielerGeld);
            db.aktualisiereFranchiseGeld(franchiseId, franchiseGeld);

            antwort[0] = "TRUE";
            antwort[1] = franchiseGeld + "";
            antwort[2] = spielerGeld + "";

            return antwort;
        } catch (Exception e) {
            e.printStackTrace();
        }
        antwort[0] = "FALSE";
        return antwort;
    }


}
