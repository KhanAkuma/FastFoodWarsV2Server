package Server.BusinessLogic;

import Server.Persistence.Database;

import java.util.Arrays;

/**
 * Created by Jakob on 26.07.2016.
 *
 * Klasse die die "UPDATE" Anfrage behandelt
 */
public class UpdateHandler {

    /**
     * Methode die den UPDATE String erzeugt, der an de Client geschickt wird, um dort alle Ressourcen und Truppen und Baeume zu aktualisieren
     *
     * @param toHandle String[] in Form: {"UPPDATE", spielerId, letzterUpdateZeitpunkt}
     * @return String[] in Form: {"TRUE,timestamp,geld,"standortid;fleisch;mehl;gemuese;l1/l2/m1/m2/s1/s2;l1ia/l2ia/m1ia/m2ia/s1ia/s2ia-standortid...",charakterBaum, fraktionsBaum, franchiseBaum}
     */
    public String[] handle(String[] toHandle){

        if(!toHandle[0].equals("UPDATE")){
            String[] antwort= {"FALSE"};
            return antwort ;
        }
        long oldTimestamp = Long.parseLong(toHandle[2]);
        long newTimestamp = System.currentTimeMillis()/1000;
        int updateLength = (int)(newTimestamp-oldTimestamp);
        int spielerId = Integer.parseInt(toHandle[1]);
        String[] ressourcen = updateRessourcen(spielerId, updateLength);
        String[] baeume = updateBaeume(spielerId);
        String[] antwort = Arrays.copyOf(ressourcen, ressourcen.length + baeume.length);
        System.arraycopy(baeume, 0, antwort, ressourcen.length, baeume.length);
        antwort[1] = Long.toString(newTimestamp);
        return antwort;
    }

    /**
     * Methode die die aktuellen Baeume eines Spielers zurueck gibt
     *
     * @param spielerId int Id des Spielers
     * @return String[] in Form: {charakterBaum, fraktionsBaum, franchiseBaum (wenn vorhanden)}
     */
    private String[] updateBaeume(int spielerId) {
        Database db = Database.gibInstanz();
        String[] antwort = new String[3];
        antwort[0] = db.gibCharakterBaum(spielerId);
        antwort[1] = db.gibFraktionBaum(spielerId);
        int franchiseId = db.gibFranchiseId(spielerId);
        if(franchiseId != 0){
            antwort[2] = db.gibFranchiseBaum(franchiseId);
        }else{
            antwort[2] = "";
        }
        return antwort;
    }

    /**
     * Methode die die aktuellen Ressourcen berechnet
     *
     * @param spielerId int Id des Spielers, dessen Ressourcen und Truppen aktualisiert werden soll
     * @param updateLength int laenge des Zeitraums zwischen jetzt und der letzten Aktualisierung
     * @return String[] in Form: {fleisch;mehl;gemuese}
     */
    public String[] updateRessourcen(int spielerId, int updateLength){
        Database db = Database.gibInstanz();
        String[] antwort = new String[5];
        antwort[0] = "TRUE";
        int geld = db.gibGeld(spielerId);
        int geldProd = 0;
        double geldProdBonus =1+ db.gibBonus(spielerId, "geldbonus")+ db.gibProduktionsbonus(spielerId);
        int franchiseId = db.gibFranchiseId(spielerId);
        if (franchiseId > 0 ){
            geldProdBonus += db.gibFranchiseBonus(franchiseId, "geldbonus");
        }
        String standortUpdate = "";
        String[] standortIds = db.gibStandortIds(spielerId).split(";");
        for (String id: standortIds) {
            if (!standortUpdate.equals("")){
                standortUpdate += "-";
            }
            int standortId = Integer.parseInt(id);
            int lvl = db.gibHauptgebaeude(standortId);
            String hauptgebaeude1 = db.gibGebaeudeEigenschaften(lvl, "H");
            String[] hauptgebaeude = hauptgebaeude1.split(";");
            geldProd += Integer.parseInt(hauptgebaeude[1]);
            standortUpdate += updateStandort(spielerId, standortId, updateLength);
        }
        geld += (int)(geldProd*geldProdBonus*updateLength);
        db.aktualisiereGeld(spielerId,geld);
        antwort[4] = Integer.toString(db.gibGutscheine(spielerId));
        antwort[2] = Integer.toString(geld);
        antwort[3] = standortUpdate;
        return antwort;
    }

