package Server.BusinessLogic;

import Server.Persistence.Database;


/**
 * Created by Jakob on 27.07.2016.
 * <p>
 * Klasse die alle Anfrageauftraege des Client bearbeitet. Unter Anfragen werden nur informative Anfragen verstanden, hier werden keine Datenbankdaten veraendert
 */
public class AnfrageHandler {

    private Database db = Database.gibInstanz();

    /**
     * Die Methode interpretiert die einkommende Anfrage und gibt entsprechende Antwort
     *
     * @param toHandle String[] in Form: {"ANFRAGE", anfrageArt, weitere Infos die benoetigt werden}
     * @return String[] mit der Anwort auf die Anfrage
     */
    public String[] handle(String[] toHandle) {
        String[] antwort = {"FALSE", ""};
        int spielerId;
        switch (toHandle[1]) {
            case "INFOBAUM":
                String baum = toHandle[3];
                if (baum.equals("F")) {
                    char knoten = toHandle[4].charAt(0);
                    String fraktion = toHandle[5];
                    int preis = db.gibFraktionKnotenPreis(knoten, fraktion);
                    antwort[0] = "TRUE";
                    antwort[1] = Integer.toString(preis);
                    return antwort;
                } else if (baum.equals("G")) {
                    char knoten = toHandle[4].charAt(0);
                    int preis = db.gibFranchiseKnotenPreis(knoten);
                    antwort[0] = "TRUE";
                    antwort[1] = Integer.toString(preis);
                    return antwort;
                } else if (baum.equals("C")) {
                    char knoten = toHandle[4].charAt(0);
                    String charakter = toHandle[5];
                    int preis = db.gibCharakterKnotenPreis(knoten, charakter);
                    antwort[0] = "TRUE";
                    antwort[1] = Integer.toString(preis);
                    return antwort;
                }
                return antwort;

            case "NACHRICHTENIDS":
                spielerId = Integer.parseInt(toHandle[2]);
                String[] antwort1 = {"TRUE", db.gibVersendeteNachrichten(spielerId), db.gibEmpfangeneNachrichten(spielerId)};
                return antwort1;

            case "AUKTIONSIDS":
                antwort[0] = "TRUE";
                antwort[1] = db.gibAuktionIds();
                return antwort;

            case "EXISTSPIELER":
                if (db.gibSpielerId(toHandle[2]) == -1) {
                    antwort[0] = "FALSE";
                    return antwort;
                } else {
                    antwort[0] = "TRUE";
                    return antwort;
                }

            case "GEBAEUDEKOSTEN":
                String typ = toHandle[2].substring(0, 2);
                int lvl = Integer.parseInt(toHandle[2].substring(2, 3));
                int preis = Integer.parseInt(db.gibGebaeudeEigenschaften(lvl, typ).split(";")[0]);
                String[] antwort2 = {"TRUE", Integer.toString(preis)};
                return antwort2;

            case "STANDORTKAP":
                int standortId = Integer.parseInt(toHandle[2]);
                int kap = 0;
                String idsString = db.gibGebaeudeIds(standortId);
                String[] ids = idsString.split(";");
                for (String id : ids) {
                    String[] eigenschaften = db.gibGebaeude(Integer.parseInt(id)).split(";");
                    if (eigenschaften[2].contains("L") && !Boolean.parseBoolean(eigenschaften[4])) {
                        kap += Integer.parseInt(db.gibGebaeudeEigenschaften(Integer.parseInt(eigenschaften[3]), eigenschaften[2]).split(";")[2]);
                    }
                }
                String[] antwort3 = {"TRUE", Integer.toString(kap)};
                return antwort3;

            case "KARTE":
                return generiereKartenString(Integer.parseInt(toHandle[2]), Integer.parseInt(toHandle[3]));

            case "FERTIGSTELLUNG":
                if (toHandle[2].equals("GEBAEUDE")) {
                    long fertigstellung = Long.parseLong(db.gibGebaeude(Integer.parseInt(toHandle[3])).split(";")[5]);
                    String[] antwort4 = {"TRUE", Long.toString(fertigstellung)};
                    return antwort4;
                }
                return antwort;

            case "TRUPPENEIGENSCHAFTEN":
                return truppenEigenschaften(toHandle[2]);

            case "GEBAEUDEEIGENSCHAFTEN":
                return gebaeudeEigenschaften(toHandle[2]);

            case "ZIEL":
                int standortId1 = Integer.parseInt(toHandle[2]);
                String[] antwort4 = {"TRUE", db.gibStandortName(standortId1), db.gibBenutzerName(db.gibSpielerVonStandort(standortId1))};
                return antwort4;

            case "GIBBAEUME":
                spielerId = Integer.parseInt(toHandle[2]);
                String cBaum = db.gibCharakterBaum(spielerId);
                String fBaum = db.gibFraktionBaum(spielerId);
                String gBaum = "";
                int franchiseId = db.gibFranchiseId(spielerId);
                if (franchiseId != -1) {
                    gBaum = db.gibFranchiseBaum(franchiseId);
                }
                String[] antwort5 = {"TRUE", cBaum, fBaum, gBaum};
                return antwort5;

            case "STANDORTWERT":
                KampfLogic kampfLogic = new KampfLogic();
                String[] antwort6 = {"" + kampfLogic.ermittleStandortWert(Integer.parseInt(toHandle[2]))};
                return antwort6;

            default:
                String[] defaultantwort = {"Diese Anfrage kann ich nicht beantworten."};
                return defaultantwort;
        }
    }

