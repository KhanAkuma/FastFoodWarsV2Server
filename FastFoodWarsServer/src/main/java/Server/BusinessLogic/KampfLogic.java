package Server.BusinessLogic;

import Server.Persistence.Database;


/**
 * Created by Jakob on 02.08.2016.
 * <p>
 * Klasse die die gesamte Logik des Kaempfens zweier Armeen beinhaltet
 */
public class KampfLogic {

    private Database db = Database.gibInstanz();

    /**
     * Methode, die bei einer UEBERFALL Bewegung die beiden Armeen gegeneinander kaempfen laesst. Ist der Angreifer der
     * Gewinner, verliert der Verteidiger alle seine Truppen, so wie so viele Ressourcen wie der Angreifer an LKWs
     * mitgeschickt hat, allerdings nie mehr als der Verteidiger an Ressourcen hat. Der Angreifer bekommt die Ressourcen,
     * sowie einen Anteil seiner Truppen zurueck. Ausserdem hat er die Chance Gutscheine zu finden.
     * Ist der Verteidiger der Gewinner, verliert der Angreifer alle seine Truppen.
     * Der Gewinner verliert solange Truppen bis der Verteidigungswert der verlorenen Truppen >= des Angriffswertes des
     * Verlierers ist, wobei der Verteidiger die starken Truppen zuerst verliert.
     *
     * @param angreiferId   int Id des Angreifers
     * @param angreifer     String der Truppen des Angreifers in Form "L1:anzahl/.../X1:anzahl
     * @param verteidiger   String[][] der Truppen des Verteidigers in Form:{L1, anzahl}{...,...}{X1, anzahl}
     * @param verteidigerId int Id des Verteidiger
     * @param startId       int Id des Standortes von dem die angreifenden Truppen kommen
     * @param zielId        int Id des Standortes der angegriffen wird
     */
    public void kaempfe(int angreiferId, String angreifer, String[][] verteidiger, int verteidigerId, int startId, int zielId) {
        Database db = Database.gibInstanz();
        double angriffswert = 0;
        double verteidigungswert = 0;
        for (int i = 0; i < verteidiger.length; i++) {
            int anzahl = Integer.parseInt(verteidiger[i][1]);
            if (anzahl > 0) {
                int verteidigungsWertEinheit = Integer.parseInt(db.gibEinheitenEigenschaften(verteidiger[i][0]).split(";")[4]);
                verteidigungswert += anzahl * verteidigungsWertEinheit;
            }
        }

        String[] angreiferArray = angreifer.split("/");
        String angreiferFraktion = db.gibFraktion(angreiferId);
        for (int i = 0; i < angreiferArray.length; i++) {
            String[] truppe = angreiferArray[i].split(":");
            int anzahl = Integer.parseInt(truppe[1]);
            if (anzahl > 0 && !truppe[0].contains("T")) {
                String eigenschaften = db.gibEinheitenEigenschaften(angreiferFraktion + truppe[0]);
                int angriffsWertEinheit = Integer.parseInt(eigenschaften.split(";")[3]);
                angriffswert += angriffsWertEinheit * anzahl;
            }
        }

        double angriffswertMitBoni = angriffswert * (1 + db.gibBonus(angreiferId, "truppenAngriffsbonus"));
        double verteidigungswertMitBoni = verteidigungswert * (1 + db.gibBonus(verteidigerId, "truppenVerteidigungsbonus")) + db.gibBonus(verteidigerId, "verteidigungsbonus");
        boolean erfolgreich = false;
        if (angriffswertMitBoni > verteidigungswertMitBoni) {
            erfolgreich = true;
        }
        if (erfolgreich) {
            db.loescheAlleEinheiten(zielId);
            int[] angreiferTruppen = new int[9];

            for (int i = 0; i < 9; i++) {
                angreiferTruppen[i] = Integer.parseInt(angreiferArray[i].split(":")[1]);
            }
            int kap = angreiferTruppen[7] * Integer.parseInt(db.gibEinheitenEigenschaften("T2").split(";")[6]);
            angreiferTruppen = aktualisiereTruppen(angreiferTruppen, verteidigungswert, angreiferFraktion, angreiferArray);
            int gepluendertMehl = kap / 3;
            int gepluendertFleisch = kap / 3;
            int gepluendertGemuese = kap / 3;

            int verteidigerMehl = db.gibMehl(zielId);
            int verteidigerFleisch = db.gibFleisch(zielId);
            int verteidigerGemuese = db.gibGemuese(zielId);

            int angreiferMehl = db.gibMehl(startId);
            int angreiferFleisch = db.gibFleisch(startId);
            int angreiferGemuese = db.gibGemuese(startId);
            if (verteidigerMehl < gepluendertMehl) {
                gepluendertMehl = verteidigerMehl;
            }
            if (verteidigerFleisch < gepluendertFleisch) {
                gepluendertFleisch = verteidigerFleisch;
            }
            if (verteidigerGemuese < gepluendertGemuese) {
                gepluendertGemuese = verteidigerGemuese;
            }
            db.aktualisiereRessourcen(zielId, verteidigerFleisch - gepluendertFleisch, verteidigerMehl - gepluendertMehl, verteidigerGemuese - gepluendertGemuese);
            db.aktualisiereRessourcen(startId, angreiferFleisch + gepluendertFleisch, angreiferMehl + gepluendertMehl, angreiferGemuese + gepluendertGemuese);

            db.aktualisiereEinheit(startId, angreiferFraktion + "L1", angreiferTruppen[0]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "L2", angreiferTruppen[1]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "M1", angreiferTruppen[2]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "M2", angreiferTruppen[3]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "S1", angreiferTruppen[4]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "S2", angreiferTruppen[5]);
            db.aktualisiereEinheit(startId, "T1", angreiferTruppen[6]);
            db.aktualisiereEinheit(startId, "T2", angreiferTruppen[7]);
            db.aktualisiereEinheit(startId, "X1", angreiferTruppen[8]);
            generiereGutscheine(angreiferId);
            NachrichtenHandler nh = new NachrichtenHandler();
            String nachricht = "Gib mir meine " + gepluendertFleisch + " Fleisch, \n" + gepluendertGemuese + " Gemuese, " + gepluendertMehl + " Mehl zurueck!";
            String[] nachrichtInfos = {"NACHRICHT", "" + verteidigerId, "" + angreiferId, nachricht};
            nh.handle(nachrichtInfos);
        } else {
            verteidigerGewinnt(verteidiger, angriffswert, verteidigerId, angreiferId);
        }
    }

