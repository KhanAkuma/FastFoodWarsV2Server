package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 22.07.2016.
 * <p>
 * Klasse die eine Nachricht in die Datenbank eintraegt
 */
public class NachrichtenHandler {
    /**
     * Methode die die "NACHRICHT" anfrage behandelt
     *
     * @param toHandle String[] in Form {NACHRICHT}{senderId}{empfaenger}{nachricht}
     * @return {"TRUE", nachrichtId} oder  {"FALSE"}
     */
    public String[] handle(String[] toHandle) {

        if (!(toHandle[0].equals("NACHRICHT"))) {
            String[] warning = {"FALSE"};
            return warning;
        }
        try {
            String[] antwort = {"FALSE", ""};
            Database db = Database.gibInstanz();
            int empfaenger = db.gibSpielerId(toHandle[2]);
            int id = db.erstelleNachricht(Integer.parseInt(toHandle[1]), empfaenger, toHandle[3]);
            if (id != -1) {
                antwort[0] = "TRUE";
                antwort[1] = Integer.toString(id);
                return antwort;
            }
            return antwort;
        } catch (Exception e) {
            String[] warning = {"FALSE"};
            return warning;
        }

    }
}
