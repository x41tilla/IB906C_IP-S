package whiteboard.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;

import whiteboard.service.servants.TransceivingServant;

/**
 * TODO
 *
 * @author
 *
 */
public class Service {

    private final TransceivingServant transceivingServant;

    /**
     * TODO
     *
     * @param localPort
     * @param remoteHost
     * @param remotePort
     * @throws SocketException
     */
    public Service(int localPort, InetAddress remoteHost, int remotePort)
	    throws SocketException {

	this.transceivingServant = new TransceivingServant(localPort,
		remoteHost, remotePort);
    }

    /**
     * TODO
     * 
     * @param delegate
     *
     */
    public void setDelegate(Delegate delegate) {
	this.transceivingServant
		.setDelegate(new TransceivingServant.Delegate() {

		    @Override
		    public void onDataReceived(byte[] data) {
			delegate.onDataReceived(data);
		    }

		    @Override
		    public void onIOException(IOException e) {
			delegate.onIOException(e);
		    }
		});

	// TODO
	Executors.newSingleThreadExecutor().submit(
		this.transceivingServant.getTask());

    }

    public void transmitData(byte[] data) {
	this.transceivingServant.transmitData(data);
	System.out.println("Transmitting!");
    }

    public boolean isServing() {
	// TODO
	return true;
    }

    @Override
    public String toString() {
	return null; // FIX
    }

    /**
     * TODO
     *
     * @author
     *
     */
    public interface Delegate extends TransceivingServant.Delegate {
	/*
	 * TODO
	 */
    }
}