    /**
     * Methode die bei einer UEBERNAHME Bewegung die beiden Armeen gegeneinander kaempfen laesst. Ist der Angreifer der
     * Gewinner, gewinnt er den angegriffenen Standort. Allerdings zerstoert der Verlierer noch alle Gebaeude ausser das Hauptgebaeude,
     * sodass der Gewinner einen komplett leeren Standort bekommt. Der Verteidiger verliert dementsprechend seinen Standort
     * und ist, wenn es sein letzter Standort war, spielerisch "TOT". Zudem kann der Angreifer Gutscheine finden.
     * Ist der Verteidiger der Gewinner verliert der Angreifer seine Truppen. So oder so verliert der Angreifer,
     * das Geld was zur UEBERNAHME benoetigt wird.
     * Der Gewinner verliert solange Truppen bis der Verteidigungswert der verlorenen Truppen >= des Angriffswertes des
     * Verlierers ist, wobei der Verteidiger die starken Truppen zuerst verliert.
     *
     * @param angreiferId   int Id des Angreifers
     * @param angreifer     String der Truppen des Angreifers in Form "L1:anzahl/.../X1:anzahl
     * @param verteidiger   String[][] der Truppen des Verteidigers in Form:{L1, anzahl}{...,...}{X1, anzahl}
     * @param verteidigerId int Id des Verteidiger
     * @param startId       int Id des Standortes von dem die angreifenden Truppen kommen
     * @param zielId        int Id des Standortes der angegriffen wird
     */
    public void uebernehme(int angreiferId, String angreifer, String[][] verteidiger, int verteidigerId, int startId, int zielId) {
        db = Database.gibInstanz();
        double angriffswert = 0;
        double verteidigungswert = 0;
        for (int i = 0; i < verteidiger.length; i++) {
            int anzahl = Integer.parseInt(verteidiger[i][1]);
            if (anzahl > 0) {
                int verteidigungsWertEinheit = Integer.parseInt(db.gibEinheitenEigenschaften(verteidiger[i][0]).split(";")[4]);
                verteidigungswert += anzahl * verteidigungsWertEinheit;
            }
        }

        String[] angreiferArray = angreifer.split("/");
        String angreiferFraktion = db.gibFraktion(angreiferId);
        for (int i = 0; i < angreiferArray.length; i++) {
            String[] truppe = angreiferArray[i].split(":");
            int anzahl = Integer.parseInt(truppe[1]);
            if (anzahl > 0 && !truppe[0].contains("T")) {
                String eigenschaften = db.gibEinheitenEigenschaften(angreiferFraktion + truppe[0]);
                int angriffsWertEinheit = Integer.parseInt(eigenschaften.split(";")[3]);
                angriffswert += angriffsWertEinheit * anzahl;
            }
        }
        double angriffswertMitBoni = angriffswert * (1 + db.gibBonus(angreiferId, "truppenAngriffsbonus"));
        double verteidigungswertMitBoni = verteidigungswert * (1 + db.gibBonus(verteidigerId, "truppenVerteidigungsbonus")) + db.gibBonus(verteidigerId, "verteidigungsbonus");
        boolean erfolgreich = false;
        if (angriffswertMitBoni > verteidigungswertMitBoni) {
            erfolgreich = true;
        }
        if (erfolgreich) {
            int[] angreiferTruppen = new int[9];

            for (int i = 0; i < 9; i++) {
                angreiferTruppen[i] = Integer.parseInt(angreiferArray[i].split(":")[1]);
            }
            angreiferTruppen = aktualisiereTruppen(angreiferTruppen, verteidigungswert, angreiferFraktion, angreiferArray);
            db.uebernehmeStandort(verteidigerId, angreiferId);
            db.aktualisiereEinheit(startId, angreiferFraktion + "L1", angreiferTruppen[0]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "L2", angreiferTruppen[1]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "M1", angreiferTruppen[2]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "M2", angreiferTruppen[3]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "S1", angreiferTruppen[4]);
            db.aktualisiereEinheit(startId, angreiferFraktion + "S2", angreiferTruppen[5]);
            db.aktualisiereEinheit(startId, "T1", angreiferTruppen[6]);
            db.aktualisiereEinheit(startId, "T2", angreiferTruppen[7]);
            db.aktualisiereEinheit(startId, "X1", angreiferTruppen[8]);
            generiereGutscheine(angreiferId);
            NachrichtenHandler nh = new NachrichtenHandler();
            String nachricht = "Oh nein! Hast du sonst nix zu tun? \n Tja was solls, mein Standort gehoert dir.";
            String[] nachrichtInfos = {"NACHRICHT", "" + verteidigerId, "" + angreiferId, nachricht};
            nh.handle(nachrichtInfos);

        } else {
            verteidigerGewinnt(verteidiger, angriffswert, verteidigerId, angreiferId);
        }
    }

