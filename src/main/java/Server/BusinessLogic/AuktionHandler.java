package Server.BusinessLogic;

import Server.Persistence.Database;

/**
 * Created by Jakob on 27.07.2016.
 * <p>
 * Klasse die sich um alle Aktionen zum Bereich Auktion behandelt, wie Auktion erstellen, loeschen, ausfuehren
 */
public class AuktionHandler {
    /**
     * Methode die eine Konkrete Clientanfrage bearbeitet
     *
     * @param toHandle {"AUKTION","ERSTELLE",spielerId, standortId, angebotsressource, anzahl, verlangteressource, anzahl}
     *                 {"AUKTION", "LOESCHE", auktionsId, spielerID}
     *                 {"AUKTION", "ABSCHLIESSEN", spielerId, auktionsId, standortId}
     * @return TRUE
     */
    public String[] handle(String[] toHandle) {
        if (!toHandle[0].equals("AUKTION")) {
            String[] antwort = {"FALSE"};
            return antwort;
        }
        Database db = Database.gibInstanz();
        String[] antwort = {"FALSE"};
        int spielerId;
        int standortId;
        int auktionsId;
        switch (toHandle[1]) {
            case "ERSTELLE":
                standortId = Integer.parseInt(toHandle[3]);
                spielerId = Integer.parseInt(toHandle[2]);
                String angebot = toHandle[4];
                int angebotAnzahl = Integer.parseInt(toHandle[5]);
                String nachfrage = toHandle[6];
                int nachfrageAnzahl = Integer.parseInt(toHandle[7]);
                db.erstelleAuktion(standortId, angebot, angebotAnzahl, nachfrage, nachfrageAnzahl);
                aktualisiereRessourcen(spielerId, standortId, angebot, (angebotAnzahl * (-1)));
                antwort[0] = "TRUE";
                return antwort;

            case "LOESCHE":
                auktionsId = Integer.parseInt(toHandle[2]);
                spielerId = Integer.parseInt(toHandle[3]);
                String[] eigenschaften = db.gibAuktionEigenschaften(auktionsId).split(";");
                int standortId1 = Integer.parseInt(eigenschaften[0]);
                String angebot1 = eigenschaften[1];
                int angebotAnzahl1 = Integer.parseInt(eigenschaften[2]);
                aktualisiereRessourcen(spielerId, standortId1, angebot1, angebotAnzahl1);
                db.loescheAuktion(auktionsId);
                antwort[0] = "TRUE";
                return antwort;

            case "ABSCHLIESSEN":
                return auktionAbschliessen(toHandle);
        }
        antwort[0] = "FALSE";
        return antwort;
    }

    /**
     * Methode die eine Auktion abschliesst. Sie kommt zum Einsatz, wenn ein Spieler eine Aktion ausfuehrt. Es werden dann die entsprechenden Ressourcen angepasst
     *
     * @param toHandle String[] mit allen notwendigen Informationen: {AUKTION", "ABSCHLIESSEN", spielerId, auktionsId, standortId}
     * @return {"TRUE"}
     */
    private String[] auktionAbschliessen(String[] toHandle) {
        Database db = Database.gibInstanz();
        int auktionsId = Integer.parseInt(toHandle[3]);
        String[] eigenschaften = db.gibAuktionEigenschaften(auktionsId).split(";");
        int standortKaeuferId = Integer.parseInt(toHandle[4]);
        int kaeuferId = Integer.parseInt(toHandle[2]);

        String nachfrage = eigenschaften[3];
        int nachfrageAnzahl = Integer.parseInt(eigenschaften[4]);
        String angebot = eigenschaften[1];
        int angebotAnzahl = Integer.parseInt(eigenschaften[2]);
        int standortAnbieterId = Integer.parseInt(eigenschaften[0]);
        int anbieterId = db.gibSpielerVonStandort(standortAnbieterId);
        //1.Schritt autkionsKauefer die angeboten Ressource draufrechnen
        aktualisiereRessourcen(kaeuferId, standortKaeuferId, angebot, angebotAnzahl);
        //2.Schritt auktionsKaeufer die nachgefaragte Ressource abziehen
        aktualisiereRessourcen(kaeuferId, standortKaeuferId, nachfrage, (nachfrageAnzahl * (-1)));
        //3.Schritt auktionsAnbieter die nachgefragte Ressource draufrechnen
        aktualisiereRessourcen(anbieterId, standortAnbieterId, nachfrage, nachfrageAnzahl);

        db.loescheAuktion(auktionsId);
        String[] antwort = {"TRUE"};
        return antwort;
    }

    /**
     * Methode um eine Ressource eines Spieler zu aktualisieren
     *
     * @param spielerId  Id des Spielers dessen Ressource aktualisiert werden soll
     * @param standortId Id des Standorts dessen Ressourcen aktualisiert werden soll
     * @param ressource  die Ressource die Aktualisiert werden soll
     * @param anzahl     die Menge um die sich die Ressource aendert
     */
    private void aktualisiereRessourcen(int spielerId, int standortId, String ressource, int anzahl) {
        Database db = Database.gibInstanz();
        int fleisch = db.gibFleisch(standortId);
        int gemuese = db.gibGemuese(standortId);
        int mehl = db.gibMehl(standortId);
        switch (ressource) {
            case "Geld":
                db.aktualisiereGeld(spielerId, (db.gibGeld(spielerId) + anzahl));
                break;
            case "Fleisch":
                db.aktualisiereRessourcen(standortId, (fleisch + anzahl), mehl, gemuese);
                break;
            case "Mehl":
                db.aktualisiereRessourcen(standortId, fleisch, (mehl + anzahl), gemuese);
                break;
            case "Gemuese":
                db.aktualisiereRessourcen(standortId, fleisch, mehl, (gemuese + anzahl));
                break;
        }
    }
}
