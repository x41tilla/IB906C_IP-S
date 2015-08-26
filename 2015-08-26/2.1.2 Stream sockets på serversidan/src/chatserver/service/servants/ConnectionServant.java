package chatserver.service.servants;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.EventListener;

/**
 * Tjänar-klass som ständigt lyssnar på och hanterar nya inkommande anslutningar
 * från klienter till servern.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class ConnectionServant extends AbstractServant {

    /**
     * Serverns egna anslutning som skall lyssna på inkommande
     * anslutningsförsök.
     */
    private final ServerSocket serverSocket;

    /**
     * Skapar ett objekt av klassen {@link ConnectionServant}. Konstruktorn
     * kräver att få två parametrar medskickade; den ena är den
     * "call-back"-metod som tjänar-objektet skall anropa i olika scenarion, den
     * andra är den nätverksport som tjänar-objektet skall lyssna på.
     * 
     * @param port
     *            den nätverksport som det skall lyssnas på för inkommande
     *            anslutningar från klienter
     * @param listener
     *            TODO
     * @throws BindException
     *             ifall det inte gick att lyssna på angiven nätverksport
     * @throws IllegalArgumentException
     *             ifall angiven nätverksport inte är en giltig sådan
     * @throws IOException
     *             ifall något annat går snett vid försök att lyssna på angiven
     *             nätverksport
     */
    public ConnectionServant(int port, Listener listener) throws BindException,
	    IllegalArgumentException, IOException {

	/*
	 * Referensen till serverns sida av anslutningen instansieras och börjar
	 * lyssna på den av användaren specifierade nätverksporten.
	 */
	this.serverSocket = new ServerSocket(port);

	this.task = new AbstractServantTask() {

	    @Override
	    void exit() {
		try {
		    ConnectionServant.this.serverSocket.close();
		} catch (IOException e) {
		    listener.onConnectionInitializationError("Fel vid avslut utav lyssnadnde på nätverksport för inkommande anslutningar.");
		}
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
		 * Tjänar-objektet lyssnar ständigt på inkommande anslutningar
		 * från nya klienter.
		 */
		try {
		    /*
		     * Lyckas en anslutning initialiseras mot en ny klient så
		     * anropas lämplig delegerad metod.
		     */
		    Socket newConnection = ConnectionServant.this.serverSocket
			    .accept();
		    listener.onNewConnection(newConnection);

		} catch (SocketException e) {
		    /*
		     * Detta undantag kastas ifall lyssningen på inkommande
		     * anslutningar avbryts. Behöver i denna implementation inte
		     * hanteras.
		     */
		} catch (IOException e) {
		    /*
		     * Lyckas en anslutning inte initialiseras mot en ny klient
		     * så anropas lämplig delegerad metod.
		     */
		    listener.onConnectionInitializationError(e.getMessage());
		}
	    }
	};
    }

    /**
     * Metod som returnerar en sträng som representerar tjänar-objektet vid det
     * tillfälle som metoden anropats. Lyckas tjänar-objektet slå upp serverns
     * host-adress så returneras {@code [HOST-ADRESS]:[NÄTVERKSPORT]}, i annat
     * fall så returneras {@code UNRESOLVED HOST:[NÄTVERKSPORT]}
     *
     * @return en representerande sträng
     */
    @Override
    public final String toString() {

	StringBuilder representation = new StringBuilder();
	try {
	    representation.append(InetAddress.getLocalHost().getHostName());
	} catch (UnknownHostException e) {
	    representation.append("UNRESOLVED HOST");
	}
	representation.append(":");
	representation.append(ConnectionServant.this.serverSocket
		.getLocalPort());
	return representation.toString();
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
	 * Den metod som skall anropas vid fel i kommunikationen med en klient.
	 * 
	 * @param errorMessage
	 *            ett felmeddelande som skickas vidare till det objekt som
	 *            deklarerat metoden
	 */
	void onConnectionInitializationError(String errorMessage);

	/**
	 * Den metod som skall anropas när en anslutning mot en ny klient
	 * initialiserats.
	 * 
	 * @param newConnection
	 *            den nya anslutningen
	 */
	void onNewConnection(Socket newConnection);
    }
}