    /**
     * Methode die ermittelt, wie viele Truppen der Gewinner eines Kampfes verliert und die Armee aktualisiert zurueck gibt
     *
     * @param gewinner          int[] der siegreichen Armee in Form (anzahl1, ... , anzahlX1)
     * @param verteidigungswert double Verteidigungswert der unterlegenen Armee
     * @param fraktion          String Fraktion der siegreichen Armee: "K", "M", oder "P"
     * @param gewinnerArray     String[] der siegreichen Armee in Form {L1, ... , X1}
     * @return
     */
    private int[] aktualisiereTruppen(int[] gewinner, double verteidigungswert, String fraktion, String[] gewinnerArray) {
        int index = 8;
        int[] truppen = gewinner;
        double verteidigungsWert = verteidigungswert;
        while (verteidigungsWert > 0) {
            int anzahl = truppen[index];
            String typ = fraktion + gewinnerArray[index].split(":")[0];
            if (typ.contains("T") || typ.contains("X")) {
                typ = typ.substring(1);
            }
            if (anzahl == 0) {
                if (index == 8) {
                    index = 5;
                } else {
                    index--;
                }
            } else {
                int angriff = Integer.parseInt(db.gibEinheitenEigenschaften(typ).split(";")[3]);
                verteidigungsWert -= angriff;
                truppen[index] = anzahl - 1;
            }

        }
        return truppen;
    }

