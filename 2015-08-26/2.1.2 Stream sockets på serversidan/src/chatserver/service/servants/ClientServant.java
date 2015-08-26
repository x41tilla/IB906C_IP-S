package chatserver.service.servants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EventListener;

/**
 * Tj�nar-klass som st�ndigt lyssnar p� och hanterar nya inkommande meddelanden
 * fr�n en specifik ansluten klient.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class ClientServant extends AbstractServant {

    /**
     * Skapar ett objekt utav klassen {@link ClientServant}. Konstruktorn kr�ver
     * att f� tv� parametrar medskickade; den ena �r den "call-back"-metod som
     * tj�nar-objektet skall anropa i olika scenarion, den andra �r det buffrade
     * tecken-l�saren kopplad till klienten.
     * 
     * @param clientReader
     *            den buffrade tecken-l�saren kopplad till den klient som det
     *            skall lyssnas p�
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
		 * V�ntar p� en rad fr�n klienten. En rad avslutas per
		 * definition med ett '\n' eller '\r' eller ocks� "\r\n".
		 * 
		 * Om ett null-v�rde returneras s� �r data-str�mmen fr�n
		 * klienten f�rmodligen st�ngd - i s�dana fall uppm�rksammas
		 * medlaren om det och objektet slutas k�ras.
		 * 
		 * Annars uppm�rksammas medlaren om att ett nytt meddelande har
		 * l�sts in och loopen forts�tter.
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
     * Det till denna klass tillh�rande gr�nssnitt som beskriver de delegerade
     * metoder som skall deklareras externt, beroende p� hur det objekt som
     * anv�nder sig utav denna klass vill ha det.
     *
     * @author Atilla �zkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Listener extends EventListener {
	/**
	 * Syftet av denna metod �r att utf�ra l�mplig operation n�r
	 * anslutningen till klienten har brutits
	 */
	void onConnectionLost();

	/**
	 * Syftet av denna metod �r att utf�ra l�mplig operation n�r ett
	 * meddelande har mottagits.
	 * 
	 * @param message
	 *            det meddelande som mottogs
	 */
	void onMessageReceived(String message);
    }
}