    /**
     *
     * Methode die die aktuellen Ressourcen berechnet und diese sowie die Truppen im Standort zurueck gibt
     *
     * @param spielerId int Id des Spielers, dessen Ressourcen und Truppen aktualisiert werden soll
     * @param standortId int Id des Standorts der aktualisiert werden soll
     * @param updateLength int laenge des Zeitraums zwischen jetzt und der letzten Aktualisierung
     * @return String[] in Form: {geld,"standortid;fleisch;mehl;gemuese;l1/l2/m1/m2/s1/s2;l1ia/l2ia/m1ia/m2ia/s1ia/s2ia-standortid..."}
    */
    public String updateStandort(int spielerId, int standortId, int updateLength){
        Database db = Database.gibInstanz();
        String antwort = "";
        int mehl = db.gibMehl(standortId);
        int gemuese = db.gibGemuese(standortId);
        int fleisch = db.gibFleisch(standortId);
        if (db.gibGebaeudeIds(standortId).equals("")){
            antwort += standortId + ";" + fleisch + ";" + mehl + ";" + gemuese;
            return antwort;
        }
        String[] gebauede = db.gibGebaeudeIds(standortId).split(";");
        int fleischProd = 0;
        int mehlProd = 0;
        int gemueseProd = 0;
        int kap = 0;
        for (String id: gebauede) {
            if (id.equals("")){
                continue;
            }
            int gebauedeId = Integer.parseInt(id);
            String[] daten = db.gibGebaeude(gebauedeId).split(";");
            String[] eigenschaften;
            String gebaeudeTyp = daten[2];
            int lvl = Integer.parseInt(daten[3]);
            if (!Boolean.parseBoolean(daten[4]) || gebaeudeTyp.contains("L")) {
                switch (gebaeudeTyp.substring(1)) {
                    case "S":
                        eigenschaften = db.gibGebaeudeEigenschaften(lvl, gebaeudeTyp).split(";");
                        fleischProd += Integer.parseInt(eigenschaften[1]);
                        break;
                    case "M":
                        eigenschaften = db.gibGebaeudeEigenschaften(lvl, gebaeudeTyp).split(";");
                        mehlProd += Integer.parseInt(eigenschaften[1]);
                        break;
                    case "B":
                        eigenschaften = db.gibGebaeudeEigenschaften(lvl, gebaeudeTyp).split(";");
                        gemueseProd += Integer.parseInt(eigenschaften[1]);
                        break;
                    case "L":
                        if (lvl != 0) {
                            eigenschaften = db.gibGebaeudeEigenschaften(lvl, gebaeudeTyp).split(";");
                            kap += Integer.parseInt(eigenschaften[2]);
                        }
                    default:
                        break;
                }
            }
        }
        int franchiseId = db.gibFranchiseId(spielerId);
        double prodBonus = db.gibBonus(spielerId, "produktionsbonus");
        double mehlProdBonus = 1;
        double fleischProdBonus = 1;
        double gemueseProdBonus = 1;
        if (franchiseId != 0){
            prodBonus += db.gibFranchiseBonus(franchiseId, "produktionsbonus");
            mehlProdBonus += db.gibFranchiseBonus(franchiseId, "mehlProduktionsbonus");
            fleischProdBonus += db.gibFranchiseBonus(franchiseId, "fleischProduktionsbonus");
            gemueseProdBonus += db.gibFranchiseBonus(franchiseId, "gemueseProduktionsbonus");
        }
        mehlProdBonus += db.gibBonus(spielerId, "mehlProduktionsbonus")+prodBonus;
        fleischProdBonus += db.gibBonus(spielerId, "fleischProduktionsbonus")+prodBonus;
        gemueseProdBonus += db.gibBonus(spielerId, "gemueseProduktionsbonus")+prodBonus;

        mehl += mehlProd*mehlProdBonus*updateLength;
        gemuese += gemueseProd*gemueseProdBonus*updateLength;
        fleisch += fleischProd*fleischProdBonus*updateLength;
        if(mehl > kap){
            mehl = kap;
        }
        if(fleisch > kap){
            fleisch = kap;
        }
        if(gemuese > kap){
            gemuese= kap;
        }
        db.aktualisiereRessourcen(standortId, fleisch, mehl, gemuese);
        String[][] alleEinheiten = db.gibAlleEinheiten(standortId);
        String einheitenString = "L1:0/L2:0/M1:0/M2:0/S1:0/S2:0/T1:0/T2:0/X1:0";
        String fraktion = db.gibFraktion(spielerId);
        if(alleEinheiten.length != 0) {
            alleEinheiten[6][0] = fraktion+"T1";
            alleEinheiten[7][0] = fraktion+"T2";
            alleEinheiten[8][0] = fraktion+"X1";
            einheitenString = erzeugeEinheitenString(alleEinheiten);
        }
        String[][] alleAusbildungseinheiten = {{fraktion + "L1", "0"},{fraktion + "L2", "0"},{fraktion + "M1", "0"},{fraktion + "M2", "0"},{fraktion + "S1", "0"},{fraktion + "S2", "0"},{fraktion + "T1", "0"},{fraktion + "T2", "0"},{fraktion + "X1", "0"}};

        int[] anzahlen = new int[9];
        anzahlen[0] = db.gibAnzahlBauendeEinheit(standortId, fraktion+"L1");
        anzahlen[1] = db.gibAnzahlBauendeEinheit(standortId, fraktion+"L2");
        anzahlen[2] = db.gibAnzahlBauendeEinheit(standortId, fraktion+"M1");
        anzahlen[3] = db.gibAnzahlBauendeEinheit(standortId, fraktion+"M2");
        anzahlen[4] = db.gibAnzahlBauendeEinheit(standortId, fraktion+"S1");
        anzahlen[5] = db.gibAnzahlBauendeEinheit(standortId, fraktion+"S2");
        anzahlen[6] = db.gibAnzahlBauendeEinheit(standortId, "T1");
        anzahlen[7] = db.gibAnzahlBauendeEinheit(standortId, "T2");
        anzahlen[8] = db.gibAnzahlBauendeEinheit(standortId, "X1");
        for (int i = 0; i < 9; i++){
            if (anzahlen[i] != -1){
                alleAusbildungseinheiten[i][1] = "" + anzahlen[i];
            }
        }
        String ausbildungsString = "L1:0/L2:0/M1:0/M2:0/S1:0/S2:0/T1:0/T2:0/X1:0";
        if (alleAusbildungseinheiten.length != 0){
            ausbildungsString = erzeugeEinheitenString(alleAusbildungseinheiten);
        }

        antwort += standortId + ";" + fleisch + ";" + mehl + ";" + gemuese + ";" + einheitenString + ";" + ausbildungsString;

        return antwort;
    }

