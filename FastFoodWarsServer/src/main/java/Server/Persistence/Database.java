package Server.Persistence;

import java.sql.*;

public class Database
{
    //Wichtige Variablen der Datenbank.

    private Connection c = null;
    private Statement stmt = null;
    private static Database instance = new Database();

    //Methoden zur Erstellung aus Ausgabe der Datenbank.

    /**
     * privater Konstrukor der Datenbank.
     */
    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:FastFoodWars-Datenbank.db3");
            initialisiere();
        } catch (Exception e) {
        }
    }

    /**
     * Singleton Instanz-Geber der Datenbank.
     *
     * @return Das Datenbankobjekt.
     */
    public static Database gibInstanz() {
        return Database.instance;
    }

    /**
     * Abfrage, ob die Datenbasis bereits erstellt wurde.
     *
     * @return true, falls ja, false sonst
     */
    private boolean istDatenbankErstellt() {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `einstellungen`;";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                rs.close();
                return true;
            } else {
                rs.close();
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Erstellt die Datenbasis, auf der das Spiel laeuft, sollte diese noch nicht vorhanden sein.
     */
    public synchronized void initialisiere() {
        if(!istDatenbankErstellt()) {
            erstelleTabellen();
            erstelleGebaeude();
            erstelleEinheiten();
            erstelleBaeume();

            int kartenGroesse = 0;

            try {
                stmt = c.createStatement();
                String sql = "INSERT INTO `einstellungen` VALUES (9, 4444);";
                stmt.executeUpdate(sql);
            } catch (Exception e) {
            }

            try {
                stmt = c.createStatement();
                String sql = "SELECT `kartenGroesse` FROM `einstellungen`;";
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    kartenGroesse = rs.getInt("kartenGroesse");
                }
            } catch (Exception e) {
            }

            fuelleKarte(kartenGroesse);
        }
    }

    /**
     * Erstellt die Tabellenbasis fuer die Datenbank.
     *
     * @return true, falls fehlerfrei erstellt, false sonst.
     */
    private boolean erstelleTabellen() {
        try {
            stmt = c.createStatement();
            String sql;

            //Einstellungen Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `einstellungen` (`kartenGroesse` integer NOT NULL, `port` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //Benutzer Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `benutzer` (`id` integer primary key autoincrement, `name` varchar(64) NOT NULL, `passwort` varchar(256) NOT NULL, " +
                    "`fraktion` integer, `geld` integer(16), `gutschein` integer(16), `charakter` varchar(1), `franchiseId` integer, `urlaubsmodus` boolean, `darfForschen` boolean, " +
                    "`letzteAktion` integer);";
            stmt.executeUpdate(sql);

            //Standort Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `standort` (`id` integer primary key autoincrement, `spielerId` integer NOT NULL, `name` varchar(64) NOT NULL, `hauptgebaeude` integer(8) NOT NULL, " +
                    "`x` integer(8) NOT NULL, `y` integer(8) NOT NULL, `mehl` integer, `fleisch` integer, `gemuese` integer, `verteidigung` REAL);";
            stmt.executeUpdate(sql);

            //Gebaeude Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `gebaeude` (`id` integer primary key autoincrement, `standortId` integer NOT NULL, `platz` integer NOT NULL," +
                    "`typ` integer NOT NULL, `level` integer NOT NULL, `imBau` boolean NOT NULL, `fertigstellung` long NOT NULL);";
            stmt.executeUpdate(sql);

            //Einheiten-Bauliste Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `einheitBauliste` (`standortId` integer NOT NULL, `typ` varchar(8) NOT NULL, " +
                    "`anzahl` integer NOT NULL, `fertigstellung` integer NOT NULL, primary key(`standortId`, `typ`));";
            stmt.executeUpdate(sql);

            //Nachrichten Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `nachricht` (`id` integer primary key autoincrement, `sender` integer NOT NULL, `empfaenger` integer NOT NULL," +
                    "`nachricht` varchar(256), `datum` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //FraktionsBaum Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `fraktionBaum` (`id` integer primary key autoincrement, `spielerId` integer NOT NULL, `baum` VARCHAR(128));";
            stmt.executeUpdate(sql);

            //CharakterBaum Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `charakterBaum` (`id` integer primary key autoincrement, `spielerId` integer NOT NULL, `baum` VARCHAR(128));";
            stmt.executeUpdate(sql);

            //Karte Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `karte` (`id` integer primary key autoincrement, `x`integer NOT NULL, `y` integer NOT NULL, `belegt` boolean NOT NULL);";
            stmt.executeUpdate(sql);

            //Bonus Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `boni` (`id` integer primary key autoincrement, `spielerId` integer NOT NULL,`franchiseId` integer NOT NULL,  `verteidigungsbonus` real, `produktionsbonus` real, `geldbonus` real," +
                    "`mehlProduktionsbonus` real, `fleischProduktionsbonus` real, `gemueseProduktionsbonus` real, `truppenGeschwindigkeitsbonus` real," +
                    "`truppenKostenbonus` real, `truppenAngriffsbonus` real, `truppenVerteidigungsbonus` real, `forschungsGeschwindigkeitsbonus` real," +
                    "`forschungsKostenbonus` real);";
            stmt.executeUpdate(sql);

            //Gebaeudestufen
            sql = "CREATE TABLE IF NOT EXISTS `gebaeudeStufen` (`level` integer NOT NULL, `typ` integer NOT NULL, `geldAufstiegskosten` integer NOT NULL, " +
                    "`produktion` integer NOT NULL, `kapazitaet` integer NOT NULL, `bauzeit` integer NOT NULL, primary key(`level`, `typ`));";
            stmt.executeUpdate(sql);

            //Franchise Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `franchise` (`id` integer primary key autoincrement, `ceo` integer NOT NULL, `name` varchar(64), `geld` integer, `baum` VARCHAR(128));";
            stmt.executeUpdate(sql);

            //Franchise-Spieler Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `franchiseSpieler` (`franchiseId` integer NOT NULL, `spielerId` integer NOT NULL, primary key(`franchiseId`, `spielerId`));";
            stmt.executeUpdate(sql);

            //Franchise-Nachricht Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `franchiseNachricht` (`id` integer primary key autoincrement, `franchiseId` integer NOT NULL, `nachricht` varchar(256) NOT NULL);";
            stmt.executeUpdate(sql);

            //Bauliste Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `bauliste` (`gebaeudeId` integer NOT NULL, `fertigstellung` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //FranchiseBaumEigenschaftenEigenschaften Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `franchiseBaumEigenschaften` (`id` varchar(1) primary key, `kosten` integer NOT NULL, `forschungsZeit` integer NOT NULL, " +
                    "`effektArt` varchar(32) NOT NULL, `effekt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //KFCFraktionBaumKostenEigenschaften Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `kfcFraktionBaumEigenschaften` (`id` varchar(1) primary key, `kosten` integer NOT NULL, `forschungsZeit` integer NOT NULL, " +
                    "`effektArt` varchar(32) NOT NULL, `effekt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //PizzaHutFraktionBaumKostenEigenschaften Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `phFraktionBaumEigenschaften` (`id` varchar(1) primary key, `kosten` integer NOT NULL, `forschungsZeit` integer NOT NULL, " +
                    "`effektArt` varchar(32) NOT NULL, `effekt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //mcKingFraktionBaumKostenEigenschaften Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `mcKFraktionBaumEigenschaften` (`id` varchar(1) primary key, `kosten` integer NOT NULL, `forschungsZeit` integer NOT NULL, " +
                    "`effektArt` varchar(32) NOT NULL, `effekt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //DiktatorCharakterBaumEigenschaften Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `dCharakterBaumEigenschaften` (`id` varchar(1) primary key, `kosten` integer NOT NULL, `forschungsZeit` integer NOT NULL, " +
                    "`effektArt` varchar(32) NOT NULL, `effekt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //KumpelCharakterBaumEigenschaften Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `kCharakterBaumEigenschaften` (`id` varchar(1) primary key, `kosten` integer NOT NULL, `forschungsZeit` integer NOT NULL, " +
                    "`effektArt` varchar(32) NOT NULL, `effekt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //Auktion Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `auktion` (`id` integer primary key autoincrement, `standortId` integer NOT NULL, " +
                    "`angebot` varchar(16) NOT NULL, `angebotAnzahl` integer NOT NULL, `nachfrage` varchar(16) NOT NULL, " +
                    "`nachfrageAnzahl` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //Einheiten Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `einheit` (`id` integer primary key autoincrement, `standortId` integer NOT NULL, " +
                    "`typ` varchar(32) NOT NULL, `anzahl` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //EinheitenEigenschaten Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `einheitEigenschaften` (`typ` varchar(32) primary key, `kostenMehl` integer NOT NULL, `kostenFleisch` integer NOT NULL, " +
                    "`kostenGemuese` integer NOT NULL, `angriff` integer NOT NULL, `verteidigung` integer NOT NULL, `bauzeit` integer NOT NULL, `kapazitaet` integer NOT NULL);";
            stmt.executeUpdate(sql);

            //Bewegung Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `bewegung` (`id` integer primary key autoincrement, `startId` integer NOT NULL, `zielId` integer NOT NULL, " +
                    "`truppen` varchar(32) NOT NULL, `ankunft` long NOT NULL, `art` varchar(32) NOT NULL);";
            stmt.executeUpdate(sql);

            //Forschungs Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `forschungsAuftraege` (`id` integer primary key autoincrement, `baumArt` varchar(32) NOT NULL, `baumId` integer NOT NULL, `knoten` varchar(32) NOT NULL, " +
                    "`fertigstellung` long NOT NULL);";
            stmt.executeUpdate(sql);

            //Atkive-Booster Tabelle
            sql = "CREATE TABLE IF NOT EXISTS `booster` (`id` integer primary key autoincrement, `spielerId` integer NOT NULL, `bonus` varchar(32) NOT NULL, `wert` REAL NOT NULL, `ablaufzeitpunkt` integer NOT NULL);";
            stmt.executeUpdate(sql);

            stmt.close();
            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Loescht den kompletten Datenbestand.
     *
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public synchronized boolean loescheTabellen() {
        try {
            stmt = c.createStatement();
            String sql;

            //Benutzer Tabelle
            sql = "DROP TABLE IF EXISTS `benutzer`;";
            stmt.executeUpdate(sql);

            //Standort Tabelle
            sql = "DROP TABLE IF EXISTS `standort`;";
            stmt.executeUpdate(sql);

            //Gebaeude Tabelle
            sql = "DROP TABLE IF EXISTS `gebaeude`;";
            stmt.executeUpdate(sql);

            //Einheiten-Bauliste Tabelle
            sql = "DROP TABLE IF EXISTS `einheitBauliste`;";
            stmt.executeUpdate(sql);

            //Nachrichten Tabelle
            sql = "DROP TABLE IF EXISTS `nachricht`;";
            stmt.executeUpdate(sql);

            //Fraktionsbaum Tabelle
            sql = "DROP TABLE IF EXISTS `fraktionBaum`;";
            stmt.executeUpdate(sql);

            //Charakterbaum Tabelle
            sql = "DROP TABLE IF EXISTS `charakterBaum`;";
            stmt.executeUpdate(sql);

            //Bonus Tabelle
            sql = "DROP TABLE IF EXISTS `boni`;";
            stmt.executeUpdate(sql);

            //Karte Tabelle
            sql = "DROP TABLE IF EXISTS `karte`;";
            stmt.executeUpdate(sql);

            //Einstellungen Tabelle
            sql = "DROP TABLE IF EXISTS `einstellungen`;";
            stmt.executeUpdate(sql);

            //HauptgebaeudeStufen Tabelle
            sql = "DROP TABLE IF EXISTS `hauptgebaeudeStufen`;";
            stmt.executeUpdate(sql);

            //MuehleStufen Tabelle
            sql = "DROP TABLE IF EXISTS `muehleStufen`;";
            stmt.executeUpdate(sql);

            //FleischereiStufen Tabelle
            sql = "DROP TABLE IF EXISTS `fleischereiStufen`;";
            stmt.executeUpdate(sql);

            //BauernhofStufen Tabelle
            sql = "DROP TABLE IF EXISTS `bauernhofStufen`;";
            stmt.executeUpdate(sql);

            //LagerStufen Tabelle
            sql = "DROP TABLE IF EXISTS `lagerStufen`;";
            stmt.executeUpdate(sql);

            //LagerStufen Tabelle
            sql = "DROP TABLE IF EXISTS `gebaeudeStufen`;";
            stmt.executeUpdate(sql);

            //Franchise Tabelle
            sql = "DROP TABLE IF EXISTS `franchise`;";
            stmt.executeUpdate(sql);

            //Franchise-Spieler Tabelle
            sql = "DROP TABLE IF EXISTS `franchiseSpieler`;";
            stmt.executeUpdate(sql);

            //Franchise-Nachricht Tabelle
            sql = "DROP TABLE IF EXISTS `franchiseNachricht`;";
            stmt.executeUpdate(sql);

            //Bauliste Tabelle
            sql = "DROP TABLE IF EXISTS `bauliste`;";
            stmt.executeUpdate(sql);

            //Bauliste Tabelle
            sql = "DROP TABLE IF EXISTS `franchiseBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //Auktion Tabelle
            sql = "DROP TABLE IF EXISTS `auktion`;";
            stmt.executeUpdate(sql);

            //Einheiten Tabelle
            sql = "DROP TABLE IF EXISTS `einheit`;";
            stmt.executeUpdate(sql);

            //EinheitenEigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `einheitEigenschaften`;";
            stmt.executeUpdate(sql);

            //Bewegeung Tabelle
            sql = "DROP TABLE IF EXISTS `bewegung`;";
            stmt.executeUpdate(sql);

            //ForschungsAuftraege Tabelle
            sql = "DROP TABLE IF EXISTS `forschungsAuftraege`;";
            stmt.executeUpdate(sql);

            //kfcFraktionBaumEigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `kfcFraktionBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //mcKFraktionBaumEigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `mcKFraktionBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //phFraktionBaumEigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `phFraktionBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //dCharakterBaumEigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `dCharakterBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //kCharakterBaumEigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `kCharakterBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //Einheiten Tabelle
            sql = "DROP TABLE IF EXISTS `franchiseBaumKosten`;";
            stmt.executeUpdate(sql);

            //Booster Tabelle
            sql = "DROP TABLE IF EXISTS `booster`;";
            stmt.executeUpdate(sql);

            //Charakter Baum-Eigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `charakterBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            //Fraktions Baum-Eigenschaften Tabelle
            sql = "DROP TABLE IF EXISTS `fraktionBaumEigenschaften`;";
            stmt.executeUpdate(sql);

            stmt.close();
            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Schreibt alle Gebaeudeeigenschaften in die Datenbank.
     */
    private void erstelleGebaeude() {
        int maximaleLevel = 3;
        String sql = "";

        try {
            stmt = c.createStatement();

            //Algorithmus fuer Hauptgebaeude
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'H', '" + i*100 + "', '" + i*5 + "', '0', '" + i*60 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Bauernhof
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KB', '" + i*100 + "', '" + i*4 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Bauernhof
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MB', '" + i*100 + "', '" + i*5 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Bauernhof
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PB', '" + i*100 + "', '" + i*7 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Muehle
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KM', '" + i*100 + "', '" + i*5 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Muehle
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MM', '" + i*100 + "', '" + i*5 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Muehle
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PM', '" + i*100 + "', '" + i*5 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Schlachter
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KS', '" + i*100 + "', '" + i*7 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Schlachter
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MS', '" + i*100 + "', '" + i*5 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Schlachter
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PS', '" + i*100 + "', '" + i*4 + "', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Lager
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KL', '" + i*100 + "', '0', '" + i*800 + "', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Lager
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'ML', '" + i*100 + "', '0', '" + i*800 + "', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Lager
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PL', '" + i*100 + "', '0', '" + i*800 + "', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Restaurant
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KR', '" + i*100 + "', '0', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Restaurant
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MR', '" + i*100 + "', '0', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Restaurant
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PR', '" + i*100 + "', '0', '0', '" + i*30 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-DNA-Labor
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KD', '" + i*100 + "', '0', '0', '" + i*50 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-DNA-Labor
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MD', '" + i*100 + "', '0', '0', '" + i*50 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-DNA-Labor
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PD', '" + i*100 + "', '0', '0', '" + i*50 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Handelsposten
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KP', '" + i*100 + "', '0', '0', '" + i*50 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Handelsposten
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MP', '" + i*100 + "', '0', '0', '" + i*50 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Handelsposten
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PP', '" + i*100 + "', '0', '0', '" + i*50 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer KFC-Rechtsabteilung
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'KA', '" + i*400 + "', '0', '0', '" + i*120 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer McKing-Rechtsabteilung
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'MA', '" + i*400 + "', '0', '0', '" + i*120 + "');";
                stmt.executeUpdate(sql);
            }

            //Algorithmus fuer PizzaCap-Rechtsabteilung
            for(int i = 1; i < maximaleLevel+1; i++) {
                sql = "INSERT INTO `gebaeudeStufen` VALUES ('" + i + "', 'PA', '" + i*400 + "', '0', '0', '" + i*120 + "');";
                stmt.executeUpdate(sql);
            }
        }catch (Exception e) {
        }
    }

    /**
     * Schreibt alle Einheiteneigenschaften in die Datenbank.
     */
    private void erstelleEinheiten() {
        String sql = "";

        try {
            stmt = c.createStatement();

            //KFC leichte Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('KL1', '12', '10', '8', '1', '1', '2', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('KL2', '14', '12', '10', '1', '2', '3', '0');";
            stmt.executeUpdate(sql);

            //KFC mittlere Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('KM1', '24', '20', '16', '4', '2', '8', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('KM2', '26', '22', '18', '3', '5', '10', '0');";
            stmt.executeUpdate(sql);

            //KFC schwere Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('KS1', '32', '30', '24', '8', '4', '14', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('KS2', '34', '32', '26', '5', '7', '16', '0');";
            stmt.executeUpdate(sql);

            //McKing leichte Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('ML1', '10', '10', '10', '1', '1', '2', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('ML2', '12', '12', '12', '2', '2', '3', '0');";
            stmt.executeUpdate(sql);

            //McKing mittlere Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('MM1', '20', '20', '20', '3', '3', '8', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('MM2', '22', '22', '22', '4', '4', '10', '0');";
            stmt.executeUpdate(sql);

            //McKing schwere Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('MS1', '30', '30', '30', '6', '6', '14', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('MS2', '32', '32', '32', '7', '6', '16', '0');";
            stmt.executeUpdate(sql);

            //PizzaCap leichte Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('PL1', '8', '10', '12', '1', '1', '2', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('PL2', '10', '12', '14', '2', '2', '3', '0');";
            stmt.executeUpdate(sql);

            //PizzaCap mittlere Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('PM1', '16', '20', '24', '3', '3', '8', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('PM2', '18', '22', '26', '4', '4', '10', '0');";
            stmt.executeUpdate(sql);

            //PizzaCap schwere Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('PS1', '24', '30', '36', '8', '3', '14', '0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('PS2', '26', '32', '38', '7', '7', '16', '0');";
            stmt.executeUpdate(sql);

            //Transport Einheiten
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('T1', '10', '10', '10', '0', '0', '10', '10');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `einheitEigenschaften` VALUES ('T2', '25', '25', '25', '0', '0', '25', '24');";
            stmt.executeUpdate(sql);

            //Super Einheit
            sql = "INSERT INTO `einheitEigenschaften` VALUES ('X1', '2400', '2400', '2400', '1000', '1000', '1800', '0');";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
        }
    }

    /**
     * Schreibt alle Forschungsbaumeigenschaften in die Datenbank.
     */
    private void erstelleBaeume() {
        int maximaleKnoten = 19;
        String sql = "";

        try {
            stmt = c.createStatement();
            String[] effektArten = {"produktionsbonus","geldbonus","geldbonus","produktionsbonus",
                    "forschungsKostenbonus","truppenKostenbonus","truppenGeschwindigkeitsbonus","forschungsKostenbonus",
                    "produktionsbonus","produktionsbonus","truppenAngriffsbonus","truppenVerteidigungsbonus",
                    "truppenVerteidigungsbonus","truppenAngriffsbonus","truppenKostenbonus","truppenGeschwindigkeitsbonus",
                    "produktionsbonus","produktionsbonus","Ultimative Einheit freischalten"};
            int[] effekte = {100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,-1};
            //Algorithmus fuer FranchiseBaum
            for(int i = 1; i < maximaleKnoten+1; i++) {
                sql = "INSERT INTO `franchiseBaumEigenschaften` VALUES ('" + (char)(64+i) + "', '" + i*1000 + "', '" + i*20 + "', '" + effektArten[i-1] +"', '"+effekte[i-1]+"');";
                stmt.executeUpdate(sql);
            }

            String[] effektArtenKFC = {"truppenGeschwindigkeitsbonus","truppenKostenbonus","truppenVerteidigungsbonus","truppenAngriffsbonus",
                    "produktionsbonus","truppenAngriffsbonus","fleischProduktionsbonus","geldbonus",
                    "fleischProduktionsbonus","truppenAngriffsbonus","truppenAngriffsbonus","fleischProduktionsbonus",
                    "fleischProduktionsbonus","truppenAngriffsbonus","geldbonus","produktionsbonus",
                    "geldbonus","produktionsbonus","Ultimative Einheit freischalten"};
            int[] effekteKFC = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 25, 50, 25,-1};
            //Algorithmus fuer KFCFraktionBaum
            for(int i = 1; i < maximaleKnoten+1; i++) {
                sql = "INSERT INTO `kfcFraktionBaumEigenschaften` VALUES ('" + (char)(64+i) + "', '" + i*100 + "', '" + i*20 + "', '" + effektArtenKFC[i-1] +"', '"+effekteKFC[i-1]+"');";
                stmt.executeUpdate(sql);
            }

            String[] effektArtenPH = {"truppenGeschwindigkeitsbonus","truppenKostenbonus","truppenVerteidigungsbonus","truppenAngriffsbonus",
                    "produktionsbonus","truppenKostenbonus","mehlProduktionsbonus","geldbonus",
                    "mehlProduktionsbonus","truppenKostenbonus","truppenKostenbonus","mehlProduktionsbonus",
                    "mehlProduktionsbonus","truppenKostenbonus","geldbonus","produktionsbonus",
                    "geldbonus","produktionsbonus","Ultimative Einheit freischalten"};
            int[] effektePH = {50, 50, 50, 50, 50, 75, 50, 50, 50, 75, 75, 50, 50, 75, 50, 25, 50, 25, -1};
            //Algorithmus fuer PizzaHutFraktionBaum
            for(int i = 1; i < maximaleKnoten+1; i++) {
                sql = "INSERT INTO `phFraktionBaumEigenschaften` VALUES ('" + (char)(64+i) + "', '" + i*100 + "', '" + i*20 + "', '" + effektArtenPH[i-1] +"', '"+effektePH[i-1]+"');";
                stmt.executeUpdate(sql);
            }
            String[] effektArtenMK = {"truppenGeschwindigkeitsbonus","truppenKostenbonus","truppenVerteidigungsbonus","truppenAngriffsbonus",
                    "produktionsbonus","truppenVerteidigungsbonus","gemueseProduktionsbonus","geldbonus",
                    "gemueseProduktionsbonus","truppenVerteidigungsbonus","truppenVerteidigungsbonus","gemueseProduktionsbonus",
                    "gemueseProduktionsbonus","truppenVerteidigungsbonus","geldbonus","produktionsbonus",
                    "geldbonus","produktionsbonus","Ultimative Einheit freischalten"};
            int[] effekteMK = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 25, 50, 25, -1};
            //Algorithmus fuer McKingFraktionBaum
            for(int i = 1; i < maximaleKnoten+1; i++) {
                sql = "INSERT INTO `mcKFraktionBaumEigenschaften` VALUES ('" + (char)(64+i) + "', '" + i*100 + "', '" + i*20 + "', '" + effektArtenMK[i-1] +"', '"+effekteMK[i-1]+"');";
                stmt.executeUpdate(sql);
            }

            String[] effektArtenD = {"geldbonus","produktionsbonus","produktionsbonus","forschungsGeschwindigkeitsbonus",
                    "produktionsbonus","truppenAngriffsbonus","truppenVerteidigungsbonus","produktionsbonus",
                    "truppenGeschwindigkeitsbonus","truppenKostenbonus","mehlProduktionsbonus","fleischProduktionsbonus","gemueseProduktionsbonus",
                    "geldbonus","mehlProduktionsbonus","fleischProduktionsbonus",
                    "gemueseProduktionsbonus","geldbonus","Ultimative Einheit freischalten"};
            int[] effekteD = {50, 50, 50, 50, 15, 25, 25, 15, 25, 25, 50, 50, 50, 50, 150, 150, 150, 150, -1};
            //Algorithmus fuer DiktatorCharakterBaum
            for(int i = 1; i < maximaleKnoten+1; i++) {
                sql = "INSERT INTO `dCharakterBaumEigenschaften` VALUES ('" + (char)(64+i) + "', '" + i*125 + "', '" + i*20 + "', '" + effektArtenD[i-1] +"', '"+effekteD[i-1]+"');";
                stmt.executeUpdate(sql);
            }

            String[] effektArtenK = {"geldbonus","produktionsbonus","produktionsbonus","forschungsKostenbonus",
                    "truppenKostenbonus","gemueseProduktionsbonus","geldbonus","truppenGeschwindigkeitsbonus",
                    "mehlProduktionsbonus","fleischProduktionsbonus","truppenGeschwindigkeitsbonus","truppenKostenbonus",
                    "truppenAngriffsbonus","truppenVerteidigungsbonus","truppenGeschwindigkeitsbonus","truppenKostenbonus",
                    "truppenAngriffsbonus","truppenVerteidigungsbonus","Ultimative Einheit freischalten"};
            int[] effekteK = {50,50,50,100,100,25,25,100,25,25,50,50,50,50,150,150,150,150, -1};
            //Algorithmus fuer KumpelCharakterBaum
            for(int i = 1; i < maximaleKnoten+1; i++) {
                sql = "INSERT INTO `kCharakterBaumEigenschaften` VALUES ('" + (char)(64+i) + "', '" + i*125 + "', '" + i*20 + "', '" + effektArtenK[i-1] +"', '"+effekteK[i-1]+"');";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            
        }
    }

    /**
     * Setzt alle Servereinstellungen.
     *
     * @param kartenGroesse Die Kartengroesse
     * @param serverPort Der Serverport
     * @return true, falls erfolgreich geaendert, false sonst.
     */
    public synchronized boolean setzeServerEinstellungen(final int kartenGroesse, final int serverPort) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `einstellungen` SET `kartenGroesse` = '" + kartenGroesse + "', `port` = '" + serverPort + "'";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Initialisiert Truppen im jeweiligen Standort der entsprechenden Fraktion der Benutzers.
     *
     * @param standortId Die StandortID des Standortes.
     * @param fraktion Die Fraktion des Benutzers.
     */
    private void initialisiereEinheiten(final int standortId, final String fraktion) {
        try {
            String sql;

            //Leichte Einheit 1
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', '" + fraktion +"L1', '0');";
            stmt.executeUpdate(sql);

            //Leichte Einheit 2
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', '" + fraktion +"L2', '0');";
            stmt.executeUpdate(sql);

            //Mittlere Einheit 1
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', '" + fraktion +"M1', '0');";
            stmt.executeUpdate(sql);

            //Mittlere Einheit 2
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', '" + fraktion +"M2', '0');";
            stmt.executeUpdate(sql);

            //Schwere Einheit 1
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', '" + fraktion +"S1', '0');";
            stmt.executeUpdate(sql);

            //Schwere Einheit 2
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', '" + fraktion +"S2', '0');";
            stmt.executeUpdate(sql);

            //Transport Einheit 1
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', 'T1', '0');";
            stmt.executeUpdate(sql);

            //Transport Einheit 2
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', 'T2', '0');";
            stmt.executeUpdate(sql);

            //Super Einheit
            sql = "INSERT INTO `einheit` VALUES (NULL, '" + standortId + "', 'X1', '0');";
            stmt.executeUpdate(sql);
        } catch ( Exception e ) {
        }
    }

    //Die Datenerstellungs-Methoden

    /**
     * Erstellt einen neuen Benutzer und schreibt die Daten in die Datenbank.
     *
     * @param benutzername Der Benutzername.
     * @param passwort Das Passwort des Benutzers.
     * @param fraktion Die zugehoerige Fraktion des Benutzers.
     * @param charakter Der zugehoerige Charakter des Benutzers.
     * @return Die ID des neu erstellten Benutzers.
     */
    public synchronized int erstelleBenutzer(final String benutzername, final String passwort, final String fraktion, final String charakter) {
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO `benutzer` VALUES (NULL, '" + benutzername + "', '" + passwort + "', '" + fraktion + "', 1000, 10, '" + charakter + "', NULL, 0, 0, '" + System.currentTimeMillis()/1000 + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT MAX(`id`) AS `id` FROM `benutzer`;";
            ResultSet rs = stmt.executeQuery(sql);
            int id = 0;
            if(rs.next()) {
                id = rs.getInt("id");
            } else {
                return -1;
            }
            rs.close();

            sql = "INSERT INTO `charakterBaum` VALUES(NULL, " + id + ", 'A0/B0/C0/D0/E0/F0/G0/H0/I0/J0/K0/L0/M0/N0/O0/P0/Q0/R0/S0');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `fraktionBaum` VALUES(NULL, " + id + ", 'A0/B0/C0/D0/E0/F0/G0/H0/I0/J0/K0/L0/M0/N0/O0/P0/Q0/R0/S0');";
            stmt.executeUpdate(sql);

            if(charakter.equals("D")){
                sql = "INSERT INTO `boni` VALUES(NULL, '" + id + "', '-1', '0', '0.05', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');";
                stmt.executeUpdate(sql);
            }else{
                sql = "INSERT INTO `boni` VALUES(NULL, '" + id + "', '-1', '0', '0', '0', '0', '0', '0', '0.05', '0.05', '0.05', '0.05', '0', '0');";
                stmt.executeUpdate(sql);
            }

            stmt.close();
            return id;
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Erstellt eine neue Auktion und schreibt die Daten in die Datenbank.
     *
     * @param standortId Die Standort-ID, an dem das Angebot erstellt wird.
     * @param angebot Die angebotene Ressource.
     * @param angebotAnzahl Die Anzahl der angebotenen Ressource.
     * @param nachfrrage Die nachgefrate Ressource.
     * @param nachfrageAnzahl Die Anzahl der nachgefragten Ressource.
     * @return Die ID der neu erstellten Auktion.
     */
    public synchronized int erstelleAuktion(final int standortId, final String angebot, final int angebotAnzahl, final String nachfrrage, final int nachfrageAnzahl) {
        try {
            int id = 0;

            stmt = c.createStatement();
            String sql = "INSERT INTO `auktion` VALUES (NULL, '" + standortId + "', '" + angebot + "', '" + angebotAnzahl + "', '" + nachfrrage + "', '" + nachfrageAnzahl + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT MAX(`id`) AS `id` FROM `auktion`;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                id = rs.getInt("id");
                rs.close();
                stmt.close();
            } else {
                return -1;
            }

            return id;
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Erstellt eine neue Franchisenachricht und schreibt die Daten in die Datenbank.
     *
     * @param franchiseId Die ID des Franchises, an dem die Nachricht erstellt werden soll.
     * @param nachricht Die Nachricht, die erstellt werden soll.
     * @return Die ID der neu erstellten Nachricht.
     */
    public synchronized int erstelleFranchiseNachricht(final int franchiseId, final String nachricht) {
        try {
            int id = 0;

            stmt = c.createStatement();

            String sql = "INSERT INTO `franchiseNachricht` VALUES (NULL, '" + franchiseId + "', '" + nachricht + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT MAX(`id`) AS `id` FROM `franchiseNachricht`;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                id = rs.getInt("id");
                rs.close();
                stmt.close();
            } else {
                return -1;
            }

            return id;
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Erstellt eine neue Nachricht und schreibt die Daten in die Datenbank.
     *
     * @param senderId Die Benutzer-ID des Senders.
     * @param empfaengerId Die Benutzer-ID des Empfaengers.
     * @param nachricht Die Nachricht, die erstellt werden soll.
     * @return Die ID der neu erstellten Nachricht.
     */
    public synchronized int erstelleNachricht(final int senderId, final int empfaengerId, final String nachricht) {
        try {
            int id = 0;
            stmt = c.createStatement();
            String sql = "INSERT INTO `nachricht` VALUES (NULL, '" + senderId + "', '" + empfaengerId + "', '" + nachricht + "', '" + System.currentTimeMillis()/1000 + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT MAX(`id`) AS `id` FROM `nachricht`;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                id = rs.getInt("id");
                rs.close();
                stmt.close();
            } else {
                return -1;
            }

            return id;
        } catch ( Exception e ) {
            
            return -1;
        }
    }

    /**
     * Erstellt eine neue Truppenbewegung und schreibt die Daten in die Datenbank.
     *
     * @param startId Die StandortId des Start-Standortes.
     * @param zielId Die StandortId des Ziel-Standortes.
     * @param truppen Die Truppen, die in Bewegung gesetzt werden sollen.
     * @param ankunft Der Ankunftszeitpunkt der Bewegung.
     * @param art Die Art der Bewegung.
     * @return Die ID der neu erstellten Bewegeung.
     */
    public synchronized int erstelleBewegung(final int startId, final int zielId, final String truppen, final long ankunft, String art) {
        try {
            int id = 0;
            stmt = c.createStatement();
            String sql = "INSERT INTO `bewegung` VALUES (NULL, '" + startId+ "', '" + zielId+ "', '" + truppen+ "', '" + ankunft + "', '" + art + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT MAX(`id`) AS `id` FROM `bewegung`;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                id = rs.getInt("id");
                rs.close();
                stmt.close();
            } else {
                return -1;
            }

            return id;
        } catch ( Exception e ) {
            
            return -1;
        }
    }

    /**
     * Erstellt ein neues Franchise und schreibt die Daten in die Datenbank.
     *
     * @param ceo Die Spieler-ID des Gruenders.
     * @param name Der Name des neuen Franchises.
     * @return Die ID des neu erstellten Franchises.
     */
    public synchronized int erstelleFranchise(final int ceo, final String name) {
        try {
            int id = 0;

            stmt = c.createStatement();
            String sql = "INSERT INTO `franchise` VALUES (NULL, '" + ceo + "', '" + name + "', '0', 'A0/B0/C0/D0/E0/F0/G0/H0/I0/J0/K0/L0/M0/N0/O0/P0/Q0/R0/S0');";
            stmt.executeUpdate(sql);


            sql = "SELECT `id` FROM `franchise` WHERE `ceo` = '" + ceo + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                id = rs.getInt("id");
            } else {
                return -1;
            }

            sql = "INSERT INTO `franchiseSpieler` VALUES('" + id + "', '" + ceo + "');";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO `boni` VALUES(NULL, '-1', '" + id +"', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');";
            stmt.executeUpdate(sql);
            stmt.close();

            return id;
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Erstellt neue Einheiten und schreibt die Daten in die Datenbank.
     *
     * @param standortId Die Standort-ID des Standortes, an dem die neuen Truppen gebaut werden.
     * @param typ Der Typ der zu bauenden Einheit.
     * @param anzahl Die Anzahl der zu bauenden Einheit.
     * @param fertigstellung Der Fertigstellungszeitpunkt der Einheit.
     * @return true, falls fehlerfrei erstellt, false sonst.
     */
    public synchronized boolean erstelleEinheiten(final int standortId, final String typ, final int anzahl, final Long fertigstellung) {
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO `einheitBauliste` VALUES ('" + standortId + "', '" + typ + "', '" + anzahl + "', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Erstellt einen neuen Booster und schreibt die Daten in die Datenbank.
     *
     * @param spielerId Die Spieler-ID des Benutzers, zu dem der Booster erstellt werden soll.
     * @param bonus Der Bonus, den der Booster besitzt.
     * @param wert Der Boosterwert.
     * @param ablaufzeitpunkt Der Ablaufzeitpunkt des Boosters.
     * @return true, falls fehlerfrei erstellt, false sonst.
     */
    public synchronized boolean erstelleBooster(final int spielerId, final String bonus, final double wert, final Long ablaufzeitpunkt) {
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO `booster` VALUES (NULL, '" + spielerId + "', '" + bonus + "', '" + wert + "', '" + ablaufzeitpunkt + "');";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Erstellt einen neuen Admin mit hohen Geld und Gutschein Werten.
     *
     * @return true, falls fehlerfrei erstellt, false sonst.
     */
    public synchronized boolean erstelleAdmin() {
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO `benutzer` VALUES(0, 'Admin', 'admin', 'K', '1000000', '1000000', 'K', NULL, '0', '1', '" + System.currentTimeMillis()/1000 + "');";
            stmt.executeUpdate(sql);

            erzeugeNeuenStandort(0, "Admins Base", 0, 0);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Die Datenbank-Auslese-Methoden

    /**
     * Gibt alle belegten Standorte aus.
     *
     * @return Die Standorte im Format: StandortId1;...;StandortIdn
     */
    public synchronized String gibAlleStandortIds() {
        try {
            int standortId = 0;
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `standort` WHERE `id` > " + standortId + ";";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle Standorte eines Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die Standorte im Format: StandortId1;...;StandortIdn
     */
    public synchronized String gibStandortIds(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `standort` WHERE `spielerId` = " + spielerId + ";";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle vorhandenen Auktionen aus.
     *
     * @return Die Auktionen im Format: AuktionId1;...;AuktionIdn
     */
    public synchronized String gibAuktionIds() {
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `auktion`;";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle Einheiten in Anzahl und Typ des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Einheiten im Format: [typ][anzahl]
     */
    public synchronized String[][] gibAlleEinheiten(final int standortId) {
        try {
            String[][] result;
            int count = 0, counter = 0;

            stmt = c.createStatement();
            String sql = "SELECT count(id) AS `anzahl` FROM `einheit` WHERE `standortId` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                count = rs.getInt("anzahl");
            } else {
                return null;
            }

            result = new String[count][2];

            sql = "SELECT `typ`, `anzahl` FROM `einheit` WHERE `standortId` = '" + standortId + "';";
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result[counter][0] = rs.getString("typ");
                result[counter][1] = rs.getInt("anzahl") + "";
                counter++;
            }
            rs.close();

            return result;
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle Bewegungen aus, die angekommen sind.
     *
     * @return Die Bewegungen im Format: BewegungId1;...;BewegungIdn
     */
    public synchronized String gibAlleAngekommenenBewegungen() {
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `bewegung` WHERE `ankunft` < '" + System.currentTimeMillis()/1000 + "';";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle Gebaeude aus, die fertig gebaut sind.
     *
     * @return Die Gebaeude im Format: GebaeudeId1;...;GebaeudeIdn
     */
    public synchronized String gibAlleFertigenGebaeude() {
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `gebaeudeId` FROM `bauliste` WHERE `fertigstellung` < '" + System.currentTimeMillis()/1000 + "';";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result += rs.getInt("gebaeudeId") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle fertiggebauten Einheiten zurueck.
     *
     * @return Die Einheiten im Format: standortId;typ;anzahl;fertigstellung fuer jeden Eintrag.
     */
    public synchronized String[] gibAlleFertigenEinheiten() {
        try {
            String[] result;
            int count = 0, counter = 0;

            stmt = c.createStatement();
            String sql = "SELECT count(typ) AS `anzahl` FROM `einheitBauliste` WHERE `fertigstellung` < '" + System.currentTimeMillis()/1000 + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                count = rs.getInt("anzahl");
            } else {
                return null;
            }

            rs.close();
            sql = "SELECT * FROM `einheitBauliste` WHERE `fertigstellung` < '" + System.currentTimeMillis()/1000 + "' LIMIT '" + count + "';";
            rs = stmt.executeQuery(sql);

            result = new String[count];

            while(rs.next()) {
                result[counter] = rs.getString("standortId") + ";" + rs.getString("typ") + ";" + rs.getString("anzahl") + ";" + rs.getString("fertigstellung");
                counter++;
            }
            rs.close();

            return result;
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle Gebaeude des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Gebaeude im Format: GebaeudeId1;...;GebaeudeIdn
     */
    public synchronized String gibGebaeudeIds(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `gebaeude` WHERE `standortId` = '" + standortId + "' AND NOT `typ` = 'H';";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die HauptgebaeudeID des jeweiligen Standortes aus.
     *
     * @param standortId Die HauptgebaeudeID des Standortes.
     * @return Die HauptgebaeudeID.
     */
    public synchronized int gibHauptgebaeudeId(final int standortId){
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `gebaeude` WHERE `standortId` = '" + standortId + "' AND `typ` = 'H';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Eigenschaften des jeweiligen Gebaeudes aus.
     *
     * @param gebaeudeId Die GebaeudeId des Gebaeudes.
     * @return Die Gebaeudeeigenschaften im Format: standortId;platz;typ;level;imBau;fertigstellung
     */
    public synchronized String gibGebaeude(final int gebaeudeId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `gebaeude` WHERE `id` = '" + gebaeudeId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                return rs.getInt("standortId") + ";" + rs.getInt("platz") + ";" + rs.getString("typ") + ";" + rs.getInt("level") + ";" + rs.getBoolean("imBau") + ";" + rs.getLong("fertigstellung");
            } else {
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Franchisenachricht der jeweiligen FranchisenachrichtenID aus.
     *
     * @param nachrichtId Die FranchisenachrichtenID.
     * @return Die Franchisenachricht.
     */
    public synchronized String gibFranchiseNachricht(final int nachrichtId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `nachricht` FROM `franchiseNachricht` WHERE `id` = '" + nachrichtId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                return rs.getString("nachricht");
            } else {
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle NachrichtenIDs in absteigender Reihenfolge nach dem Erstelldatum eines Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Die NachrichtenIDs im Format: FranchisenachrichtIDn;..;FranchisenachrichtID1
     */
    public synchronized String gibAlleFranchiseNachrichtenIds(final int franchiseId) {
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `franchiseNachricht` WHERE `franchiseId` = '" + franchiseId + "' ORDER BY `id` ASC;";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Fraktion des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return die jeweilige Fraktion.
     */
    public synchronized String gibFraktion(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `fraktion` FROM `benutzer` WHERE `id` = '" + spielerId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString("fraktion");
            } else {
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die BenutzerID zu dem jeweiligen Spielernamen aus.
     *
     * @param spielername Der Spielername des Benutzers.
     * @return Die jeweilige BenutzerID.
     */
    public synchronized int gibSpielerId(String spielername) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `benutzer` WHERE `name` = '" + spielername + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1;
            }

        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt den Fraktionsbaum des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Der Fraktionsbaum.
     */
    public synchronized String gibFraktionBaum(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `baum` FROM `fraktionBaum` WHERE `spielerId` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("baum");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt den Charakterbaum des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Der Charakterbaum.
     */
    public synchronized String gibCharakterBaum(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `baum` FROM `charakterBaum` WHERE `spielerId` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("baum");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt den Franchisebaum des jeweiligen Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Der Franchisebaum.
     */
    public synchronized String gibFranchiseBaum(final int franchiseId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `baum` FROM `franchise` WHERE `id` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("baum");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt die FranchiseID des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die FranchiseID.
     */
    public synchronized int gibFranchiseId(int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `franchiseId` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("franchiseId");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die FranchiseID des jeweiligen Franchises aus.
     *
     * @param franchiseName Der Franchisename des Franchises.
     * @return Die FranchiseID.
     */
    public synchronized int gibFranchiseId(final String franchiseName) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `franchise` WHERE `name` = '" + franchiseName + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Anzahl der jeweiligen Einheit in einem Standort aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @param typ Der Einheitentyp.
     * @return Die Einheitenanzahl.
     */
    public synchronized int gibEinheitAnzahl(final int standortId, final String typ) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `anzahl` FROM `einheit` WHERE `standortId` = '" + standortId + "' AND `typ` = '" + typ + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt("anzahl");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt das Geld des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Das Geld.
     */
    public synchronized int gibGeld(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `geld` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("geld");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Gutscheine des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die Gutscheine.
     */
    public synchronized int gibGutscheine(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `gutschein` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("gutschein");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt den Charakter des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Der Charakter.
     */
    public synchronized String gibCharakter(int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `charakter` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("charakter");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt die BoniID des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die BoniID.
     */
    public synchronized int gibBoniId(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `boni` WHERE `spielerId` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Nachrichten des jeweiligen Benutzers aus, die der Benutzer versendet hat.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die versendeten Nachrichten.
     */
    public synchronized String gibVersendeteNachrichten(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `nachricht` WHERE `sender` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Nachrichten des jeweiligen Benutzers aus, die der Benutzer empfangen hat.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die empfangen Nachrichten.
     */
    public synchronized String gibEmpfangeneNachrichten(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `nachricht` WHERE `empfaenger` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Mehlanzahl des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Mehlanzahl.
     */
    public synchronized int gibMehl(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `mehl` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("mehl");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Fleischanzahl des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Fleischanzahl.
     */
    public synchronized int gibFleisch(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `fleisch` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("fleisch");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Gemueseanzahl des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Gemueseanzahl.
     */
    public synchronized int gibGemuese(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `gemuese` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("gemuese");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die X-Koordinate des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die X-Koordinate.
     */
    public synchronized int gibXKoordinate(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `x` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("x");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Y-Koordinate des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Y-Koordinate.
     */
    public synchronized int gibYKoordinate(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `y` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("y");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Kartengroesse aus.
     *
     * @return Die Kartengroesse.
     */
    public synchronized int gibKarte() {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `kartenGroesse` FROM `einstellungen`;";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("kartenGroesse");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt den Wert des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Gebaeudewerte im Format: [Gebaeudestufe][Kosten]
     */
    public synchronized int[][] gibStandortWert(final int standortId) {
        try {
            int[][] gebaeudeWerte = new int[10][2];
            int counter = 1;
            int hauptgebaeudeLevel;

            stmt = c.createStatement();
            String sql = "SELECT `hauptgebaeude` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                hauptgebaeudeLevel = rs.getInt("hauptgebaeude");
                gebaeudeWerte[0][0] = hauptgebaeudeLevel;
            } else {
                return null;
            }

            sql = "SELECT `geldAufstiegskosten` FROM `gebaeudeStufen` WHERE `level` = '" + hauptgebaeudeLevel + "' AND `typ` = 'H';";
            rs = stmt.executeQuery(sql);

            if(rs.next()) {
                gebaeudeWerte[0][1] = rs.getInt("geldAufstiegskosten");
            } else {
                return null;
            }

            sql = "SELECT g.level AS `level`, gs.geldAufstiegskosten AS `kosten` FROM gebaeude g JOIN gebaeudeStufen gs ON gs.level = g.level WHERE g.typ = gs.typ;";
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                gebaeudeWerte[counter][0] = rs.getInt("level");
                gebaeudeWerte[counter][1] = rs.getInt("kosten");
                counter++;
            }

            for(int i = counter; i < 10; i++) {
                gebaeudeWerte[i][0] = 0;
                gebaeudeWerte[i][1] = 0;
            }

            return gebaeudeWerte;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt die Hauptgebaeudestufe des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Hauptgebaeudestufe.
     */
    public synchronized int gibHauptgebaeude(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `hauptgebaeude` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("hauptgebaeude");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt das Geld des jeweiligen Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Das Geld des Franchises.
     */
    public synchronized int gibFranchiseGeld(final int franchiseId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `geld` FROM `franchise` WHERE `id` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("geld");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt sowohl alle eingehenden, wie auch alle ausgehenden Truppenbewegungen des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die Truppenbewegungen im Format: BewegungsID1;...;BewegungeIDn
     */
    public synchronized String gibBewegungen(final int standortId) {
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `bewegung` WHERE `startId` = '" + standortId + "' OR `zielId` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt den Zeitpunkt der letzten Aktion des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Der Zeitpunkt der letzten Aktion.
     */
    public synchronized long gibLetztesUpdate(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `letzteAktion` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getLong("letzteAktion");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt die Verteidigung des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Der Verteidigungswert.
     */
    public synchronized int gibStandortVerteidigung(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `verteidigung` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("verteidigung");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt den Namen des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Der Standortname.
     */
    public synchronized String gibStandortName(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `name` FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt die Nachrichteneigenschaften der jeweiligen Nahchricht aus.
     *
     * @param nachrichtId Die NachrichtenID der Nachricht.
     * @return Die Nachrichteneigenschaften.
     */
    public synchronized String[] gibNachricht(final int nachrichtId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `nachricht` WHERE `id` = '" + nachrichtId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                String[] result = new String[4];
                result[0] = rs.getInt("sender") + "";
                result[1] = rs.getInt("empfaenger") + "";
                result[2] = rs.getString("nachricht");
                result[3] = rs.getLong("datum") + "";

                return  result;
            } else {
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die BenutzerIDs des jeweiligen Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Die BenutzerIDs im Format: BenutzerID1;...;BenutzerIDn
     */
    public synchronized String gibFranchiseMitglieder(final int franchiseId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `benutzer` WHERE `franchiseId` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Anzahl der Einheit in der Bauschleife des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @param typ Der Einheitentyp.
     * @return Die Anzahl der jeweiigen Einheit in der Bauschleife.
     */
    public synchronized int gibAnzahlBauendeEinheit(final int standortId, final String typ) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `anzahl` FROM `einheitBauliste` WHERE `standortId` = '" + standortId + "' AND `typ` = '" + typ + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                return rs.getInt("anzahl");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt die Gebaeudeeigenschaften des jeweiligen Typs, sowie Levels aus.
     *
     * @param level Die Gebaeudestufe.
     * @param typ Der Gebaeudetyp.
     * @return Die Gebaeudeeigenschaften im Format: geldAufstiegskosten;produktion;kapazitaet;bauzeit
     */
    public synchronized String gibGebaeudeEigenschaften(final int level, final String typ) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `gebaeudeStufen` WHERE `level` = '" + level + "' AND `typ` = '" + typ + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString("geldAufstiegskosten") + ";" + rs.getInt("produktion") + ";" + rs.getInt("kapazitaet") + ";" + rs.getInt("bauzeit");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt den Boosterbonus des jeweiigen Boosters aus.
     *
     * @param boosterId Die BoosterID des Boosters.
     * @return Der Bonus des Boosters.
     */
    public synchronized String gibBoosterEigenschaft(final int boosterId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `bonus` FROM `booster` WHERE `id` = '" + boosterId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString("bonus");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Boostereigenschaften des jeweiligen Benutzers aus.
     *
     * @param boosterId Die BoosterID des Boosters.
     * @return Die Boostereigenschaften im Format: spielerId;bonus;wert;ablaufzeitpunkt
     */
    public synchronized String gibAktiveBoosterEigenschaft(final int boosterId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `spielerId`, `bonus`, `wert`, `ablaufzeitpunkt` AS `zeit` FROM `booster` WHERE `id` = '" + boosterId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("spielerId") + ";" + rs.getString("bonus") + ";" + rs.getDouble("wert") + ";" + rs.getLong("zeit");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle aktiven Booster des jeweiigen Benutzers aus.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return Die aktiven Booster im Format: [bonus][ablaufzeitpunkt] fuer jeden Eintrag.
     */
    public synchronized String[][] gibAktiveBooster(final int spielerId) {
        try {
            String[][] result;
            int count = 0, counter = 0;

            stmt = c.createStatement();
            String sql = "SELECT count(id) AS `id` FROM `booster` WHERE `spielerId` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                count = rs.getInt("id");
            } else {
                return null;
            }

            result = new String[count][2];

            sql = "SELECT `bonus`, `ablaufzeitpunkt` AS `zeit` FROM `booster` WHERE `spielerId` = '" + spielerId + "';";
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result[counter][0] = rs.getString("bonus");
                result[counter][1] = Long.toString(rs.getLong("zeit"));
                counter++;
            }

            return result;
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Einheiteneigenschaften des jeweiligen Typs aus.
     *
     * @param typ Der Einheitentyp.
     * @return Die Eigenschaften im Format: kostenMehl;kostenFleisch;kostenGemuese;angriff;verteidigung;bauzeit
     */
    public synchronized String gibEinheitenEigenschaften(final String typ) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `einheitEigenschaften` WHERE `typ` = '" + typ + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString("kostenMehl") + ";" + rs.getInt("kostenFleisch") + ";" + rs.getInt("kostenGemuese") + ";" + rs.getInt("angriff") + ";" + rs.getInt("verteidigung") + ";" + rs.getInt("bauzeit") + ";" + rs.getInt("Kapazitaet");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Bewegungseigenschaften der jeweiligen Bewegung aus.
     *
     * @param bewegungId Die BewegungsID der Bewegung.
     * @return Die Bewegungseigenschaften im Format: startId;zielId;truppen;ankunft;art
     */
    public synchronized String gibBewegungEigenschaften(final int bewegungId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `bewegung` WHERE `id` = '" + bewegungId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("startId") + ";" + rs.getInt("zielId") + ";" + rs.getString("truppen") + ";" + rs.getLong("ankunft") + ";" + rs.getString("art");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die BenutzerID des jeweiligen Standortes aus.
     *
     * @param standortId Die StandortID des Standortes.
     * @return Die BenutzerID.
     */
    public synchronized int gibSpielerVonStandort(int standortId){
        try {
            stmt = c.createStatement();
            String sql = "SELECT spielerId FROM `standort` WHERE `id` = '" + standortId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("spielerId");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt die Auktionseigenschaften der jeweiigen Auktion aus.
     *
     * @param auktionsId Die AuktionID der Auktion.
     * @return Die Auktionseigenschaften im Format: standortId;angebot;angebotAnzahl;nachfrage;nachfrageAnzahl
     */
    public synchronized String gibAuktionEigenschaften(final int auktionsId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `auktion` WHERE `id` = '" + auktionsId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("standortId") + ";" + rs.getString("angebot") + ";" + rs.getInt("angebotAnzahl") + ";" + rs.getString("nachfrage") + ";" + rs.getInt("nachfrageAnzahl");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt den jeweiigen Bonus eines bestimmten Boni aus.
     *
     * @param boniId Die BonusID des Boni.
     * @param bonus Der Bonus des Boni.
     * @return Den jeweiligen Bonus.
     */
    public synchronized double gibBonus(final int boniId, String bonus){
        try {
            stmt = c.createStatement();
            String sql = "SELECT `" + bonus + "` FROM `boni` WHERE `id` = '" + boniId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble(bonus);
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt alle Boni des jeweiligen Boni aus.
     *
     * @param boniId Die BonusID des Boni.
     * @return Die Boni im Format: verteidigungsbonus;produktionsbonus;geldbonus;mehlProduktionsbonus;fleischProduktionsbonus;gemueseProduktionsbonus;truppenGeschwindigkeitsbonus;truppenKostenbonus;truppenAngriffsbonus;truppenVerteidigungsbonus;forschungsGeschwindigkeitsbonus;forschungsKostenbonus
     */
    public synchronized String gibAlleBoni(final int boniId){
        try {
            stmt = c.createStatement();
            String sql = "SELECT  * FROM `boni` WHERE `id` = '" + boniId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            String result = "";
            if(rs.next()) {
                result += rs.getDouble("verteidigungsbonus") + ";" + rs.getDouble("produktionsbonus")+ ";" + rs.getDouble("geldbonus")+ ";" + rs.getDouble("mehlProduktionsbonus")+ ";" + rs.getDouble("fleischProduktionsbonus")+ ";" + rs.getDouble("gemueseProduktionsbonus")+ ";" + rs.getDouble("truppenGeschwindigkeitsbonus")+ ";" + rs.getDouble("truppenKostenbonus")+ ";" + rs.getDouble("truppenAngriffsbonus")+ ";" + rs.getDouble("truppenVerteidigungsbonus")+ ";" + rs.getDouble("forschungsGeschwindigkeitsbonus")+ ";" + rs.getDouble("forschungsKostenbonus");
                return result;
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt den jeweiigen Bonus des entsprechenden Franchises aus.
     *
     * @param franchiseId Die FranchiseID der Franchise.
     * @param bonus Der Bonus des Boni.
     * @return Den jeweiligen Bonus.
     */
    public synchronized double gibFranchiseBonus(final int franchiseId, final String bonus){
        try {
            stmt = c.createStatement();
            String sql = "SELECT `" + bonus + "` FROM `boni` WHERE `franchiseId` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble(bonus);
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt den Produktionsbonus der jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenuzterID des Benutzers.
     * @return Der Produktionsbonus.
     */
    public synchronized double gibProduktionsbonus(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `produktionsbonus` FROM `boni` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble("produktionsbonus");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt den Melh-Produktionsbonus der jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenuzterID des Benutzers.
     * @return Der Mehl-Produktionsbonus.
     */
    public synchronized double gibMehlProduktionsbonus(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `mehlProduktionsbonus` FROM `boni` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble("mehlProduktionsbonus");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt den Fleisch-Produktionsbonus der jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenuzterID des Benutzers.
     * @return Der Fleisch-Produktionsbonus.
     */
    public synchronized double gibFleischProduktionsbonus(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `fleischProduktionsbonus` FROM `boni` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble("fleischProduktionsbonus");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt den Gemuese-Produktionsbonus der jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenuzterID des Benutzers.
     * @return Der Gemuese-Produktionsbonus.
     */
    public synchronized double gibGemueseProduktionsbonus(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `gemueseProduktionsbonus` FROM `boni` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble("gemueseProduktionsbonus");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt den Geldbonus der jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenuzterID des Benutzers.
     * @return Der Geldbonus.
     */
    public synchronized double gibGeldbonus(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `geldbonus` FROM `boni` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getDouble("geldbonus");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }


    /**
     * Gibt die Forschungskosten des jeweiigen Franchise-Knotens aus.
     *
     * @param knoten Der Knoten.
     * @return Die Forschungskosten.
     */
    public synchronized int gibFranchiseKnotenPreis(final char knoten) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `kosten` FROM `franchiseBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("kosten");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt die Forschungsdauer des jeweiigen Franchise-Knotens aus.
     *
     * @param knoten Der Knoten.
     * @return Die Forschungsdauer.
     */
    public long gibFranchiseKnotenDauer(final char knoten){
        try {
            stmt = c.createStatement();
            String sql = "SELECT `forschungsDauer` FROM `franchiseBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getLong("forschungsDauer");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt die Forschungskosten des jeweiigen Fraktion-Knotens aus.
     *
     * @param knoten Der Knoten.
     * @return Die Forschungskosten.
     */
    public synchronized int gibFraktionKnotenPreis(final char knoten, final String fraktion) {
        try {
            stmt = c.createStatement();
            String sql = "";
            if(fraktion.equals("K")){
                sql = "SELECT `kosten` FROM `kfcFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            }else if (fraktion.equals("P")){
                sql = "SELECT `kosten` FROM `phFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            }else if (fraktion.equals("M")){
                sql = "SELECT `kosten` FROM `mcKfraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            }
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("kosten");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Gibt die Forschungskosten des jeweiigen Charakter-Knotens aus.
     *
     * @param knoten Der Knoten.
     * @return Die Forschungskosten.
     */
    public synchronized int gibCharakterKnotenPreis(final char knoten, final String charakter) {
        try {
            stmt = c.createStatement();
            String sql = "";
            if (charakter.equals("K")) {
                sql = "SELECT `kosten` FROM `kCharakterBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            } else if (charakter.equals("D")) {
                sql = "SELECT `kosten` FROM `dCharakterBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            }
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("kosten");
            } else {
                rs.close();
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt den Spielernamen des jeweiligen Benutzers aus.
     *
     * @param spielerId Die BenuzterID des Benutzers.
     * @return Der Spielername.
     */
    public synchronized String gibBenutzerName(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `name` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gibt den CEO des jeweiligen Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Die BenutzerID des CEOs.
     */
    public synchronized int gibFranchiseCeo(final int franchiseId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `ceo` FROM `franchise` WHERE `id` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("ceo");
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gibt den Franchisenamen des jeweiligen Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Der Franchisename.
     */
    public synchronized String gibFranchiseName(final int franchiseId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `name` FROM `franchise` WHERE `id` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Gibt alle fertigen Forschungsauftraege aus.
     *
     * @return Alle fertigen Forschungsauftraege im Format: ForschungsID1;...;ForschungsIDn
     */
    public synchronized String gibAlleFertigenForschungsAuftrage(){
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `forschungsAuftraege` WHERE `fertigstellung` < '" + System.currentTimeMillis()/1000 + "';";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Franchiseboni des jeweiligen Franchises aus.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @return Die Franchiseboni im Format: verteidigungsbonus;produktionsbonus;geldbonus;mehlProduktionsbonus;fleischProduktionsbonus;gemueseProduktionsbonus;truppenGeschwindigkeitsbonus;truppenKostenbonus;truppenAngriffsbonus;truppenVerteidigungsbonus;forschungsGeschwindigkeitsbonus;forschungsKostenbonus
     */
    public String gibFranchiseBoni(int franchiseId) {
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT  * FROM `boni` WHERE `franchiseId` = '" + franchiseId + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                result = rs.getDouble("verteidigungsbonus") + ";" + rs.getDouble("produktionsbonus")+ ";" + rs.getDouble("geldbonus")+ ";" + rs.getDouble("mehlProduktionsbonus")+ ";" + rs.getDouble("fleischProduktionsbonus")+ ";" + rs.getDouble("gemueseProduktionsbonus")+ ";" + rs.getDouble("truppenGeschwindigkeitsbonus")+ ";" + rs.getDouble("truppenKostenbonus")+ ";" + rs.getDouble("truppenAngriffsbonus")+ ";" + rs.getDouble("truppenVerteidigungsbonus")+ ";" + rs.getDouble("forschungsGeschwindigkeitsbonus")+ ";" + rs.getDouble("forschungsKostenbonus");
                return result;
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Effekte des jeweiligen Knotens im entsprechendem Baum aus.
     *
     * @param baumArt Der entsprechende Baum.
     * @param knoten Der jeweilige Knoten.
     * @return Der Effekt im Format: EffektArt;Effekt
     */
    public String gibKnotenEffekt(String baumArt, char knoten) {
        try {
            stmt = c.createStatement();
            String sql = "";
            if(baumArt.charAt(0) == 'C'){
                if(baumArt.charAt(1) == 'D'){
                    sql = "SELECT * FROM `dCharakterBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }else if(baumArt.charAt(1) == 'K'){
                    sql = "SELECT * FROM `kCharakterBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }
            }else if(baumArt.charAt(0)=='F'){
                if(baumArt.charAt(1) == 'K'){
                    sql = "SELECT * FROM `kfcFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                } else if(baumArt.charAt(1) == 'M'){
                    sql = "SELECT * FROM `mcKFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }else if(baumArt.charAt(1) == 'P') {
                    sql = "SELECT * FROM `phFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }
            }else if(baumArt.charAt(0) == 'G'){
                sql = "SELECT * FROM `franchiseBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            }
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString("effektArt") + ";" + rs.getInt("effekt");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt alle fertigen Booster aus.
     *
     * @return Die Booster im Format: BoosterID1;...;BoosterIDn
     */
    public synchronized String gibAlleFertigenBooster(){
        try {
            String result = "";

            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `booster` WHERE `ablaufzeitpunkt` < '" + System.currentTimeMillis()/1000 + "';";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                result += rs.getInt("id") + ";";
            }
            rs.close();

            if(result.length() > 1) {
                return result.substring(0, result.length()-1);
            } else {
                return result;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Eigenschaften des jeweiligen Forschungsauftrags aus.
     *
     * @param forschungsAuftragsId Die ForschungsauftragID des Forschungsauftrags.
     * @return Die Eigenschaften im Format: baumArt;baumId;knoten;fertigstellung
     */
    public synchronized String gibForschungsAuftragEigenschaften(int forschungsAuftragsId){
        try {
            stmt = c.createStatement();
            String sql = "SELECT * FROM `forschungsAuftraege` WHERE `id` = '" + forschungsAuftragsId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString("baumArt") + ";" + rs.getInt("baumId") + ";" + rs.getString("knoten") + ";" + rs.getLong("fertigstellung");
            } else {
                rs.close();
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Gibt die Forschungsdauer des jeweiligen Knoten des entsprechenden Forschungsbaumes aus.
     *
     * @param baumArt Der entsprechende Forschungsbaum.
     * @param knoten Der jeweilige Knoten.
     * @return Die Forschungsdauer.
     */
    public synchronized long gibForschungsdauer(String baumArt, char knoten){
        try {
            stmt = c.createStatement();
            String sql = "";
            if(baumArt.charAt(0) == 'C'){
                if(baumArt.charAt(1) == 'D'){
                    sql = "SELECT forschungsZeit FROM `dCharakterBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }else if(baumArt.charAt(1) == 'K'){
                    sql = "SELECT forschungsZeit FROM `kCharakterBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }
            }else if(baumArt.charAt(0) == 'F'){
                if(baumArt.charAt(1) == 'K'){
                    sql = "SELECT forschungsZeit FROM `kfcFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                } else if(baumArt.charAt(1) == 'M'){
                    sql = "SELECT forschungsZeit FROM `mcKFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }else if(baumArt.charAt(1) == 'P') {
                    sql = "SELECT forschungsZeit FROM `phFraktionBaumEigenschaften` WHERE `id` = '" + knoten + "';";
                }
            }else if(baumArt.charAt(0) == 'G'){
                sql = "SELECT forschungsZeit FROM `franchiseBaumEigenschaften` WHERE `id` = '" + knoten + "';";
            }
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getLong("forschungsZeit");
            } else {
                rs.close();
                return -1;
            }
        } catch ( Exception e ) {
            return -1;
        }
    }

    //Die Datenbank-Aktualisierungsmethoden.

    /**
     * Setzt neue Werte fuer die jeweilige Einheit im entsprechendem Standort.
     *
     * @param standortId Die StandortID des Standortes.
     * @param typ Der Einheitentyp.
     * @param anzahl Die Einheitenanzahl.
     * @param fertigstellung Der Fertigstellungszeitpunkt.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereBauendeEineheit(final int standortId, final String typ, final int anzahl, final Long fertigstellung) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `einheitBauliste` SET `anzahl` = '" + anzahl + "', `fertigstellung` = '" + fertigstellung + "' WHERE `standortId` = '" + standortId + "' AND `typ` = '" + typ + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den neuen Wert fuer das Geld des jeweiligen Benutzers.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param geld Der Geldwert.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereGeld(final int spielerId, final int geld) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `benutzer` SET `geld` = '" + geld + "' WHERE `id` = " + spielerId + ";";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Anzahl-Wert der jeweiligen Einheit im ensprechendem Standort um die uebergebene Anzahl hoch.
     *
     * @param standortId Die StandortID des Standortes.
     * @param typ Der Einheitentyp.
     * @param anzahlFertiger Anzahl zu erhoehender Einheiten.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereEinheit(final int standortId, final String typ, final int anzahlFertiger) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `einheit` SET `anzahl` = anzahl + '" + anzahlFertiger + "' WHERE `standortId` = '" + standortId + "' AND `typ` = '" + typ + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt eine andere Franchise im jeweiligen Benutzer.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param franchisId Die FranchiseID des Franchises.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereFranchise(final int spielerId, final int franchisId) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `benutzer` SET `franchiseId` = '" + franchisId + "' WHERE `id` = " + spielerId + ";";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Wert der Gutscheine des jeweiligen Benutzers neu.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param gutschein Der neue Gutscheinwert.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereGutscheine(final int spielerId, final int gutschein) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `benutzer` SET `gutschein` = '" + gutschein + "' WHERE `id` = " + spielerId + ";";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Wert der Ressourcen im jeweiligen Standort neu.
     *
     * @param standortId Die StandortID des Standortes.
     * @param fleisch Der neue Fleisch-Wert.
     * @param mehl Der neue Mehl-Wert.
     * @param gemuese Der neue Gemuese-Wert.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereRessourcen(final int standortId, final int fleisch, final int mehl, final int gemuese) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `standort` SET `fleisch` = '" + fleisch + "', `mehl` = '" + mehl + "', `gemuese` = '" + gemuese + "' WHERE `id` = '" + standortId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Charakterbaum des jeweiigen Benutzers neu.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param baum Der neue Charakterbaum.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereCharakterbaum(final int spielerId, final String baum) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `charakterBaum` SET `baum` = '" + baum + "' WHERE `spielerId` = '" + spielerId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Fraktionbaum des jeweiigen Benutzers neu.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param baum Der neue Fraktionbaum.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereFraktionsBaum(final int spielerId, final String baum) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `fraktionBaum` SET `baum` = '" + baum + "' WHERE `spielerId` = '" + spielerId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Franchisebaum des jeweiigen Franchises neu.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @param baum Der neue Franchisebaum.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereFranchiseBaum(final int franchiseId, final String baum) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `franchise` SET `baum` = '" + baum + "' WHERE `id` = '" + franchiseId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Wert des Franchisegeldes neu.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @param geld Der neue Geldwert.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereFranchiseGeld(final int franchiseId, final int geld) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `franchise` SET `geld` = '" + geld + "' WHERE `id` = '" + franchiseId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Wert des jeweiligen Bonus des entsprechenden Benutzers neu.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param bonus Der jeweilige Bonus.
     * @param wert Der neue Wert.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereBonus(final int spielerId, final String bonus, final double wert){
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `boni` SET " + bonus + " = '" + wert + "' WHERE `id` = '" + spielerId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Wert des jeweiligen Bonus des entsprechenden Franchises neu.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @param bonus Der jeweilige Bonus.
     * @param wert Der neue Wert.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aktualisiereFranchiseBonus(final int franchiseId, final String bonus, final double wert) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `boni` SET " + bonus + " = '" + wert + "' WHERE `franchiseId` = '" + franchiseId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Setzt den Inhalt der entsprechenden FranchiseNachricht neu.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @param i Die ID des Franchisenachricht.
     * @param nachricht Der Nachrichteninhalt.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public boolean aktualisiereFranchiseNachricht(final int franchiseId, final int i, final String nachricht) {
        try {
            int id = (franchiseId-1)*15 + i;
            stmt = c.createStatement();
            String sql = "UPDATE `franchiseNachricht` SET `nachricht` = '" + nachricht + "' WHERE `franchiseId` = '" + franchiseId+ "' AND `id` = '" + id + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    //Die Datenbank-Loeschungsmethoden.

    /**
     * Loescht die bauende Einheit des entsprechenden Typs im jeweiligen Standort.
     *
     * @param standortId Die StandortID des Standortes.
     * @param typ Der Einheitentyp.
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public synchronized boolean loescheBauendeEineheit(final int standortId, final String typ) {
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `einheitBauliste` WHERE `standortId` = '" + standortId + "' AND `typ` = '" + typ + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Loescht alle Einheiten im jeweiligen Standort.
     *
     * @param standortId Die StandortID des Standortes.
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public synchronized boolean loescheAlleEinheiten(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `einheit` SET `anzahl` = '0' WHERE `standortId` = '" + standortId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loescht den jeweiligen Booster.
     *
     * @param boosterId Die BoosterID des Boosters.
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public synchronized boolean loescheBooster(final int boosterId) {
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `booster` WHERE `id` = '" + boosterId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Loescht die jeweilige Truppenbewegung.
     *
     * @param bewegungsId Die BewegungsID der Bewegung.
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public boolean loescheBewegung(int bewegungsId) {
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `bewegung` WHERE `id` = '" + bewegungsId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Loescht den jeweiligen Forschungsauftrag.
     *
     * @param forschungsAuftragsId Die ForschungsauftragsID des Forschungsauftrages.
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public synchronized boolean loescheForschungsAuftrag(int forschungsAuftragsId){
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `forschungsAuftraege` WHERE `id` = '" + forschungsAuftragsId+ "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Loescht die jeweilige Autkion.
     *
     * @param auktionId Die AuktionID der Auktion.
     * @return true, falls fehlerfrei geloescht, false sonst.
     */
    public synchronized boolean loescheAuktion(final int auktionId) {
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `auktion` WHERE `id` = '" + auktionId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    //Andere Datenbankmethoden.

    /**
     * Baut das entsprechende Gebaeude auf dem entsprechendem Platz im jeweiligen Standort.
     *
     * @param standortId Die StandortID des Standorts.
     * @param platz Der Platz, auf dem das Gebaeude gebaut werden soll.
     * @param typ Der Gebaeudetyp.
     * @param fertigstellung Der Fertigstellungszeitpunkt.
     * @return true, falls fehlerfrei, false sonst.
     */
    public synchronized int baueGebaeude(final int standortId, final int platz, final String typ, final Long fertigstellung) {
        try {
            int id = 0;

            stmt = c.createStatement();
            String sql = "INSERT INTO `gebaeude` VALUES (NULL, '" + standortId + "', '" + platz + "', '" + typ + "', '0', '1', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT `id` FROM `gebaeude` WHERE `standortId` = '" + standortId + "' AND `platz` = '" + platz + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                id = rs.getInt("id");
            } else {
                return -1;
            }

            sql = "INSERT INTO `bauliste` VALUES ('" + id + "', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            return id;
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Stuft das jeweilige Gebaeude um eine Stufe auf.
     *
     * @param gebaeudeId Die GebaeudeID des Gebaeudes.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean aufleveleGebaeude(final int gebaeudeId) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `gebaeude` SET `level` = level+1, `imBau` = '0' WHERE `id` = '" + gebaeudeId + "';";
            stmt.executeUpdate(sql);
            String typ = "";
            sql = "SELECT `typ` FROM `gebaeude` WHERE `id` = '" + gebaeudeId+ "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                typ = rs.getString("typ");
                rs.close();
            } else {
                rs.close();
                return false;
            }

            if(typ.equals("H")) {
                int standortId;
                sql = "SELECT `standortId` FROM `gebaeude` WHERE `id` = '" + gebaeudeId+ "';";
                rs = stmt.executeQuery(sql);
                if(rs.next()) {
                    standortId = rs.getInt("standortId");
                } else {
                    rs.close();
                    return true;
                }
                sql = "UPDATE `standort` SET `hauptgebaeude` = hauptgebaeude+1 WHERE `id` = '" + standortId + "';";
                stmt.executeUpdate(sql);
            }
            sql = "DELETE FROM `bauliste` WHERE `gebaeudeId` = '" + gebaeudeId + "';";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt das aufzustufende Gebaeude in die Bauliste.
     *
     * @param gebaeudeId Die GebaeudeID des Gebaeudes.
     * @param fertigstellung Der Fertigstellungszeipunkt.
     * @return true, falls fehlerfrei, false sonst.
     */
    public synchronized boolean hochstufeGebaeude(final int gebaeudeId, final Long fertigstellung) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `gebaeude` SET `imBau` = '1' WHERE `id` = '" + gebaeudeId + "';";
            stmt.executeUpdate(sql);
            sql = "UPDATE `gebaeude` SET `fertigstellung` = '"+fertigstellung+"' WHERE `id` = '" + gebaeudeId + "';";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `bauliste` VALUES ('" + gebaeudeId + "', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Der jeweilige Standort wird von dem entsprechendem Spieler uebernommen.
     *
     * @param standortId Die StandortID des Standortes.
     * @param spielerId Die BenutzerID des Benutzers.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean uebernehmeStandort(final int standortId, final int spielerId) {
        try {
            String fraktion = gibFraktion(spielerId);

            stmt = c.createStatement();
            String sql = "DELETE FROM `einheit` WHERE `standortId` = '" + standortId + "';";
            stmt.executeUpdate(sql);

            sql = "DELETE FROM `gebaeude` WHERE `standortId` = '" + standortId + "';";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `gebaeude` VALUES (NULL, '" + standortId + "', '0', '" + fraktion + "L', '1', '0', '0');";
            stmt.executeUpdate(sql);

            sql = "UPDATE `standort` SET `spielerId` = '" + spielerId + "', `hauptgebaeude` = '1', `mehl` = '0', `fleisch` = '0', " +
                    "`gemuese` = '0', `verteidigung` = '0' WHERE `id` = '"+standortId+"';";
            stmt.executeUpdate(sql);

            sql = "DELETE FROM `einheitBauliste` WHERE `standortId` = '" + standortId + "'";
            stmt.executeUpdate(sql);

            sql = "DELETE FROM b.gebaeudeId AS gid FROM `gebaeude` g JOIN `bauliste` b on b.gebaeudeId = gid WHERE g.standortId = '" + standortId + "';";
            stmt.executeUpdate(sql);

            sql = "DELETE FROM `bewegeung` WHERE `startID` = '" + standortId + "';";
            stmt.executeUpdate(sql);

            long fertigstellung = System.currentTimeMillis()/1000;
            sql = "INSERT INTO `gebaeude` VALUES (NULL, '" + standortId + "', '10', 'H', '1', '0', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            initialisiereEinheiten(standortId, fraktion);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ueberprueft ob der jeweilige Benutzer mit entsprechendem Passwort im Datenbestand vorhanden ist.
     *
     * @param spielername Der Spielername.
     * @param passwort Das Passwort des Benutzers.
     * @return true, falls vorhanden, false sonst.
     */
    public synchronized boolean loginCorrect(final String spielername, final String passwort) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `id` FROM `benutzer` WHERE `name` = '" + spielername + "' AND `passwort` = '" + passwort + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Erzeugt einen neuen Standort mit dem uebergebenen Eigenschaften.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @param name Der Name des Standortes.
     * @param x Die X-Koordinate.
     * @param y Die Y-Koordiante.
     * @retur true, falls fehlerfrei erstellt, false sonst.
     */
    public synchronized int erzeugeNeuenStandort(final int spielerId, final String name, final int x, final int y) {
        try {
            int id = 0;
            String fraktion = "";

            stmt = c.createStatement();
            String sql = "INSERT INTO `standort` VALUES(NULL, " + spielerId + ", '" + name + "', 1, " + x + ", " + y + ", 100, 100, 100, 0);";
            stmt.executeUpdate(sql);

            sql = "SELECT max(`id`) AS `id` FROM `standort`;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                id = rs.getInt("id");
            } else {
                return -1;
            }

            sql = "SELECT `fraktion` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            rs = stmt.executeQuery(sql);
            if(rs.next()) {
                fraktion = rs.getString("fraktion");
            } else {
                return -1;
            }
            long fertigstellung = System.currentTimeMillis()/1000;
            sql = "INSERT INTO `gebaeude` VALUES (NULL, '" + id + "', '0', '" + fraktion + "L', '1', '0', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO `gebaeude` VALUES (NULL, '" + id + "', '10', 'H', '1', '0', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            initialisiereEinheiten(id, fraktion);

            return id;
        } catch ( Exception e ) {
            return -1;
        }
    }

    /**
     * Fuellt die Kartentabelle quadratisch um die uebergebene Kartengroesse.
     *
     * @param kartenGroesse Die Kartengroesse.
     * @return true, falls fehlerfrei erstellt, false sonst.
     */
    private boolean fuelleKarte(final int kartenGroesse) {
        if(kartenGroesse < 0) {
            throw new IllegalArgumentException("Die Kartengrosse muss ein positiver Werte sein.");
        }
        try {
            stmt = c.createStatement();
            String sql = "";

            for(int i = 0; i < kartenGroesse; i++) {
                for (int j = 0; j < kartenGroesse; j++) {
                    sql = "INSERT INTO `karte` VALUES (NULL, " + i + ", " + j + ", 0);";
                    stmt.executeUpdate(sql);
                }
            }

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Abfrage ob der uebergeben Standort belegt ist, oder nicht.
     *
     * @param standortId Die StandortID des Standortes.
     * @return true, falls belegt, false sonst.
     */
    public synchronized boolean istStandortBelegt(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `belegt` FROM `karte` WHERE `id` = " + standortId + ";";

            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getBoolean("belegt");
            }
            return false;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Belege den uebergebenen Standort.
     *
     * @param standortId Die StandortID des Standortes.
     * @return true, falls fehlerfrei belegt, false sonst.
     */
    public synchronized String belegeStandort(final int standortId) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `karte` SET `belegt` = 1 WHERE `id` = " + standortId + ";";
            stmt.executeUpdate(sql);

            sql = "SELECT `x`, `y` FROM `karte` WHERE `id` = " + standortId + ";";

            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt("x") + ";" + rs.getInt("y");
            } else {
                return null;
            }
        } catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Ueberprueft ob beide uebergebenen Benutzer miteinander verbuendet sind, oder nicht.
     *
     * @param startSpieler Die BenutzerID des ersten Benutzers.
     * @param zielSpieler Die BenutzerID des zweiten Benutzers.
     * @return true, falls verbuendet, false sonst.
     */
    public boolean isVerbuendeter(int startSpieler, int zielSpieler) {
        try {
            int franchiseStartSpieler = -1;
            int franchiseZielSpieler = -2;
            stmt = c.createStatement();
            String sql = "SELECT `franchiseId` FROM `benutzer` WHERE `id` = '" + startSpieler + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                franchiseStartSpieler = rs.getInt("franchiseId");
                if(franchiseStartSpieler == 0){
                    return false;
                }
            } else {
                return false;
            }
            sql = "SELECT `franchiseId` FROM `benutzer` WHERE `id` = '" + zielSpieler+ "';";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                franchiseZielSpieler = rs.getInt("franchiseId");
                if(franchiseStartSpieler == 0){
                    return false;
                }
            } else {
                return false;
            }
            if (franchiseStartSpieler == franchiseZielSpieler){
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gibt aus, ob der jeweilige Benutzer forschen darf, oder nicht.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @returnt rue, falls forschen darf, false sonst.
     */
    public synchronized boolean darfForschen(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "SELECT `darfForschen` FROM `benutzer` WHERE `id` = '" + spielerId + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getBoolean("darfForschen");
            } else {
                return false;
            }
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den Wert der Forschungseigenschaft des jeweiligen Benutzers.
     *
     * @param spielerId Die BenutzerID des Benutzers.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized boolean setzeDarfForschen(final int spielerId) {
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `benutzer` SET `darfForschen` = '" + 1 + "' WHERE `id` = " + spielerId + ";";
            stmt.executeUpdate(sql);
            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    /**
     * Setzt den entsprechenden Knoten des jeweiligen Forschungsbaums auf erfoscht.
     *
     * @param franchiseId Die FranchiseID des Franchises.
     * @param baumArt Der Forschungsbaum.
     * @param knoten Der jeweilige Knoten.
     * @param fertigstellung Der Fertigstellungszeitpunkt.
     * @return true, falls fehlerfrei aktualisiert, false sonst.
     */
    public synchronized int erforscheKnoten(int franchiseId, String baumArt, char knoten, long fertigstellung) {
        try {
            int id = 0;

            stmt = c.createStatement();
            String sql = "INSERT INTO `forschungsAuftraege` VALUES (NULL, '"+baumArt+"', '" + franchiseId + "', '" + knoten + "', '" + fertigstellung + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT `id` FROM `forschungsAuftraege` WHERE `baumId` = '" + franchiseId + "' AND `fertigstellung` = '" + fertigstellung + "';";
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                id = rs.getInt("id");
            } else {
                return -1;
            }
            return id;
        } catch (SQLException e) {
            return -1;
        }
    }

    /**
     * Setzt den Standortnamen des jeweiligen Standortes.
     *
     * @param standortId Die StandortID des Standortes.
     * @param name Der Standortname.
     * @return true, falls fehlerfrei aktualisert, false sonst.
     */
    public boolean setzteStandortnamen(int standortId, String name){
        try {
            stmt = c.createStatement();
            String sql = "UPDATE `standort` SET `name` = '" + name + "' WHERE `id` = " + standortId + ";";
            stmt.executeUpdate(sql);

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Fuegt den jeweiligen Benutzer in das enstsprechende Franchise ein.
     *
     * @param spieler Die BenutzerID des Benutzers.
     * @param franchise Die FranchiseID des Franchises.
     * @return true, falls fehlerfrei hinzugefuegt, false sonst.
     */
    public synchronized boolean hinzufuegeSpielerZuFranchise(final int spieler, final int franchise) {
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO `franchiseSpieler` VALUES('" + franchise + "', '" + spieler + "');";
            stmt.executeUpdate(sql);

            stmt = c.createStatement();
            sql = "UPDATE `benutzer` SET `franchiseId` = '" + franchise + "' WHERE `id` = " + spieler + ";";
            stmt.executeUpdate(sql);

            return true;
        } catch ( Exception e ) {
            return false;
        }
    }
}