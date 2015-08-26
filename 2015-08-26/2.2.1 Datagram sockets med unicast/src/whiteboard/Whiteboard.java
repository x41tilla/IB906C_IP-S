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
     *             om bygget utav det grafiska anv�ndargr�nssnittet avbr�ts
     * @throws ExecutionException
     *             om bygget utav det grafiska anv�ndargr�nssnittet inte kunde
     *             slutf�ras
     * @throws SocketException
     */
    public Whiteboard(int localPort, InetAddress remoteHost, int remotePort)
	    throws InterruptedException, ExecutionException, SocketException {

	/*
	 * Instansierar ett nytt objekt utav tj�nst-klassen.
	 */
	final Service service = new Service(localPort, remoteHost, remotePort);

	/*
	 * Instansierar ett nytt objekt utav den grafiska
	 * anv�ndergr�nssnitts-klassen.
	 */
	final GUI gui = new GUI();

	/*
	 * Instansierar ett nytt objekt utav den mellanliggande
	 * f�rmedlar-klassen. Registrerar sedan ovanst�ende tv� komponenter.
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
			.println("Anv�ndning:\nWhiteboard [LOKAL PORT] [DEST. ADRESS] [DEST. PORT]");
	} catch (UnknownHostException e) {
	    /*
	     * Ifall angiven n�tverksadress inte g�r att upps�ka.
	     */
	    System.out.println("Angiven n�tverksadress g�r ej att upps�ka!");
	} catch (IllegalArgumentException e) {
	    /*
	     * Ifall angiven n�tverksport inte g�r att �vers�tta till siffror
	     * eller inte �r giltig.
	     */
	    System.out
		    .println("N�tverksporten ska best� utav enbart siffror inom intervallet 1-65535!");
	} catch (InterruptedException e) {
	    System.out
		    .println("Uppbygget utav det grafiska anv�ndargr�nssnittet avbr�ts:");
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    System.out
		    .println("Uppbygget utav det grafiska anv�ndargr�nssnittet kunde inte slutf�ras:");
	    e.printStackTrace();
	} catch (SocketException e) {
	    // TODO
	    e.printStackTrace();
	}
    }
}
