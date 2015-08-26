package chatserver.service.misc;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Klass som representerar ett utsändningsuppdrag adresserad till en specifik
 * klients buffrade tecken-läsare.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
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
	 * På ett sanerat och ordningssamt sätt - genom att "spola" strömmens
	 * buffer för att förhindra att ett meddelande ligger och väntar -
	 * försöks meddelandet skrivas via den mottagande klientens
	 * skrivar-objekt. Tecknet för radbryte indikerar slutet på meddelandet.
	 */
	try {
	    this.writerToReciever.write(this.messageToBroadcast + "\n");
	    this.writerToReciever.flush();
	} catch (IOException e) {
	    /*
	     * Stöter programmet på ett undantag så betyder det att det inte
	     * gick att kommunicera med mottagaren - denne är då högst troligen
	     * inte länge uppkopplad och därför anropas den i förväg definierade
	     * metod som är till för att anropas i situationer som denna.
	     */
	    this.clientsDelegate.onConnectionLost();
	}
    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som skall anropas vid olika scenarion.
     * 
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Events {
	/**
	 * Den metod som skall anropas vid fel i kommunikationen med en klient.
	 */
	void onConnectionLost();
    }
}