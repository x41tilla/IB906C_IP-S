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
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Client {

    /**
     * Instansvariabler som refererar till klientens anslutning samt dess läsar-
     * och skrivar-objekt.
     */
    private final Socket socket;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Skapar ett objekt utav klassen {@link Client}.
     * 
     * @param clientsSocket
     *            den socket klienten är ansluten genom
     * @throws IOException
     *             ifall ett fel inträffar vid initalisering utav läsare samt
     *             skrivare till/från klienten
     */
    public Client(Socket clientsSocket) throws IOException {
	this.socket = clientsSocket;

	/*
	 * En ingående byte-ströms-objekt kopplas till anslutningen. En ingående
	 * byte-ströms läsare - med hårdkodad teckenuppsättning för
	 * kompatibilitet - som översätter byte-strömmen till en ström bestående
	 * utav tecken kopplas till byte-strömmen. Slutligen så kopplas en
	 * buffrad läsare till den ingående tecken-strömmen.
	 */
	InputStream inputStream = this.socket.getInputStream();
	InputStreamReader inputStreamReader = new InputStreamReader(
		inputStream, StandardCharsets.ISO_8859_1);
	this.reader = new BufferedReader(inputStreamReader);

	/*
	 * En mot anslutningen gående byte-ström initieras. En tecken-skrivare
	 * kopplad till byte-strömmen - med hårdkodad teckenuppsättning för
	 * kompatibilitet - instansieras och kopplas i sin tur till
	 * byte-strömmen.
	 */
	OutputStream outputStream = this.socket.getOutputStream();
	this.writer = new OutputStreamWriter(outputStream,
		StandardCharsets.ISO_8859_1);
    }

    /**
     * Metod som terminerar anslutningen till klienten.
     * 
     * @throws IOException
     *             ifall ett fel inträffar vid försök att terminera anslutningen
     */
    public final void kill() throws IOException {
	this.writer.close();
	this.reader.close();
	this.socket.close();
    }

    /**
     * Metod för att få klientens buffrade tecken-läsar-objekt.
     * 
     * @return klientens buffrade tecken-läsare
     */
    public final BufferedReader getReader() {
	return this.reader;
    }

    /**
     * Metod för att få klientens buffrade tecken-skrivar-objekt.
     * 
     * @return klientens buffrade tecken-skrivare
     */
    public final OutputStreamWriter getWriter() {
	return this.writer;
    }

    /**
     * Metod som returnerar klientens host-adress i form av en sträng.
     * 
     * @return klientens host-adress i form av en sträng
     */
    @Override
    public final String toString() {
	if (this.socket.isBound() && this.socket.isConnected())
	    return this.socket.getInetAddress().getHostAddress();
	return "Disconnected client";
    }
}
