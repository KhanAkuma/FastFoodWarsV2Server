package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 22.07.2016.
 *
 * Klasse die BEZAHLEN Anfragen behandelt
 */
public class BezahlenHandler {
    /**
     * behandelt einen Kostenstring der immer wie folgt aufgebaut ist: {BEZAHLEN,GELD,spielerid,geld}" oder {BEZAHLEN,RESSOURCEN,spielerid,standortid,fleisch,mehl,gemuese}"
     * und zieht dann entsprechend die Ressourcen ab
     *
     * @param toHandle String[] mit allen Infos um die Anfrage zu behandeln
     * @return {"FALSE"} oder {"TRUE"} als Antwort an den Client
     */
    public String[] handle(String[] toHandle){
        String[] antwort = new String[1];
        antwort[0] = "FALSE";
        if (!(toHandle[0].equals("BEZAHLEN")) && !(toHandle[0].equals("BEZAHLEN"))){

            return antwort;
        }
        Database db = Database.gibInstanz();
        int spielerId = Integer.parseInt(toHandle[2]);
        switch (toHandle[1]){
            case "GELD":
                int boniId = db.gibBoniId(spielerId);
                double bonus = 1+ db.gibBonus(boniId, "forschungsKostenBonus");
                int geld = db.gibGeld(spielerId)-((int)(Integer.parseInt(toHandle[3])/bonus));
                db.aktualisiereGeld(spielerId, geld);
                antwort[0] = "TRUE";
                return antwort;

            default:
                return antwort;
        }
    }
}