    /**
     * Methode die bei Sieg des Verteidigers seine Truppen aktualisiert, sowie eine Nachricht an der Verlierer schickt
     *
     * @param verteidiger   String[][] der Truppen des Verteidigers in Form:{L1, anzahl}{...,...}{X1, anzahl}
     * @param angriffswert  double Angriffswert der angreifenden Armee
     * @param verteidigerId int Id des verteidigenden Spielers
     * @param angreiferId   int Id des angreifenden Spielers
     */
    private void verteidigerGewinnt(String[][] verteidiger, double angriffswert, int verteidigerId, int angreiferId) {
        int[] gewinner = new int[9];
        for (int i = 0; i < gewinner.length; i++) {
            gewinner[i] = Integer.parseInt(verteidiger[i][1]);
        }
        String verteidigerFraktion = db.gibFraktion(verteidigerId);
        String[] verteiderigerArray = {"L1:0", "L2:0", "M1:0", "M2:0", "S1:0", "S2:0", "T1:0", "T2:0", "X1:0"};

        NachrichtenHandler nh = new NachrichtenHandler();
        String nachricht = "Ha, dein Angriff war ja mal laecherlich! \n Versuch es ruhig noch mal";
        String[] nachrichtInfos = {"NACHRICHT", "" + verteidigerId, "" + angreiferId, nachricht};
        nh.handle(nachrichtInfos);

        aktualisiereTruppen(gewinner, angriffswert, verteidigerFraktion, verteiderigerArray);
    }

    /**
     * Methode die den Wert ermittelt, den ein Standort hat, dabei werden die Level aller Gebaeude des Standortes mit ihren
     * Aufwertungskosten multipliziert und zusammen addiert.
     *
     * @param standortId int Id des Standortes dessen Wert ermittelt werden soll
     * @return int Wert des Standortes
     */
    public int ermittleStandortWert(int standortId) {
        String[] gebaeudeIds = db.gibGebaeudeIds(standortId).split(";");
        int wert = 0;
        for (String id : gebaeudeIds) {
            int gId = Integer.parseInt(id);
            String[] gebaeude = db.gibGebaeude(gId).split(";");
            int lvl = Integer.parseInt(gebaeude[3]);
            if (lvl == 0) {
                lvl++;
            }
            String[] eigenschaften = db.gibGebaeudeEigenschaften(lvl, gebaeude[2]).split(";");
            wert += lvl * Integer.parseInt(eigenschaften[0]);
        }
        String[] eigenschaften = db.gibGebaeudeEigenschaften(db.gibHauptgebaeude(standortId), "H").split(";");
        wert += Integer.parseInt(eigenschaften[0]);

        return wert; //TODO wert;
    }

    /**
     * Methode, die mit einer Wahrscheinlichkeit von 10% einen Gutscheine fuer einen Spieler generiert
     *
     * @param spielerId int Id des Spielers fuer den ein Gutschein generiert werden soll
     * @return true
     */
    private boolean generiereGutscheine(int spielerId) {
        if (0.1 > Math.random()) {
            db.aktualisiereGutscheine(spielerId, db.gibGutscheine(spielerId) + 1);
        }
        return true;
    }
}
