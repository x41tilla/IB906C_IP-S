package chatclient.service.servants;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.EventListener;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Tj�nar-klass som hanterar dem meddelanden som v�ntar p� att skickas ut till
 * servern, samt utskickandet utav dessa.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class MessageSendingServant extends
	AbstractServant<MessageSendingServant.Delegate> {

    /**
     * En "blockerande" k�-struktur f�r att lagra meddelanden i den ordning
     * (First-In-First-Out) som dem ska skickas till servern. Dess "blockerande"
     * egenskap �r l�mplig i det h�r fallet d� det �r en - fr�n
     * till�mpningsprogrammet separat - tr�d som skall hantera meddelandena
     * vilket till�ter att tr�den p� l�mpligt s�tt v�ntar p� att k�-strukturen
     * populeras med meddelanden allt eftersom att tr�den skickar dem.
     */
    private final LinkedBlockingQueue<String> outgoingMessages;

    /**
     * Referns till en buffrad tecken-skrivare kopplad till servern. Denna
     * anv�nds f�r att skicka utg�ende meddelanden till.
     */
    private final OutputStreamWriter serverWriter;

    /**
     * Skapar ett objekt utav klassen {@link MessageSendingServant}.
     * Konstruktorn kr�ver att f� en referens till den buffrade tecken-skrivaren
     * som tillh�r servern klienten �r ansluten till.
     * 
     * @param serverWriter
     *            den buffrade tecken-skrivaren tillh�rande den server som
     *            meddelandena skall skickas till
     */
    public MessageSendingServant(OutputStreamWriter serverWriter) {

	/*
	 * Referensen till den buffrade tecken-skrivare kopplad till servern
	 * uppdateras.
	 */
	this.serverWriter = serverWriter;

	/*
	 * Instansierar k�-strukturen som har i syfte att populeras med
	 * utskicksjobb samtidigt som jobben utf�rs en efter en - i r�tt
	 * ordning.
	 */
	this.outgoingMessages = new LinkedBlockingQueue<>();

	/*
	 * Delegat-objektet instanseras till att inte g�ra n�got vid n�gon
	 * h�ndelse �ver huvud taget. Detta f�r att undvika att instansvariabeln
	 * ist�llet pekar mot null.
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
	 * F�rs�ker plocka n�sta utskicksjobb som st�r p� tur att utf�ras. Finns
	 * inga s� v�ntas det - h�r.
	 */
	String outgoingMessage = MessageSendingServant.this.outgoingMessages
		.take();

	/*
	 * P� ett sanerat och ordningssamt s�tt - genom att "spola" str�mmens
	 * buffer f�r att f�rhindra att ett meddelande ligger och v�ntar -
	 * f�rs�ks meddelandet skrivas via den mottagande serverns
	 * skrivar-objekt. Tecknet f�r radbryte indikerar slutet p� meddelandet.
	 */
	try {
	    this.serverWriter.write(outgoingMessage + "\r");
	    this.serverWriter.flush();
	} catch (IOException e) {
	    /*
	     * St�ter programmet p� ett undantag s� betyder det att det inte
	     * gick att kommunicera med mottagaren - denne �r d� h�gst troligen
	     * inte l�nge uppkopplad och d�rf�r anropas den i f�rv�g definierade
	     * metod som �r till f�r att anropas i situationer som denna.
	     */
	    MessageSendingServant.this.delegate.onConnectionError();
	}

    }

    @Override
    void exit() {
	try {
	    /*
	     * Terminerar resurser som �r kopplade till servern.
	     */
	    this.serverWriter.close();
	} catch (IOException e) {
	    /*
	     * Problem vid st�ngning av den buffrade tecken-skrivaren - finns
	     * inte mycket att g�ra �t detta.
	     */
	}
    }

    /**
     * Metod f�r att placera ett nytt meddelande i k�n �ver utskicksjobb som
     * v�ntar p� att utf�ras.
     *
     * @param message
     *            det meddelande som skall skickas ut
     */
    public void addMessageToQueue(String message) {
	this.outgoingMessages.offer(message);
    }

    /**
     * Metod som returnerar en str�ng som representerar tj�nar-objektet vid det
     * tillf�lle som metoden anropats; denna represantation best�r i detta fall
     * utav antalet utskicksjobb som v�ntar p� att hanteras.
     * 
     * @return en representerande str�ng
     */
    @Override
    public final String toString() {
	final String numberOfAwaitingMessages;
	numberOfAwaitingMessages = String.valueOf(this.outgoingMessages.size());
	return numberOfAwaitingMessages;
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
	 * Den metod som internt anropas vid fel i kommunikationen med servern.
	 */
	void onConnectionError();
    }
}
