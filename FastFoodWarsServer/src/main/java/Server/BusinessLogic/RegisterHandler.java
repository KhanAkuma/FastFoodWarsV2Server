package Server.BusinessLogic;

import Server.Persistence.Database;
import Server.Exceptions.KomException;

/**
 * Created by Jakob on 30.06.2016.
 *
 * Klasse die das REGISTRIEREN behandelt
 */
public class RegisterHandler {
    public RegisterHandler(){}

    /**
     * Behandelt eine Registrieren-Anfrage: Traegt einen neuen Benutzer in die DB ein. Befehl muss wie folgt aufgebaut sein:
     * "REGISTRIEREN;SPIELERNAME;PASSWORT;FRAKTION;CHARAKTER";
     *
     * @param strings Liste der Strings: stings[1] = SPIELERNAME, strings[2]= PASSWORT
     * @return "TRUE" wenn eintragung erfolgreich
     * @throws KomException
     */
    public String[] handle(String[] strings) throws KomException {
        if(!strings[0].equals("REGISTRIEREN")){
            throw new KomException("Fehler bei der Registration");
        }
        Database db = Database.gibInstanz();

        String spielername = strings[1];
        if (db.gibSpielerId(spielername)!=-1){
            String[] fail = {"EXISTIERT BEREITS"};
            return fail;
        }
        String passwort = strings[2];
        String fraktion = strings[3];
        String charakter = strings[4];
        String[] antwort = {"FALSE"};

        try {
            db.erstelleBenutzer(spielername,passwort, fraktion, charakter);
            int spielerId = db.gibSpielerId(spielername);
            boolean generated = generateStandort(spielerId);
            if (generated){
                antwort[0] = "TRUE";
            }else{
                String[] voll= {"Server ist voll"};
                return voll;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return antwort;
    }

    /**
     * Generiert einen ersten Standort bei der Registration eines Spielers
     * @param spielerId in Id des Spielers der einen Standort bekommen soll
     * @return true, wenn erfolgreich
     */
    public boolean generateStandort(int spielerId){
        try {
            Database db = Database.gibInstanz();
            int[] koord = generateKoord();
            if(koord[0] == -1){
                return false;
            }
            String name = "Standort" + spielerId;
            db.erzeugeNeuenStandort(spielerId, name, koord[0] ,koord[1]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Erzeugt die Koordinaten des neu anzulegenden Standort in einem Radius von +-4 um den zuletzt erzeugten Standort
     * Wenn die Karte voll ist, wird [-1][-1] zurueckgegeben
     *
     * @return int[] in Form: {xKoordinate, yKoordinate}
     */
    private int[] generateKoord() {
        Database db = Database.gibInstanz();
        int[] koord = new int[2];
        String[] standorte = db.gibAlleStandortIds().split(";");
        int letzterStandort;
        int mapGroesse = db.gibKarte();
        if ( standorte.length >= (mapGroesse*mapGroesse)){
            koord[0] = -1;
            koord[1] = -1;
            return koord;
        }
        if(standorte[0].equals("")){
            letzterStandort = -1;
        }else {
            letzterStandort = Integer.parseInt(standorte[standorte.length-1]);
        }
        koord[0] = db.gibXKoordinate(letzterStandort);
        koord[1] = db.gibYKoordinate(letzterStandort);

        if(koord[0] == -1){
            koord[0] = mapGroesse/2;
            koord[1] = mapGroesse/2;
            return koord;
        }
        boolean erzeugt = false;
        int xMax = koord[0]+4;
        int xMin = koord[0]-4;
        if(xMin < 0){
            xMin = 0;
        }
        int yMax = koord[0]+4;
        int yMin = koord[0]-4;
        if(yMin < 0){
            yMin = 0;
        }
        mapGroesse = mapGroesse*mapGroesse;
        while(!erzeugt){
            int nummer = (int )(Math.random() * mapGroesse)+1;
            if (!db.istStandortBelegt(nummer)){
                erzeugt = true;
                String k = db.belegeStandort(nummer);
                String[] xy = k.split(";");
                koord[0] = Integer.parseInt(xy[0]);
                koord[1] = Integer.parseInt(xy[1]);
                if (koord[0] < xMin || koord[0] > xMax || koord[1] < yMin || koord[1] > yMax){
                    erzeugt = false;
                }
            }
        }
        return koord;
    }



}
