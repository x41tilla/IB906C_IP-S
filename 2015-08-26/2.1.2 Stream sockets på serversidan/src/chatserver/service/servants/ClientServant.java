package chatserver.service.servants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EventListener;

/**
 * Tjänar-klass som ständigt lyssnar på och hanterar nya inkommande meddelanden
 * från en specifik ansluten klient.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class ClientServant extends AbstractServant {

    /**
     * Skapar ett objekt utav klassen {@link ClientServant}. Konstruktorn kräver
     * att få två parametrar medskickade; den ena är den "call-back"-metod som
     * tjänar-objektet skall anropa i olika scenarion, den andra är det buffrade
     * tecken-läsaren kopplad till klienten.
     * 
     * @param clientReader
     *            den buffrade tecken-läsaren kopplad till den klient som det
     *            skall lyssnas på
     * @param listener
     *            TODO
     */
    public ClientServant(BufferedReader clientReader, Listener listener) {

	this.task = new AbstractServantTask() {

	    @Override
	    void exit() {
		/*
		 * Utebliven handling vid avslut.
		 */
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
		 * Väntar på en rad från klienten. En rad avslutas per
		 * definition med ett '\n' eller '\r' eller också "\r\n".
		 * 
		 * Om ett null-värde returneras så är data-strömmen från
		 * klienten förmodligen stängd - i sådana fall uppmärksammas
		 * medlaren om det och objektet slutas köras.
		 * 
		 * Annars uppmärksammas medlaren om att ett nytt meddelande har
		 * lästs in och loopen fortsätter.
		 */
		String message;
		try {
		    if ((message = clientReader.readLine()) == null)
			listener.onConnectionLost();
		    else
			listener.onMessageReceived(message);

		} catch (IOException e) {
		    listener.onConnectionLost();
		}
	    }
	};

    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver de delegerade
     * metoder som skall deklareras externt, beroende på hur det objekt som
     * använder sig utav denna klass vill ha det.
     *
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Listener extends EventListener {
	/**
	 * Syftet av denna metod är att utföra lämplig operation när
	 * anslutningen till klienten har brutits
	 */
	void onConnectionLost();

	/**
	 * Syftet av denna metod är att utföra lämplig operation när ett
	 * meddelande har mottagits.
	 * 
	 * @param message
	 *            det meddelande som mottogs
	 */
	void onMessageReceived(String message);
    }
}