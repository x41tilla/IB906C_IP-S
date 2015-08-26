package chatclient.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chatclient.service.servants.MessageReceivingServant;
import chatclient.service.servants.MessageSendingServant;

/**
 * Klass utg�r den tj�nst som omfattar den data och kommunikation som �r
 * relevant till anslutningarna mellan klienten och servern.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Service {

    /*
     * Referens till den anslutning som skall uppr�ttas till servern.
     */
    private final Socket socket;

    /*
     * Referens till dem objekt som skall hantera mottagning samt uts�ndning
     * utav meddelanden.
     */
    private final MessageSendingServant messageSendingServant;
    private final MessageReceivingServant messageRecievingServant;

    /*
     * Anv�nder en tr�dpool med plats f�r upp till tv� stycken tr�dar, d� det
     * inte kr�vs mer.
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    /**
     * Skapar ett objekt av klassen {@link Service} som lyssnar p� en
     * specifierad port.
     * 
     * @param host
     *            den n�tverksadress som klienten skall ansluta till
     * @param port
     *            n�tverksporten som klienten skall ansluta till
     * @throws IOException
     *             vid problem orsakat n�r en socket instanserats f�r att lyssna
     *             p� best�md server.
     */
    public Service(InetAddress host, int port) throws IOException {

	/*
	 * Anslutningen till servern uppr�ttas.
	 */
	this.socket = new Socket(host, port);

	/*
	 * En ing�ende byte-str�ms-objekt kopplas till anslutningen. En ing�ende
	 * byte-str�ms l�sare - med h�rdkodad teckenupps�ttning f�r
	 * kompatibilitet - som �vers�tter byte-str�mmen till en str�m best�ende
	 * utav tecken kopplas till byte-str�mmen. Slutligen s� kopplas en
	 * buffrad l�sare till den ing�ende tecken-str�mmen.
	 */
	final InputStream inputStream;
	inputStream = this.socket.getInputStream();
	final InputStreamReader inputStreamReader;
	inputStreamReader = new InputStreamReader(inputStream,
		StandardCharsets.ISO_8859_1);
	final BufferedReader serverReader;
	serverReader = new BufferedReader(inputStreamReader);

	/*
	 * En utg�ende byte-str�m kopplas mot anslutningen. En tecken-skrivare
	 * kopplad till byte-str�mmen - med h�rdkodad teckenupps�ttning f�r
	 * kompatibilitet - instansieras och kopplas i sin tur till
	 * byte-str�mmen.
	 */
	final OutputStream serverOutputStream;
	serverOutputStream = this.socket.getOutputStream();
	final OutputStreamWriter serverWriter;
	serverWriter = new OutputStreamWriter(serverOutputStream,
		StandardCharsets.ISO_8859_1);

	/*
	 * Instansierar instansvariabeln som �r utav klassen
	 * MessageRecievingServant och som har i syfte att lyssna p� nya
	 * inkommande meddelanden fr�n servern. Instansieringen kr�ver att man
	 * skickar med en buffrad tecken-l�sare kopplad till servern.
	 */
	this.messageRecievingServant = new MessageReceivingServant(serverReader);

	/*
	 * Instansierar instansvariabeln som �r utav klassen
	 * MessageSendingServant och som har i syfte att skicka meddelanden till
	 * servern. Instansieringen kr�ver att man skickar med en buffrad
	 * tecken-skrivare kopplad till servern.
	 */
	this.messageSendingServant = new MessageSendingServant(serverWriter);
    }

    /**
     * Metod f�r att registrera den delegat av metoder som skall delegeras till
     * de tv� tj�nar-objekt - varav den ena lyssnar p� nya inkommande
     * meddelanden fr�n servern medan den andra skickar utg�ende meddelanden
     * till servern.
     *
     * @param delegate
     *            det delegat-objekt som skall delegeras vidare till
     *            tj�nar-objekten
     */
    public void setDelegate(Delegate delegate) {

	/*
	 * Registrerar de delegerade "call-back"-metoder som best�mmer vad det
	 * tj�nar-objekt �mnat �t att lyssna p� nya inkommande meddelanden skall
	 * g�ras i olika scenarion - i detta fall �r det tv�; n�r ett nytt
	 * meddelande mottagits och n�r ett avbrott i kommunikationen har �gt
	 * rum.
	 */
	this.messageRecievingServant
		.setDelegate(new MessageReceivingServant.Delegate() {

		    /*
		     * Vid ett mottaget meddelande s� meddelas f�rmedlaren och
		     * meddelandet skickas vidare till denna.
		     */
		    @Override
		    public void onMessageReceived(String message) {
			delegate.onMessageReceived(message);
		    }

		    /*
		     * Vid ett fel i anslutningen till servern som uppt�ckts vid
		     * f�rs�k att lyssna p� servern f�r nya inkommande
		     * meddelanden s� meddelas f�rmedlaren om detta och f�r ta
		     * l�mpliga �tg�rder.
		     */
		    @Override
		    public void onConnectionError() {
			delegate.onConnectionError();
		    }
		});

	/*
	 * Registrerar de delegerade "call-back"-metoder som best�mmer vad det
	 * tj�nar-objekt �mnat �t att skicka meddelanden till servern skall
	 * g�ras i olika scenarion - i detta fall �r det en; n�r ett avbrott i
	 * kommunikationen har �gt rum.
	 */
	this.messageSendingServant
		.setDelegate(new MessageSendingServant.Delegate() {

		    /*
		     * Vid ett fel i anslutningen till servern som uppt�ckts vid
		     * f�rs�k att skicka ett meddelande till servern s� meddelas
		     * f�rmedlaren om detta och f�r ta l�mpliga �tg�rder.
		     */
		    @Override
		    public void onConnectionError() {
			delegate.onConnectionError();
		    }
		});
    }

    /**
     * Metod f�r att skicka ett meddelande till servern.
     *
     * @param message
     *            meddelandet som ska skickas
     */
    public void sendMessage(String message) {
	this.messageSendingServant.addMessageToQueue(message);
    }

    /**
     * Metod som returnerar ett booleansk v�rde baserat p� om tj�nsten �r redo
     * eller ej. Om tj�nsten �r redo eller inte baseras p� om dess underliggande
     * komponenter �r redo - det kr�vs att dessa �r instansierade, att dessa
     * k�rs i varsinn tr�d och att dessa p� s� s�tt �r redo att bearbeta data.
     *
     * @return {@code true} om tj�nsten �r redo, i annat fall {@code false}
     */
    public boolean isServing() {
	if (this.socket != null)
	    if (this.socket.isBound() && this.socket.isConnected())
		if (this.messageRecievingServant != null
			&& this.messageSendingServant != null)
		    if (this.messageRecievingServant.isServing()
			    && this.messageSendingServant.isServing())
			return true;
	return false;
    }

    /**
     * Metod f�r att starta tj�nsten. Om tj�nsten redan k�rs s� h�nder inget.
     *
     */
    public void startServing() {

	/*
	 * Kontrollerar ifall tj�nsten redan k�rs. Om s� fallet s� g�r man ur
	 * metoden.
	 */
	if (isServing())
	    return;

	/*
	 * L�gger till uppdragen tillh�randes tj�nar-objekten till tr�dpoolen
	 * f�r samtidig exekvering.
	 */
	this.threadPool.submit(this.messageRecievingServant.getTask());
	this.threadPool.submit(this.messageSendingServant.getTask());
    }

    /**
     * Metod f�r att stoppa tj�nsten.
     */
    public void stopServing() {
	/*
	 * Stoppar tj�narna - b�de den som hanterar utg�ende och den som
	 * hanterar inkommande meddelanden - fr�n att k�ras.
	 */
	this.messageRecievingServant.stopServing();
	this.messageSendingServant.stopServing();

	try {
	    /*
	     * F�rs�ker st�nga anslutningen till servern.
	     */
	    this.socket.close();
	} catch (IOException e) {
	    /*
	     * Problem vid st�ngning av anslutningen - finns inte mycket att
	     * g�ra �t detta.
	     */
	}
    }

    /**
     * Metod som bygger upp och returnerar en str�ng som representerar klientens
     * aktuella uppkopplingsstatus.
     * 
     * @return str�ng som representerar klientens uppkopplingsstatus
     */
    @Override
    public String toString() {
	return this.socket.getRemoteSocketAddress().toString();
    }

    /**
     * Det till denna klass tillh�rande gr�nssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) skall delegeras vidare till
     * tj�nar-objekten.
     *
     * @author Atilla �zkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate extends MessageReceivingServant.Delegate,
	    MessageSendingServant.Delegate {
	/*
	 * D� de delegerade metoderna h�rstammar fr�n tj�nar-objekten s� ut�kar
	 * enbart denna gr�nssnitt de tv� redan befintliga gr�nssnittet hos
	 * b�gge tj�nar-klasser.
	 */
    }
}
