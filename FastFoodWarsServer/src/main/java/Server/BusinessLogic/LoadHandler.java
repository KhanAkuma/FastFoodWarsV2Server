package Server.BusinessLogic;

import Server.Persistence.Database;
import Server.Exceptions.KomException;

/**
 * Created by Jakob on 30.06.2016.
 *
 *Klasse die alle LADEN Anfragen bearbeitet
 */
public class LoadHandler {

    Database db = Database.gibInstanz();

    /**
     * Behandelt alle Anfragen vom Client, die mit dem String "LADEN" anfangen
     *
     * @param strings String[] in Form: {"LADEN", was geladen werden soll, weiterInfos[]}
     * @return String[] je nach Anfrage
     * @throws KomException
     */
    public String[] handle(String[] strings) throws KomException {
        if (!strings[0].equals("LADEN")){
            throw new KomException("Fehler beim Login.");
        }
        switch (strings[1]) {
            case "STANDORT":
                return generateStandortString(strings[2]);

            case "GEBAEUDE":
            return generateGebaeudeString(strings[2]);

            case "AUKTION":
                return generateAuktionsString(Integer.parseInt(strings[2]));

            case "NACHRICHT":
                return generateNachrichtenString(strings[2]);

            case "BONI":
                return generateBoniString(strings[2]);

            case "BEWEGUNG":
                return generateBewegung(strings[2]);
        }
        String[] antwort = new String[1];
        antwort[0] = "FALSE";
        return antwort;
    }

    /**
     * Erzeigt eine String[] entsprechend der BewegungsId in Form: {startId, zielId, truppen, ankunft, art}
     *
     * @param bewegungsId int Id der angefragten Bewegung
     * @return String[] in Form: {startId, zielId, truppen, ankunft, art}
     */
    private String[] generateBewegung(String bewegungsId) {
        int id = Integer.parseInt(bewegungsId);
        String[] eigenschaften = db.gibBewegungEigenschaften(id).split(";");
        String[] antwort = {eigenschaften[0], eigenschaften[1], eigenschaften[3], eigenschaften[4]};
        return antwort;
    }

    /**
     * Erzeugt einen StringArray in dem alle Boniwerte einer Boniklasse sthen
     *
     * @param boniId int Id der Boni
     * @return String[] in Form: {verteidigungsbonus, produktionsbonus, geldbonus, mehlProduktionsbonus,
     * fleischProduktionsbonus, gemueseProduktionsbonus, truppenGeschwindigkeitsbonus, truppenKostenbonus,
     * truppenAngriffsbonus, truppenVerteidigungsbonus, forschungsGeschwindigkeitsbonus, forschungsKostenbonus"}
     */
    private String[] generateBoniString(String boniId) {
        int spielerId = Integer.parseInt(boniId);
        int franchiseId = db.gibFranchiseId(spielerId);
        String boni = db.gibAlleBoni(spielerId);
        if(franchiseId > 0){
            double[] spielerBoni = parseStringArray(boni.split(";"));
            double[] franchiseBoni = parseStringArray(db.gibFranchiseBoni(franchiseId).split(";"));
            String[] summe = new String[spielerBoni.length];
            for(int i = 0; i < summe.length; i++){
                summe[i] = Double.toString(spielerBoni[i] + franchiseBoni[i]);
            }
            return summe;
        }

        String[] antwort = boni.split(";");
        return antwort;
    }

    /**
     * Wandelt einen StringArray in ein DoubleArray um
     *
     * @param array StringArray das umgewandelt werden soll
     * @return DoubleArray
     */
    private double[] parseStringArray(String[] array){
        double[] zahlen = new double[array.length];
        for(int i = 0; i< zahlen.length; i++){
            zahlen[i] = Double.parseDouble(array[i]);
        }
        return zahlen;
    }

    /**
     *Erzeugt einen StringArray der eine Nachricht wiedergibt
     *
     * @param nachrichtId ist die Id der angefragten Nachricht
     * @return String[] {"TRUE", sender, empfaenger, nachricht, datum}
     */
    private String[] generateNachrichtenString(String nachrichtId) {
        int id = Integer.parseInt(nachrichtId);
        String nachricht[] = db.gibNachricht(id);
        String sender = db.gibBenutzerName(Integer.parseInt(nachricht[0]));
        String empfaenger = db.gibBenutzerName(Integer.parseInt(nachricht[1]));
        String text = nachricht[2];
        String datum = nachricht[3];
        String[] antwort = {"TRUE", sender, empfaenger, text, datum};
        return antwort;
    }

