package chatclient.service.servants;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.EventListener;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Tjänar-klass som hanterar dem meddelanden som väntar på att skickas ut till
 * servern, samt utskickandet utav dessa.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class MessageSendingServant extends
	AbstractServant<MessageSendingServant.Delegate> {

    /**
     * En "blockerande" kö-struktur för att lagra meddelanden i den ordning
     * (First-In-First-Out) som dem ska skickas till servern. Dess "blockerande"
     * egenskap är lämplig i det här fallet då det är en - från
     * tillämpningsprogrammet separat - tråd som skall hantera meddelandena
     * vilket tillåter att tråden på lämpligt sätt väntar på att kö-strukturen
     * populeras med meddelanden allt eftersom att tråden skickar dem.
     */
    private final LinkedBlockingQueue<String> outgoingMessages;

    /**
     * Referns till en buffrad tecken-skrivare kopplad till servern. Denna
     * används för att skicka utgående meddelanden till.
     */
    private final OutputStreamWriter serverWriter;

    /**
     * Skapar ett objekt utav klassen {@link MessageSendingServant}.
     * Konstruktorn kräver att få en referens till den buffrade tecken-skrivaren
     * som tillhör servern klienten är ansluten till.
     * 
     * @param serverWriter
     *            den buffrade tecken-skrivaren tillhörande den server som
     *            meddelandena skall skickas till
     */
    public MessageSendingServant(OutputStreamWriter serverWriter) {

	/*
	 * Referensen till den buffrade tecken-skrivare kopplad till servern
	 * uppdateras.
	 */
	this.serverWriter = serverWriter;

	/*
	 * Instansierar kö-strukturen som har i syfte att populeras med
	 * utskicksjobb samtidigt som jobben utförs en efter en - i rätt
	 * ordning.
	 */
	this.outgoingMessages = new LinkedBlockingQueue<>();

	/*
	 * Delegat-objektet instanseras till att inte göra något vid någon
	 * händelse över huvud taget. Detta för att undvika att instansvariabeln
	 * istället pekar mot null.
	 */
	this.delegate = new Delegate() {

	    @Override
	    public void onConnectionError() {
		/*
		 * Utebliven handling.
		 */
	    }
	};
    }

    @Override
    void init() {
	/*
	 * Utebliven handling vid start.
	 */
    }

    @Override
    void serve() throws InterruptedException {
	/*
	 * Försöker plocka nästa utskicksjobb som står på tur att utföras. Finns
	 * inga så väntas det - här.
	 */
	String outgoingMessage = MessageSendingServant.this.outgoingMessages
		.take();

	/*
	 * På ett sanerat och ordningssamt sätt - genom att "spola" strömmens
	 * buffer för att förhindra att ett meddelande ligger och väntar -
	 * försöks meddelandet skrivas via den mottagande serverns
	 * skrivar-objekt. Tecknet för radbryte indikerar slutet på meddelandet.
	 */
	try {
	    this.serverWriter.write(outgoingMessage + "\r");
	    this.serverWriter.flush();
	} catch (IOException e) {
	    /*
	     * Stöter programmet på ett undantag så betyder det att det inte
	     * gick att kommunicera med mottagaren - denne är då högst troligen
	     * inte länge uppkopplad och därför anropas den i förväg definierade
	     * metod som är till för att anropas i situationer som denna.
	     */
	    MessageSendingServant.this.delegate.onConnectionError();
	}

    }

    @Override
    void exit() {
	try {
	    /*
	     * Terminerar resurser som är kopplade till servern.
	     */
	    this.serverWriter.close();
	} catch (IOException e) {
	    /*
	     * Problem vid stängning av den buffrade tecken-skrivaren - finns
	     * inte mycket att göra åt detta.
	     */
	}
    }

    /**
     * Metod för att placera ett nytt meddelande i kön över utskicksjobb som
     * väntar på att utföras.
     *
     * @param message
     *            det meddelande som skall skickas ut
     */
    public void addMessageToQueue(String message) {
	this.outgoingMessages.offer(message);
    }

    /**
     * Metod som returnerar en sträng som representerar tjänar-objektet vid det
     * tillfälle som metoden anropats; denna represantation består i detta fall
     * utav antalet utskicksjobb som väntar på att hanteras.
     * 
     * @return en representerande sträng
     */
    @Override
    public final String toString() {
	final String numberOfAwaitingMessages;
	numberOfAwaitingMessages = String.valueOf(this.outgoingMessages.size());
	return numberOfAwaitingMessages;
    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som internt skall anropas av det ägande
     * objeket vid olika scenarion.
     * 
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate extends EventListener {
	/**
	 * Den metod som internt anropas vid fel i kommunikationen med servern.
	 */
	void onConnectionError();
    }
}
