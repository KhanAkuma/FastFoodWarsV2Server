package Server.Communication;

import Server.BusinessLogic.*;
import Server.Exceptions.KomException;

/**
 * Created by Falko on 09.06.2016.
 *
 * Eine Klasse die den ersten InputString des Clients analysiert und dann an die entsprechenden Handler weiter leitet
 * und deren Antwort an den Client zurueck gibt
 */
public class CommandInterpreter{

    public CommandInterpreter(){}

    /**
     * Methode die den InputString[] analysiert
     *
     * @param input String[] der vom CLient geschickt wird
     * @return String[] die Antwort die aus dem Input folgt
     */
    public String[] interpret(String[] input){
        String[] antwort = {"FALSE"};
        switch (input[0]) {
            case "SPEICHERN":
                SaveHandler saveHandler = new SaveHandler();
                return saveHandler.handle(input);

            case "EINLOGGEN":
                LoginHandler loginHandler = new LoginHandler();
                try {
                    return loginHandler.handle(input);
                } catch (KomException e) {
                    e.printStackTrace();
                    System.out.println("KomException");
                }
                return antwort;

            case "LADEN":
                LoadHandler loadHandler = new LoadHandler();
                try{
                    return loadHandler.handle(input);
                } catch(KomException e) {
                    System.out.println("KomException");
                }

            case "FRANCHISE":
                FranchiseHandler franchiseHandler= new FranchiseHandler();
                return franchiseHandler.handle(input);

            case "KOLONISIEREN":
                break;

            case "ANFRAGE":
                AnfrageHandler anfrageHandler = new AnfrageHandler();
                return anfrageHandler.handle(input);

            case "BAUEN":
                Buildhandler buildhandler = new Buildhandler();
                try{
                    return buildhandler.handle(input);
                } catch(KomException e) {
                    System.out.println("KomException");
                }
                return antwort;

            case "REGISTRIEREN":
                RegisterHandler registerHandler = new RegisterHandler();
                try{
                    return registerHandler.handle(input);
                } catch(KomException e) {
                System.out.println("KomException");
                }

            case "UPDATEBAUM":
                BaumHandler baumHandler = new BaumHandler();
                return baumHandler.handle(input);

            case "BEZAHLEN":
                BezahlenHandler bezahlenHandler = new BezahlenHandler();
                try {
                    return bezahlenHandler.handle(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case "NACHRICHT":
                NachrichtenHandler nachrichtenHandler = new NachrichtenHandler();
                    return nachrichtenHandler.handle(input);

            case "UPDATE":
                UpdateHandler uh = new UpdateHandler();
                return uh.handle(input);

            case "AUKTION":
                AuktionHandler auktionHandler = new AuktionHandler();
                return auktionHandler.handle(input);

            case "BONI":
                BoniHandler boniHandler = new BoniHandler();
                return boniHandler.handle(input);

            case "TRUPPEN":
                TruppenHandler truppenHandler = new TruppenHandler();
                return truppenHandler.handle(input);
            default:
                String[] defaultantwort = {"No valid Input"};
                return defaultantwort;
        }
        String[] failMessage ={"Critical Failure"};
        return failMessage;
    }
}