    /**
     * Laedt die alle Eigenschaften aller Truppenarten einer Fraktion aus der Datenbank
     *
     * @param fraktion ein String der die Fraktion ("K", "P", "M") darstellt
     * @return gibt einen String[] zurueck in dem alle Eigenschaften nach Truppen sortiert stehen
     */
    private String[] truppenEigenschaften(String fraktion) {
        String l1 = db.gibEinheitenEigenschaften(fraktion + "L1");
        String l2 = db.gibEinheitenEigenschaften(fraktion + "L2");
        String m1 = db.gibEinheitenEigenschaften(fraktion + "M1");
        String m2 = db.gibEinheitenEigenschaften(fraktion + "M2");
        String s1 = db.gibEinheitenEigenschaften(fraktion + "S1");
        String s2 = db.gibEinheitenEigenschaften(fraktion + "S2");
        String t1 = db.gibEinheitenEigenschaften("T1");
        String t2 = db.gibEinheitenEigenschaften("T2");
        String x1 = db.gibEinheitenEigenschaften("X1");
        String[] antwort = {l1, l2, m1, m2, s1, s2, t1, t2, x1};
        return antwort;
    }

    /**
     * Laedt die alle Eigenschaften aller Gebaeudearten einer Fraktion aus der Datenbank
     *
     * @param fraktion ein String der die Fraktion ("K", "P", "M") darstellt
     * @return gibt einen String[] zurueck in dem alle Eigenschaften nach Gebaeude sortiert stehen
     */
    private String[] gebaeudeEigenschaften(String fraktion) {
        String h1 = db.gibGebaeudeEigenschaften(1, "H");
        String h2 = db.gibGebaeudeEigenschaften(2, "H");
        String h3 = db.gibGebaeudeEigenschaften(3, "H");
        String b1 = db.gibGebaeudeEigenschaften(1, fraktion + "B");
        String b2 = db.gibGebaeudeEigenschaften(2, fraktion + "B");
        String b3 = db.gibGebaeudeEigenschaften(3, fraktion + "B");
        String m1 = db.gibGebaeudeEigenschaften(1, fraktion + "M");
        String m2 = db.gibGebaeudeEigenschaften(2, fraktion + "M");
        String m3 = db.gibGebaeudeEigenschaften(3, fraktion + "M");
        String s1 = db.gibGebaeudeEigenschaften(1, fraktion + "S");
        String s2 = db.gibGebaeudeEigenschaften(2, fraktion + "S");
        String s3 = db.gibGebaeudeEigenschaften(3, fraktion + "S");
        String l1 = db.gibGebaeudeEigenschaften(1, fraktion + "L");
        String l2 = db.gibGebaeudeEigenschaften(2, fraktion + "L");
        String l3 = db.gibGebaeudeEigenschaften(3, fraktion + "L");
        String r1 = db.gibGebaeudeEigenschaften(1, fraktion + "R");
        String r2 = db.gibGebaeudeEigenschaften(2, fraktion + "R");
        String r3 = db.gibGebaeudeEigenschaften(3, fraktion + "R");
        String d1 = db.gibGebaeudeEigenschaften(1, fraktion + "D");
        String d2 = db.gibGebaeudeEigenschaften(2, fraktion + "D");
        String d3 = db.gibGebaeudeEigenschaften(3, fraktion + "D");
        String p1 = db.gibGebaeudeEigenschaften(1, fraktion + "P");
        String p2 = db.gibGebaeudeEigenschaften(2, fraktion + "P");
        String p3 = db.gibGebaeudeEigenschaften(3, fraktion + "P");
        String a1 = db.gibGebaeudeEigenschaften(1, fraktion + "A");
        String a2 = db.gibGebaeudeEigenschaften(2, fraktion + "A");
        String a3 = db.gibGebaeudeEigenschaften(3, fraktion + "A");
        String[] antwort = {h1, h2, h3, b1, b2, b3, m1, m2, m3, s1, s2, s3, l1, l2, l3, r1, r2, r3, d1, d2, d3, p1, p2, p3, a1, a2, a3};
        return antwort;
    }

    /**
     * Erzeugt einen String[], der die Karte um einen Standort darstellt. Jeder Eintrag stellt eine x-Koordinatenreihe dar und ist genau 9 Koordinaten lang.
     * Die Zahlen stellen die Standorte dar: 0=kein Standort, >0 = standort mit der entsprechenden Id
     *
     * @param zentrumX X-Koordinate um den die Karte generiert werden soll
     * @param zentrumY Y-Koordinate um den die Karte generiert werden soll
     * @return String[] in Form {0;0;0;0;0;0;0;0;0,...,0;0;0;0;0;0;0;0;0}
     */
    private String[] generiereKartenString(int zentrumX, int zentrumY) {
        int mapGroesse = 9;
        int[][] map = new int[mapGroesse][mapGroesse];
        for (int i = 0; i < mapGroesse; i++) {
            for (int j = 0; j < mapGroesse; j++) {
                map[i][j] = 0;
            }
        }
        String[] standortIds = db.gibAlleStandortIds().split(";");
        for (String id : standortIds) {
            int sId = Integer.parseInt(id);
            int x = db.gibXKoordinate(sId);
            int y = db.gibYKoordinate(sId);
            if (x == zentrumX && y == zentrumY) {
                map[4][4] = sId;
            } else {
                if (zentrumX + 4 >= x && x >= zentrumX - 4) {
                    if (zentrumY + 4 >= y && y >= zentrumY - 4) {
                        map[y - (zentrumY - 4)][x - (zentrumX - 4)] = sId;
                    }
                }
            }


        }
        String[] antwort = new String[mapGroesse + 1];
        antwort[0] = "TRUE";
        for (int i = 0; i < mapGroesse; i++) {
            String zeile = "";
            for (int j = 0; j < mapGroesse; j++) {
                zeile += map[i][j] + ";";
            }
            antwort[i + 1] = zeile.substring(0, zeile.length() - 1);
        }

        return antwort;
    }
}
