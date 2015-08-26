package chatserver.service.misc;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Klass som representerar ett uts�ndningsuppdrag adresserad till en specifik
 * klients buffrade tecken-l�sare.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class BroadcastTask implements Runnable {
    /**
     * Instansvariabler som representerar den buffrade tecken-skrivar objekt
     * riktad mot den adresserade klienten samt det meddelande som skall
     * skickas.
     */
    private final OutputStreamWriter writerToReciever;
    private final String messageToBroadcast;
    private final Events clientsDelegate;

    /**
     * Skapar ett objekt av klassen {@link BroadcastTask}.
     * 
     * @param events
     *            TODO
     * 
     * @param writerToReciever
     *            den adresserade klientens buffrade tecken-skrivar objekt att
     *            skicka meddelandet genom
     * @param messageToBroadcast
     *            det meddelande som skall skickas
     */
    public BroadcastTask(Events events, OutputStreamWriter writerToReciever,
	    String messageToBroadcast) {
	this.clientsDelegate = events;
	this.writerToReciever = writerToReciever;
	this.messageToBroadcast = messageToBroadcast;
    }

    @Override
    public void run() {

	/*
	 * P� ett sanerat och ordningssamt s�tt - genom att "spola" str�mmens
	 * buffer f�r att f�rhindra att ett meddelande ligger och v�ntar -
	 * f�rs�ks meddelandet skrivas via den mottagande klientens
	 * skrivar-objekt. Tecknet f�r radbryte indikerar slutet p� meddelandet.
	 */
	try {
	    this.writerToReciever.write(this.messageToBroadcast + "\n");
	    this.writerToReciever.flush();
	} catch (IOException e) {
	    /*
	     * St�ter programmet p� ett undantag s� betyder det att det inte
	     * gick att kommunicera med mottagaren - denne �r d� h�gst troligen
	     * inte l�nge uppkopplad och d�rf�r anropas den i f�rv�g definierade
	     * metod som �r till f�r att anropas i situationer som denna.
	     */
	    this.clientsDelegate.onConnectionLost();
	}
    }

    /**
     * Det till denna klass tillh�rande gr�nssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som skall anropas vid olika scenarion.
     * 
     * @author Atilla �zkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Events {
	/**
	 * Den metod som skall anropas vid fel i kommunikationen med en klient.
	 */
	void onConnectionLost();
    }
}