    /**
     * Erzeugt aus einem entsprechenden Datenbankeintrag einen String der an den Client geschickt und dort in ein StandortObjekt umgewandelt wird
     *
     * @param standortId die Id des Standortes des Daten an den Client geschickt werden sollen
     * @return gibt einen String[] zurueck in Form {TRUE}{NAME}{name}{RESSOURCEN}{mehl}{gemuese}{fleisch}{KOORD}{x}{y}{HAUPTGEBAEUDE}{lvl}{VERTEIDIGUNG}{verteidigung}{GEBAEUDE}{gebaeudeIds}{HANDELSKAP}{handelskap}{bewegungsIds}
     */
    private String[] generateStandortString(String standortId) {
        String[] antwort = new String[18];
        antwort[0] = "FALSE";
        int id = 0;
        try {
            id = Integer.parseInt(standortId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            int mehl = db.gibMehl(id);
            int gemuese = db.gibGemuese(id);
            int fleisch = db.gibFleisch(id);
            int x = db.gibXKoordinate(id);
            int y = db.gibYKoordinate(id);
            int lvl = db.gibHauptgebaeude(id);
            String gebaeudeIds = db.gibGebaeudeIds(id);
            String name = db.gibStandortName(id);
            int verteidigung = db.gibStandortVerteidigung(id);
            String bewegungIds = db.gibBewegungen(id);
            if(bewegungIds.equals("")){
                bewegungIds = "0";
            }
            String hauptGebaeudeEigenschaften[] = db.gibGebaeude(db.gibHauptgebaeudeId(id)).split(";");
            String hInBau = hauptGebaeudeEigenschaften[4] + ";" + hauptGebaeudeEigenschaften[5];
            antwort[1] = "NAME";
            antwort[2] = name;
            antwort[3] = "RESSOUCEN";
            antwort[4] = Integer.toString(mehl);
            antwort[5] = Integer.toString(gemuese);
            antwort[6] = Integer.toString(fleisch);
            antwort[7] = "KOORD";
            antwort[8] = Integer.toString(x);
            antwort[9] = Integer.toString(y);
            antwort[10] = "HAUPTGEBAEUDE";
            antwort[11] = Integer.toString(lvl);
            antwort[12] = "VERTEIDIGUNG";
            antwort[13] = Integer.toString(verteidigung);
            antwort[14] = "GEBAEUDE";
            antwort[15] = gebaeudeIds;
            antwort[16] = bewegungIds;
            antwort[17] = hInBau;
            antwort[0] = "TRUE";
            return antwort;
        }catch (Exception e){}
        return antwort;
    }

    /**
     * Erzeugt einen String[] mit den Eigenschaften eines Gebaeudes
     *
     * @param gebaudeId int Id des gewuenschten Gebauedes
     * @return gibt String[] in Form zurueck: {TRUE, TYP, name, STELLPLATZ, nummer, imBau}
     */
    private String[] generateGebaeudeString(String gebaudeId){
        String[] gebaude = db.gibGebaeude(Integer.parseInt(gebaudeId)).split(";");
        String typ = gebaude[2] + gebaude[3];
        int nummer = Integer.parseInt(gebaude[1]);
        String[]antwort = {"TRUE", "TYP", typ, "STELLPLATZ", Integer.toString(nummer), gebaude[4]};
        return antwort;
    }

    /**
     *Erzeugt einen StringArray der eine Auktion darstellt
     *
     * @param auktionsId
     * @return gibt String[] in Form zurueck: {TRUE,ANBIETER,name,ANGEBOTENERESSOURCE,name,ANGEBOTSMENGE,menge,VERLANGTERESSOURCE,name,ANGEBOTSPREIS,menge}
     */
    private String[] generateAuktionsString(int auktionsId){
        String[] eigenschaften = db.gibAuktionEigenschaften(auktionsId).split(";");
        int standortId = Integer.parseInt(eigenschaften[0]);
        String name = db.gibBenutzerName(db.gibSpielerVonStandort(standortId));
        String[] antwort = {"TRUE","ANBIETER", name,"ANGEBOTENERESSOURCE",eigenschaften[1],"ANGEBOTSMENGE",eigenschaften[2],"VERLANGTERESSOURCE",eigenschaften[3],"ANGEBOTSPREIS",eigenschaften[4]};
        return antwort;
    }
}
