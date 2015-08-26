package chatclient.service.servants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EventListener;

/**
 * Tj�nar-klass som st�ndigt lyssnar p� och hanterar nya inkommande meddelanden
 * fr�n servern.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class MessageReceivingServant extends
	AbstractServant<MessageReceivingServant.Delegate> {

    /**
     * Referns till en buffrad tecken-l�sare kopplad till servern. Denna anv�nds
     * f�r att l�sa inkommande meddelanden ifr�n.
     */
    private final BufferedReader serverReader;

    /**
     * Skapar ett objekt utav klassen {@link MessageReceivingServant}.
     * Konstruktorn kr�ver att f� en referens till den buffrade tecken-l�saren
     * som tillh�r servern klienten �r ansluten till.
     * 
     * @param serverReader
     *            den buffrade tecken-l�saren tillh�rande den server som
     *            meddelandena skall skickas till
     */
    public MessageReceivingServant(BufferedReader serverReader) {

	/*
	 * Referensen till den buffrade tecken-l�saren kopplad till servern
	 * uppdateras.
	 */
	this.serverReader = serverReader;

	/*
	 * Delegat-objektet instanseras till att inte g�ra n�got vid n�gon
	 * h�ndelse �ver huvud taget. Detta f�r att undvika att instansvariabeln
	 * ist�llet pekar mot null.
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
	 * Loop som v�ntar p� en rad fr�n servern. En rad avslutas per
	 * definition med ett '\n' eller '\r' eller ocks� "\r\n".
	 * 
	 * Om ett null-v�rde returneras s� �r data-str�mmen fr�n servern
	 * f�rmodligen st�ngd - i s�dana fall uppm�rksammas medlaren om det och
	 * objektet slutas k�ras.
	 * 
	 * Annars uppm�rksammas medlaren om att ett nytt meddelande har l�sts in
	 * och loopen forts�tter.
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
	     * Terminerar resurser som �r kopplade till servern.
	     */
	    this.serverReader.close();
	} catch (IOException e) {
	    /*
	     * Problem vid st�ngning av den buffrade tecken-l�saren - finns inte
	     * mycket att g�ra �t detta.
	     */
	}
    }

    @Override
    public String toString() {
	return this.isServing() ? "Listening on incoming messages from the server..."
		: "Standing by...";
    }

    /**
     * Det till denna klass tillh�rande gr�nssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som internt skall anropas av det �gande
     * objeket vid olika scenarion.
     * 
     * @author Atilla �zkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate extends EventListener {
	/**
	 * Den metod som internt anropas n�r ett meddelande mottagits fr�n
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