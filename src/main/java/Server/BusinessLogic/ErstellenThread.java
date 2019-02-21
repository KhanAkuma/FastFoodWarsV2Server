package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 30.07.2016.
 * <p>
 * Thread der laufen prueft ob  Gebaeude, Truppen, oder Forschungsauftraege fertiggestellt sind und wenn ja diese aktualisiert
 */
public class ErstellenThread extends Thread {

    private boolean sollLaufen;

    public ErstellenThread(boolean sollLaufen) {
        super("ErstellenThread");
        this.sollLaufen = sollLaufen;
    }

    /**
     * Wenn gestartet, prueft der Thread laufend nach fertiggestellten Gebaeuden, Truppen und Forschungsauftraegen.
     * Wenn etwas fertig ist, wird es entsprechend in der Datenbank aktualisiert und aus den Erstellenlisten geloescht
     */
    public void run() {
        Database db = Database.gibInstanz();

        while (sollLaufen) {
            String gebaeudeIds = db.gibAlleFertigenGebaeude();

            if (!gebaeudeIds.equals("")) {
                String[] ids = gebaeudeIds.split(";");
                for (String id : ids) {
                    db.aufleveleGebaeude(Integer.parseInt(id));
                    String[] eigenschaften = db.gibGebaeude(Integer.parseInt(id)).split(";");
                    if (eigenschaften[2].contains("D")) {
                        int spielerId = db.gibSpielerVonStandort(Integer.parseInt(eigenschaften[0]));
                        double lvl = Integer.parseInt(eigenschaften[3]);
                        db.aktualisiereBonus(spielerId, "forschungsKostenbonus", 0.01 * lvl + db.gibBonus(spielerId, "forschungsKostenbonus"));
                        db.aktualisiereBonus(spielerId, "forschungsGeschwindigkeitsbonus", 0.01 * lvl + db.gibBonus(spielerId, "forschungsGeschwindigkeitsbonus"));
                    }
                }
            }
            String[] fertigeEinheiten = db.gibAlleFertigenEinheiten();

            for (String einheit : fertigeEinheiten) {
                String[] eigenschaften = einheit.split(";");
                int anzahl = Integer.parseInt(eigenschaften[2]);
                long fertigstellung = Long.parseLong(eigenschaften[3]);
                int anzahlFertigerEinheiten = 0;
                int standortId = Integer.parseInt(eigenschaften[0]);
                String typ = eigenschaften[1];
                while (anzahl >= 1 && fertigstellung < System.currentTimeMillis() / 1000) {
                    anzahl--;
                    anzahlFertigerEinheiten++;
                    fertigstellung += Long.parseLong(db.gibEinheitenEigenschaften(typ).split(";")[5]);
                }
                db.aktualisiereEinheit(standortId, typ, anzahlFertigerEinheiten);
                if (anzahl == 0) {
                    db.loescheBauendeEineheit(standortId, typ);
                } else {
                    db.aktualisiereBauendeEineheit(standortId, typ, anzahl, fertigstellung);
                }
            }

            String auftragsIds = "";

            auftragsIds = db.gibAlleFertigenForschungsAuftrage();

            if (!auftragsIds.equals("")) {
                String[] ids = auftragsIds.split(";");
                for (String id : ids) {
                    String eigenschaften[] = db.gibForschungsAuftragEigenschaften(Integer.parseInt(id)).split(";");
                    String baumArt = eigenschaften[0];
                    String baum;
                    int baumId = Integer.parseInt(eigenschaften[1]);
                    char knoten = eigenschaften[2].charAt(0);
                    String[] effekt;
                    switch (baumArt.substring(0, 1)) {
                        case "F":
                            effekt = db.gibKnotenEffekt(baumArt, knoten).split(";");
                            if (!effekt[1].equals("-1")) {
                                double wert = (Integer.parseInt(effekt[1]));
                                wert = wert / 1000;
                                db.aktualisiereBonus(baumId, effekt[0], wert);
                            }
                            baum = db.gibFraktionBaum(baumId);
                            baum = baum.replace(knoten + "2", knoten + "1");
                            db.aktualisiereFraktionsBaum(baumId, baum);
                            break;
                        case "G":
                            effekt = db.gibKnotenEffekt(baumArt, knoten).split(";");
                            if (!effekt[1].equals("-1")) {
                                double wert = ((double) Integer.parseInt(effekt[1]));
                                wert = wert / 1000;
                                db.aktualisiereFranchiseBonus(baumId, effekt[0], wert);
                            }
                            baum = db.gibFranchiseBaum(baumId);
                            baum = baum.replace(knoten + "2", knoten + "1");
                            db.aktualisiereFranchiseBaum(baumId, baum);
                            break;
                        case "C":
                            effekt = db.gibKnotenEffekt(baumArt, knoten).split(";");
                            if (!effekt[1].equals("-1")) {
                                double wert = Integer.parseInt(effekt[1]);
                                wert = wert / 1000;
                                db.aktualisiereBonus(baumId, effekt[0], wert);
                            }
                            baum = db.gibCharakterBaum(baumId);
                            baum = baum.replace(knoten + "2", knoten + "1");
                            db.aktualisiereCharakterbaum(baumId, baum);
                            break;
                    }
                    db.loescheForschungsAuftrag(Integer.parseInt(id));
                }
            }
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getSollLaufen() {
        return sollLaufen;
    }

    public void setSollLaufen(boolean sollLaufen) {
        this.sollLaufen = sollLaufen;
    }

}