    /**
     * Generiert den Einheiten String aus dem String[][] allEinheiten der aus der Datenbank generiert wird
     *
     * @param alleEinheiten String[][] mit allen Einheiten eines Standortes in Form: {L1, anzahl}{...,...}{X1, anzahl}
     * @return String in From: "L1:anzahl/.../X1:anzahl"
     */
    private String erzeugeEinheitenString(String[][] alleEinheiten){
        String[] einheiten = {"L1:0","L2:0","M1:0","M2:0","S1:0","S2:0","T1:0","T2:0", "X1:0"};
        String einheitenString = "";
        for (int i = 0; i < alleEinheiten.length; i++){
            String einheit = (alleEinheiten[i][0] + ":" + alleEinheiten[i][1]).substring(1);
            switch (einheit.substring(0,2)){
                case "L1":
                    einheiten[0] = einheit;
                    break;
                case "L2":
                    einheiten[1] = einheit;
                    break;
                case "M1":
                    einheiten[2] = einheit;
                    break;
                case "M2":
                    einheiten[3] = einheit;
                    break;
                case "S1":
                    einheiten[4] = einheit;
                    break;
                case "S2":
                    einheiten[5] = einheit;
                    break;
                case "T1":
                    einheiten[6] = einheit;
                    break;
                case "T2":
                    einheiten[7] = einheit;
                    break;
                case "X1":
                    einheiten[8] = einheit;
                    break;
            }
        }
        for (int i = 0; i < einheiten.length; i++){
            einheitenString += einheiten[i] + "/";
        }
        einheitenString = einheitenString.substring(0, einheitenString.length()-1);
        return einheitenString;
    }
}
