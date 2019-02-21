package Server.BusinessLogic;

import Server.Exceptions.KomException;
import Server.Persistence.Database;

/**
 * Created by Jakob on 30.06.2016.
 */
public class LoginHandler {
    /**
     * Methode den alle wichtigen Daten fuer den LoginProzess bereit stellt. Prueft vorher ob LoginEingaben korrekt sind
     * und der Spieler noch am Leben
     *
     * @param strings String[] in Form: {"EINLOGGEN", spielername, passwort}
     * @return Gibt Loginarray in Form {TRUE,SPIELER,spielerId,spielerName,charakterbaum,FRAKTION,
     * fraktion,fraktionbaum,FRANCHISE,franchiseId,GELD,geld,GUTSCHEINE,gutscheine,STANDORTE,standortids,
     * CHARAKTER,charakter,BONI,boniId,VNACHRICHTEN,vNachrichtenIds,ENACHRICHTEN,eNachrichtenIds,AUKTIONEN,auktionsIds,
     * lastUpdate,darfForschen} wenn LoginDaten korrekt sonst {"FALSE", warum false}
     * @throws KomException
     */
    public String[] handle(String[] strings) throws KomException {
        if (!strings[0].equals("EINLOGGEN")) {
            throw new KomException("Fehler beim Login.");
        }

        if (strings.length == 3) {

            String spielername = strings[1];
            String passwort = strings[2];
            Database db = null;
            try {
                db = Database.gibInstanz();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (db.loginCorrect(spielername, passwort)) {
                String antwort[] = new String[28];
                antwort[0] = "TRUE";
                antwort[1] = "SPIELER";
                int spielerId = db.gibSpielerId(spielername);
                String charakterBaum = db.gibCharakterBaum(spielerId);
                String fraktion = db.gibFraktion(spielerId);
                String fraktionBaum = db.gibFraktionBaum(spielerId);
                int franchiseId = db.gibFranchiseId(spielerId);
                int geld = db.gibGeld(spielerId);
                int gutscheine = db.gibGutscheine(spielerId);
                String standorte = db.gibStandortIds(spielerId);
                if (standorte.equals("")) {
                    antwort[0] = "FALSE";
                    antwort[1] = "Du hast verloren! Du hast \n keinen Standort mehr!";
                    return antwort;
                }
                String charakter = db.gibCharakter(spielerId);
                int boniId = db.gibBoniId(spielerId);
                String vNachrichtenIds = db.gibVersendeteNachrichten(spielerId);
                String eNachrichtenIds = db.gibEmpfangeneNachrichten(spielerId);
                antwort[2] = Integer.toString(spielerId);
                antwort[3] = spielername;
                antwort[4] = charakterBaum;
                antwort[5] = "FRAKTION";
                antwort[6] = fraktion;
                antwort[7] = fraktionBaum;
                antwort[8] = "FRANCHISE";
                antwort[9] = Integer.toString(franchiseId);
                antwort[10] = "GELD";
                antwort[11] = Integer.toString(geld);
                antwort[12] = "GUTSCHEINE";
                antwort[13] = Integer.toString(gutscheine);
                antwort[14] = "STANDORTE";
                antwort[15] = standorte;
                antwort[16] = "CHARAKTER";
                antwort[17] = charakter;
                antwort[18] = "BONI";
                antwort[19] = Integer.toString(boniId);
                antwort[20] = "VNACHRICHTEN";
                antwort[21] = vNachrichtenIds;
                antwort[22] = "ENACHRICHTEN";
                antwort[23] = eNachrichtenIds;
                antwort[24] = "AUKTIONEN";
                antwort[25] = db.gibAuktionIds();
                antwort[26] = Long.toString(db.gibLetztesUpdate(spielerId));
                antwort[27] = Boolean.toString(db.darfForschen(spielerId));
                return antwort;

            } else {
                String[] antwort = new String[2];
                antwort[0] = "FALSE";
                antwort[1] = "Benutzername oder Passwort falsch.";
                return antwort;
            }
        }
        String[] antwort = new String[2];
        antwort[0] = "FALSE";
        antwort[1] = "FATAL ERROR";
        return antwort;
    }
}
