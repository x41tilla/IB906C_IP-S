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
 * Klass utgör den tjänst som omfattar den data och kommunikation som är
 * relevant till anslutningarna mellan klienten och servern.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Service {

    /*
     * Referens till den anslutning som skall upprättas till servern.
     */
    private final Socket socket;

    /*
     * Referens till dem objekt som skall hantera mottagning samt utsändning
     * utav meddelanden.
     */
    private final MessageSendingServant messageSendingServant;
    private final MessageReceivingServant messageRecievingServant;

    /*
     * Använder en trådpool med plats för upp till två stycken trådar, då det
     * inte krävs mer.
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    /**
     * Skapar ett objekt av klassen {@link Service} som lyssnar på en
     * specifierad port.
     * 
     * @param host
     *            den nätverksadress som klienten skall ansluta till
     * @param port
     *            nätverksporten som klienten skall ansluta till
     * @throws IOException
     *             vid problem orsakat när en socket instanserats för att lyssna
     *             på bestämd server.
     */
    public Service(InetAddress host, int port) throws IOException {

	/*
	 * Anslutningen till servern upprättas.
	 */
	this.socket = new Socket(host, port);

	/*
	 * En ingående byte-ströms-objekt kopplas till anslutningen. En ingående
	 * byte-ströms läsare - med hårdkodad teckenuppsättning för
	 * kompatibilitet - som översätter byte-strömmen till en ström bestående
	 * utav tecken kopplas till byte-strömmen. Slutligen så kopplas en
	 * buffrad läsare till den ingående tecken-strömmen.
	 */
	final InputStream inputStream;
	inputStream = this.socket.getInputStream();
	final InputStreamReader inputStreamReader;
	inputStreamReader = new InputStreamReader(inputStream,
		StandardCharsets.ISO_8859_1);
	final BufferedReader serverReader;
	serverReader = new BufferedReader(inputStreamReader);

	/*
	 * En utgående byte-ström kopplas mot anslutningen. En tecken-skrivare
	 * kopplad till byte-strömmen - med hårdkodad teckenuppsättning för
	 * kompatibilitet - instansieras och kopplas i sin tur till
	 * byte-strömmen.
	 */
	final OutputStream serverOutputStream;
	serverOutputStream = this.socket.getOutputStream();
	final OutputStreamWriter serverWriter;
	serverWriter = new OutputStreamWriter(serverOutputStream,
		StandardCharsets.ISO_8859_1);

	/*
	 * Instansierar instansvariabeln som är utav klassen
	 * MessageRecievingServant och som har i syfte att lyssna på nya
	 * inkommande meddelanden från servern. Instansieringen kräver att man
	 * skickar med en buffrad tecken-läsare kopplad till servern.
	 */
	this.messageRecievingServant = new MessageReceivingServant(serverReader);

	/*
	 * Instansierar instansvariabeln som är utav klassen
	 * MessageSendingServant och som har i syfte att skicka meddelanden till
	 * servern. Instansieringen kräver att man skickar med en buffrad
	 * tecken-skrivare kopplad till servern.
	 */
	this.messageSendingServant = new MessageSendingServant(serverWriter);
    }

    /**
     * Metod för att registrera den delegat av metoder som skall delegeras till
     * de två tjänar-objekt - varav den ena lyssnar på nya inkommande
     * meddelanden från servern medan den andra skickar utgående meddelanden
     * till servern.
     *
     * @param delegate
     *            det delegat-objekt som skall delegeras vidare till
     *            tjänar-objekten
     */
    public void setDelegate(Delegate delegate) {

	/*
	 * Registrerar de delegerade "call-back"-metoder som bestämmer vad det
	 * tjänar-objekt ämnat åt att lyssna på nya inkommande meddelanden skall
	 * göras i olika scenarion - i detta fall är det två; när ett nytt
	 * meddelande mottagits och när ett avbrott i kommunikationen har ägt
	 * rum.
	 */
	this.messageRecievingServant
		.setDelegate(new MessageReceivingServant.Delegate() {

		    /*
		     * Vid ett mottaget meddelande så meddelas förmedlaren och
		     * meddelandet skickas vidare till denna.
		     */
		    @Override
		    public void onMessageReceived(String message) {
			delegate.onMessageReceived(message);
		    }

		    /*
		     * Vid ett fel i anslutningen till servern som upptäckts vid
		     * försök att lyssna på servern för nya inkommande
		     * meddelanden så meddelas förmedlaren om detta och får ta
		     * lämpliga åtgärder.
		     */
		    @Override
		    public void onConnectionError() {
			delegate.onConnectionError();
		    }
		});

	/*
	 * Registrerar de delegerade "call-back"-metoder som bestämmer vad det
	 * tjänar-objekt ämnat åt att skicka meddelanden till servern skall
	 * göras i olika scenarion - i detta fall är det en; när ett avbrott i
	 * kommunikationen har ägt rum.
	 */
	this.messageSendingServant
		.setDelegate(new MessageSendingServant.Delegate() {

		    /*
		     * Vid ett fel i anslutningen till servern som upptäckts vid
		     * försök att skicka ett meddelande till servern så meddelas
		     * förmedlaren om detta och får ta lämpliga åtgärder.
		     */
		    @Override
		    public void onConnectionError() {
			delegate.onConnectionError();
		    }
		});
    }

    /**
     * Metod för att skicka ett meddelande till servern.
     *
     * @param message
     *            meddelandet som ska skickas
     */
    public void sendMessage(String message) {
	this.messageSendingServant.addMessageToQueue(message);
    }

    /**
     * Metod som returnerar ett booleansk värde baserat på om tjänsten är redo
     * eller ej. Om tjänsten är redo eller inte baseras på om dess underliggande
     * komponenter är redo - det krävs att dessa är instansierade, att dessa
     * körs i varsinn tråd och att dessa på så sätt är redo att bearbeta data.
     *
     * @return {@code true} om tjänsten är redo, i annat fall {@code false}
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
     * Metod för att starta tjänsten. Om tjänsten redan körs så händer inget.
     *
     */
    public void startServing() {

	/*
	 * Kontrollerar ifall tjänsten redan körs. Om så fallet så går man ur
	 * metoden.
	 */
	if (isServing())
	    return;

	/*
	 * Lägger till uppdragen tillhörandes tjänar-objekten till trådpoolen
	 * för samtidig exekvering.
	 */
	this.threadPool.submit(this.messageRecievingServant.getTask());
	this.threadPool.submit(this.messageSendingServant.getTask());
    }

    /**
     * Metod för att stoppa tjänsten.
     */
    public void stopServing() {
	/*
	 * Stoppar tjänarna - både den som hanterar utgående och den som
	 * hanterar inkommande meddelanden - från att köras.
	 */
	this.messageRecievingServant.stopServing();
	this.messageSendingServant.stopServing();

	try {
	    /*
	     * Försöker stänga anslutningen till servern.
	     */
	    this.socket.close();
	} catch (IOException e) {
	    /*
	     * Problem vid stängning av anslutningen - finns inte mycket att
	     * göra åt detta.
	     */
	}
    }

    /**
     * Metod som bygger upp och returnerar en sträng som representerar klientens
     * aktuella uppkopplingsstatus.
     * 
     * @return sträng som representerar klientens uppkopplingsstatus
     */
    @Override
    public String toString() {
	return this.socket.getRemoteSocketAddress().toString();
    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) skall delegeras vidare till
     * tjänar-objekten.
     *
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate extends MessageReceivingServant.Delegate,
	    MessageSendingServant.Delegate {
	/*
	 * Då de delegerade metoderna härstammar från tjänar-objekten så utökar
	 * enbart denna gränssnitt de två redan befintliga gränssnittet hos
	 * bägge tjänar-klasser.
	 */
    }
}
