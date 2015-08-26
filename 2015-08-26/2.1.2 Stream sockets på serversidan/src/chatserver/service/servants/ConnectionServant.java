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
 * Tj�nar-klass som st�ndigt lyssnar p� och hanterar nya inkommande anslutningar
 * fr�n klienter till servern.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class ConnectionServant extends AbstractServant {

    /**
     * Serverns egna anslutning som skall lyssna p� inkommande
     * anslutningsf�rs�k.
     */
    private final ServerSocket serverSocket;

    /**
     * Skapar ett objekt av klassen {@link ConnectionServant}. Konstruktorn
     * kr�ver att f� tv� parametrar medskickade; den ena �r den
     * "call-back"-metod som tj�nar-objektet skall anropa i olika scenarion, den
     * andra �r den n�tverksport som tj�nar-objektet skall lyssna p�.
     * 
     * @param port
     *            den n�tverksport som det skall lyssnas p� f�r inkommande
     *            anslutningar fr�n klienter
     * @param listener
     *            TODO
     * @throws BindException
     *             ifall det inte gick att lyssna p� angiven n�tverksport
     * @throws IllegalArgumentException
     *             ifall angiven n�tverksport inte �r en giltig s�dan
     * @throws IOException
     *             ifall n�got annat g�r snett vid f�rs�k att lyssna p� angiven
     *             n�tverksport
     */
    public ConnectionServant(int port, Listener listener) throws BindException,
	    IllegalArgumentException, IOException {

	/*
	 * Referensen till serverns sida av anslutningen instansieras och b�rjar
	 * lyssna p� den av anv�ndaren specifierade n�tverksporten.
	 */
	this.serverSocket = new ServerSocket(port);

	this.task = new AbstractServantTask() {

	    @Override
	    void exit() {
		try {
		    ConnectionServant.this.serverSocket.close();
		} catch (IOException e) {
		    listener.onConnectionInitializationError("Fel vid avslut utav lyssnadnde p� n�tverksport f�r inkommande anslutningar.");
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
		 * Tj�nar-objektet lyssnar st�ndigt p� inkommande anslutningar
		 * fr�n nya klienter.
		 */
		try {
		    /*
		     * Lyckas en anslutning initialiseras mot en ny klient s�
		     * anropas l�mplig delegerad metod.
		     */
		    Socket newConnection = ConnectionServant.this.serverSocket
			    .accept();
		    listener.onNewConnection(newConnection);

		} catch (SocketException e) {
		    /*
		     * Detta undantag kastas ifall lyssningen p� inkommande
		     * anslutningar avbryts. Beh�ver i denna implementation inte
		     * hanteras.
		     */
		} catch (IOException e) {
		    /*
		     * Lyckas en anslutning inte initialiseras mot en ny klient
		     * s� anropas l�mplig delegerad metod.
		     */
		    listener.onConnectionInitializationError(e.getMessage());
		}
	    }
	};
    }

    /**
     * Metod som returnerar en str�ng som representerar tj�nar-objektet vid det
     * tillf�lle som metoden anropats. Lyckas tj�nar-objektet sl� upp serverns
     * host-adress s� returneras {@code [HOST-ADRESS]:[N�TVERKSPORT]}, i annat
     * fall s� returneras {@code UNRESOLVED HOST:[N�TVERKSPORT]}
     *
     * @return en representerande str�ng
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
     * Det till denna klass tillh�rande gr�nssnitt som beskriver de delegerade
     * metoder som skall deklareras externt, beroende p� hur det objekt som
     * anv�nder sig utav denna klass vill ha det.
     *
     * @author Atilla �zkan | 930304-4474 | atoz0393
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
	 * Den metod som skall anropas n�r en anslutning mot en ny klient
	 * initialiserats.
	 * 
	 * @param newConnection
	 *            den nya anslutningen
	 */
	void onNewConnection(Socket newConnection);
    }
}