package whiteboard.service.servants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.EventListener;

/**
 * Tjänar-klass som har i syfte att lyssna på inkommande data, samt kunna skicka
 * sådan.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class TransceivingServant extends
	AbstractServant<TransceivingServant.Delegate> {

    private final DatagramSocket socket;
    private final DatagramPacket inPacket;
    private final DatagramPacket outPacket;

    /**
     * TODO
     * 
     * @param localPort
     * @param remotePort
     * @param remoteHost
     * 
     * @throws SocketException
     *
     */
    public TransceivingServant(int localPort, InetAddress remoteHost,
	    int remotePort) throws SocketException {

	this.socket = new DatagramSocket(localPort);
	this.inPacket = new DatagramPacket(new byte[50], 50);
	this.outPacket = new DatagramPacket(new byte[50], 50, remoteHost,
		remotePort);

	/*
	 * Delegat-objektet instanseras till att inte göra något vid någon
	 * händelse över huvud taget. Detta för att undvika att instansvariabeln
	 * istället pekar mot null.
	 */
	this.delegate = new Delegate() {

	    @Override
	    public void onDataReceived(byte[] data) {
		/*
		 * Utebliven handling.
		 */
	    }

	    @Override
	    public void onIOException(IOException e) {
		/*
		 * Utebliven handling.
		 */
	    }
	};
    }

    @Override
    void init() {
	// TODO

    }

    @Override
    void serve() {
	try {
	    this.socket.receive(this.inPacket);
	    byte[] data = this.inPacket.getData();
	    this.delegate.onDataReceived(data);
	} catch (IOException e) {
	    this.delegate.onIOException(e);
	}
    }

    @Override
    void exit() {
	this.socket.close();
    }

    /**
     * TODO
     *
     * @param data
     */
    public void transmitData(byte[] data) {
	this.outPacket.setData(data);
	try {
	    this.socket.send(this.outPacket);
	} catch (IOException e) {
	    this.delegate.onIOException(e);
	}
    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som skall anropas vid olika scenarion.
     * 
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate extends EventListener {
	void onDataReceived(byte[] data);

	void onIOException(IOException e);
    }
}
