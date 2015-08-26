package chatclient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import chatclient.gui.GUI;
import chatclient.mediator.Mediator;
import chatclient.service.Service;

/**
 * Till�mpningsprogram f�r en chat-klient som g�r att starta p� 3 olika s�tt: A)
 * Inga parametrar - f�rs�ker ansluta till localhost p� port 2000, B) En
 * parametrar - n�tverksadress skickas som parametrar, f�rs�ker ansluta till
 * denna p� port 2000, samt C) Tv� parametrar - n�tverksadress och port skickas
 * som parametrar som programmet f�rs�ker ansluta till.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Client {

    /**
     * N�tverksadressen {@value #DEFAULT_HOST} som klienten kommer f�rs�ka
     * anslutna mot om inget annat best�ms.
     */
    public final static InetAddress DEFAULT_HOST = InetAddress
	    .getLoopbackAddress();
    /**
     * N�tverksporten {@value #DEFAULT_PORT} som klienten kommer f�rs�ka
     * anslutna mot om inget annat best�ms.
     */
    public final static int DEFAULT_PORT = 2000;

    /**
     * Instansierar ett nytt objekt utav klassen {@link Client}.
     *
     * @param host
     *            n�tverksadressen som klienten kommer f�rs�ka anslutna mot
     * @param port
     *            n�tverksporten {@value #DEFAULT_PORT} som klienten kommer
     *            f�rs�ka anslutna mot
     * @throws IOException
     *             vid problem orsakat n�r en socket instanserats f�r att lyssna
     *             p� best�md server
     * @throws InterruptedException
     *             om bygget utav det grafiska anv�ndargr�nssnittet avbr�ts
     * @throws ExecutionException
     *             om bygget utav det grafiska anv�ndargr�nssnittet inte kunde
     *             slutf�ras
     */
    public Client(InetAddress host, int port) throws IOException,
	    InterruptedException, ExecutionException {

	/*
	 * Instansierar ett nytt objekt utav tj�nst-klassen.
	 */
	final Service service = new Service(host, port);

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
     * Huvudmetoden som anropas n�r applikationen k�rs.
     * 
     * @param args
     *            n�tverksadressen och n�tverksporten g�r att specifiera genom
     *            att skicka med denna som argument. I annat fall anv�nds
     *            n�tverksadressen {@link #DEFAULT_HOST} och n�tverksporten
     *            {@link #DEFAULT_PORT}.
     */
    public static void main(String[] args) {
	final InetAddress host;
	final int port;

	try {
	    switch (args.length) {

	    case 0:
		new Client(DEFAULT_HOST, DEFAULT_PORT);
		break;

	    case 1:
		host = InetAddress.getByName(args[0]);
		new Client(host, DEFAULT_PORT);
		break;

	    case 2:
		host = InetAddress.getByName(args[0]);
		port = Integer.parseInt(args[1]);
		new Client(host, port);
		break;

	    default:
		System.out.println("Anv�ndning:\nClient [ADRESS] [PORT]");
		break;
	    }
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
	} catch (ConnectException e) {
	    /*
	     * Ifall anslutningsf�rs�ket mot servern inte besvaras eller avisas.
	     */
	    System.out
		    .println("Gick inte att n� servern - anslutningen avisades!");
	} catch (IOException e) {
	    /*
	     * Ifall kritiskt dock inte specifierat fel intr�ffat vind
	     * initialisering utav anslutningen.
	     */
	    System.out
		    .println("Ett allvarligt fel har intr�ffat vid initialiseringen utav klientens anslutning:");
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    /*
	     * Bygget utav det grafiska anv�ndargr�nssnittet avbr�ts.
	     */
	    System.out
		    .println("Uppbygget utav det grafiska anv�ndargr�nssnittet avbr�ts:");
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    /*
	     * Bygget utav det grafiska anv�ndargr�nssnittet inte kunde
	     * slutf�ras.
	     */
	    System.out
		    .println("Uppbygget utav det grafiska anv�ndargr�nssnittet kunde inte slutf�ras:");
	    e.printStackTrace();
	}
    }
}
