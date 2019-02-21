package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Falko on 30.06.2016.
 * <p>
 * Klasse die alle "SPEICHERN" Anfragen bearbeitet
 */
public class SaveHandler {

    public SaveHandler() {
    }

    /**
     * Behandelt alle "SPEICHERN" Anfragen
     *
     * @param strings String[] in Form: {"SPEICHERN, wasZuSpeicher, infosDazu}
     * @return {"TRUE"} oder {"FALSE"}
     */
    public String[] handle(String[] strings) {
        Database db = Database.gibInstanz();
        switch (strings[1]) {
            case "DARFFORSCHEN":
                db.setzeDarfForschen(Integer.parseInt(strings[2]));
                String[] antwort = {"TRUE"};
                return antwort;

            case "STANDORTNAME":
                db.setzteStandortnamen(Integer.parseInt(strings[2]), strings[3]);
                String[] antwort1 = {"TRUE"};
                return antwort1;
        }
        String[] antwort = {"FALSE"};
        return antwort;
    }


}
