package chatclient.service.servants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EventListener;

/**
 * Tjänar-klass som ständigt lyssnar på och hanterar nya inkommande meddelanden
 * från servern.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class MessageReceivingServant extends
	AbstractServant<MessageReceivingServant.Delegate> {

    /**
     * Referns till en buffrad tecken-läsare kopplad till servern. Denna används
     * för att läsa inkommande meddelanden ifrån.
     */
    private final BufferedReader serverReader;

    /**
     * Skapar ett objekt utav klassen {@link MessageReceivingServant}.
     * Konstruktorn kräver att få en referens till den buffrade tecken-läsaren
     * som tillhör servern klienten är ansluten till.
     * 
     * @param serverReader
     *            den buffrade tecken-läsaren tillhörande den server som
     *            meddelandena skall skickas till
     */
    public MessageReceivingServant(BufferedReader serverReader) {

	/*
	 * Referensen till den buffrade tecken-läsaren kopplad till servern
	 * uppdateras.
	 */
	this.serverReader = serverReader;

	/*
	 * Delegat-objektet instanseras till att inte göra något vid någon
	 * händelse över huvud taget. Detta för att undvika att instansvariabeln
	 * istället pekar mot null.
	 */
	this.delegate = new Delegate() {

	    @Override
	    public void onMessageReceived(String message) {
		/*
		 * Utebliven handling.
		 */
	    }

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
    void serve() {
	/*
	 * Loop som väntar på en rad från servern. En rad avslutas per
	 * definition med ett '\n' eller '\r' eller också "\r\n".
	 * 
	 * Om ett null-värde returneras så är data-strömmen från servern
	 * förmodligen stängd - i sådana fall uppmärksammas medlaren om det och
	 * objektet slutas köras.
	 * 
	 * Annars uppmärksammas medlaren om att ett nytt meddelande har lästs in
	 * och loopen fortsätter.
	 */
	String message;
	try {
	    if ((message = this.serverReader.readLine()) == null)
		MessageReceivingServant.this.delegate.onConnectionError();
	    else
		MessageReceivingServant.this.delegate
			.onMessageReceived(message);

	} catch (IOException e) {
	    MessageReceivingServant.this.delegate.onConnectionError();
	}
    }

    @Override
    void exit() {
	try {
	    /*
	     * Terminerar resurser som är kopplade till servern.
	     */
	    this.serverReader.close();
	} catch (IOException e) {
	    /*
	     * Problem vid stängning av den buffrade tecken-läsaren - finns inte
	     * mycket att göra åt detta.
	     */
	}
    }

    @Override
    public String toString() {
	return this.isServing() ? "Listening on incoming messages from the server..."
		: "Standing by...";
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
	 * Den metod som internt anropas när ett meddelande mottagits från
	 * servern.
	 * 
	 * @param message
	 *            det meddelande som mottogs
	 */
	void onMessageReceived(String message);

	/**
	 * Den metod som internt anropas vid fel i kommunikationen med servern.
	 */
	void onConnectionError();
    }
}