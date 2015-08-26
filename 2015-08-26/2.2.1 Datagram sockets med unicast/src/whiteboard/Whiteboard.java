package whiteboard;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import whiteboard.gui.GUI;
import whiteboard.mediator.Mediator;
import whiteboard.service.Service;

/**
 * TODO
 *
 * @author
 *
 */
public class Whiteboard {

    /**
     * TODO
     *
     * @param localPort
     * @param remoteHost
     * @param remotePort
     * @throws InterruptedException
     *             om bygget utav det grafiska användargränssnittet avbröts
     * @throws ExecutionException
     *             om bygget utav det grafiska användargränssnittet inte kunde
     *             slutföras
     * @throws SocketException
     */
    public Whiteboard(int localPort, InetAddress remoteHost, int remotePort)
	    throws InterruptedException, ExecutionException, SocketException {

	/*
	 * Instansierar ett nytt objekt utav tjänst-klassen.
	 */
	final Service service = new Service(localPort, remoteHost, remotePort);

	/*
	 * Instansierar ett nytt objekt utav den grafiska
	 * användergränssnitts-klassen.
	 */
	final GUI gui = new GUI();

	/*
	 * Instansierar ett nytt objekt utav den mellanliggande
	 * förmedlar-klassen. Registrerar sedan ovanstående två komponenter.
	 */
	final Mediator mediator = new Mediator();
	mediator.registerService(service);
	mediator.registerGUI(gui);
    }

    /**
     * TODO
     *
     * @param args
     */
    public static void main(String[] args) {
	final int localPort;
	final InetAddress remoteHost;
	final int remotePort;

	try {
	    if (args.length == 3) {

		localPort = Integer.parseInt(args[0]);
		remoteHost = InetAddress.getByName(args[1]);
		remotePort = Integer.parseInt(args[2]);

		new Whiteboard(localPort, remoteHost, remotePort);
	    } else
		System.out
			.println("Användning:\nWhiteboard [LOKAL PORT] [DEST. ADRESS] [DEST. PORT]");
	} catch (UnknownHostException e) {
	    /*
	     * Ifall angiven nätverksadress inte går att uppsöka.
	     */
	    System.out.println("Angiven nätverksadress går ej att uppsöka!");
	} catch (IllegalArgumentException e) {
	    /*
	     * Ifall angiven nätverksport inte går att översätta till siffror
	     * eller inte är giltig.
	     */
	    System.out
		    .println("Nätverksporten ska bestå utav enbart siffror inom intervallet 1-65535!");
	} catch (InterruptedException e) {
	    System.out
		    .println("Uppbygget utav det grafiska användargränssnittet avbröts:");
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    System.out
		    .println("Uppbygget utav det grafiska användargränssnittet kunde inte slutföras:");
	    e.printStackTrace();
	} catch (SocketException e) {
	    // TODO
	    e.printStackTrace();
	}
    }
}
