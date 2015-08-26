package chatserver.service.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Klass som representerar en ansluten klient.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Client {

    /**
     * Instansvariabler som refererar till klientens anslutning samt dess l�sar-
     * och skrivar-objekt.
     */
    private final Socket socket;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Skapar ett objekt utav klassen {@link Client}.
     * 
     * @param clientsSocket
     *            den socket klienten �r ansluten genom
     * @throws IOException
     *             ifall ett fel intr�ffar vid initalisering utav l�sare samt
     *             skrivare till/fr�n klienten
     */
    public Client(Socket clientsSocket) throws IOException {
	this.socket = clientsSocket;

	/*
	 * En ing�ende byte-str�ms-objekt kopplas till anslutningen. En ing�ende
	 * byte-str�ms l�sare - med h�rdkodad teckenupps�ttning f�r
	 * kompatibilitet - som �vers�tter byte-str�mmen till en str�m best�ende
	 * utav tecken kopplas till byte-str�mmen. Slutligen s� kopplas en
	 * buffrad l�sare till den ing�ende tecken-str�mmen.
	 */
	InputStream inputStream = this.socket.getInputStream();
	InputStreamReader inputStreamReader = new InputStreamReader(
		inputStream, StandardCharsets.ISO_8859_1);
	this.reader = new BufferedReader(inputStreamReader);

	/*
	 * En mot anslutningen g�ende byte-str�m initieras. En tecken-skrivare
	 * kopplad till byte-str�mmen - med h�rdkodad teckenupps�ttning f�r
	 * kompatibilitet - instansieras och kopplas i sin tur till
	 * byte-str�mmen.
	 */
	OutputStream outputStream = this.socket.getOutputStream();
	this.writer = new OutputStreamWriter(outputStream,
		StandardCharsets.ISO_8859_1);
    }

    /**
     * Metod som terminerar anslutningen till klienten.
     * 
     * @throws IOException
     *             ifall ett fel intr�ffar vid f�rs�k att terminera anslutningen
     */
    public final void kill() throws IOException {
	this.writer.close();
	this.reader.close();
	this.socket.close();
    }

    /**
     * Metod f�r att f� klientens buffrade tecken-l�sar-objekt.
     * 
     * @return klientens buffrade tecken-l�sare
     */
    public final BufferedReader getReader() {
	return this.reader;
    }

    /**
     * Metod f�r att f� klientens buffrade tecken-skrivar-objekt.
     * 
     * @return klientens buffrade tecken-skrivare
     */
    public final OutputStreamWriter getWriter() {
	return this.writer;
    }

    /**
     * Metod som returnerar klientens host-adress i form av en str�ng.
     * 
     * @return klientens host-adress i form av en str�ng
     */
    @Override
    public final String toString() {
	if (this.socket.isBound() && this.socket.isConnected())
	    return this.socket.getInetAddress().getHostAddress();
	return "Disconnected client";
    }
}
