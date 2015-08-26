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
 * Tillämpningsprogram för en chat-klient som går att starta på 3 olika sätt: A)
 * Inga parametrar - försöker ansluta till localhost på port 2000, B) En
 * parametrar - nätverksadress skickas som parametrar, försöker ansluta till
 * denna på port 2000, samt C) Två parametrar - nätverksadress och port skickas
 * som parametrar som programmet försöker ansluta till.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Client {

    /**
     * Nätverksadressen {@value #DEFAULT_HOST} som klienten kommer försöka
     * anslutna mot om inget annat bestäms.
     */
    public final static InetAddress DEFAULT_HOST = InetAddress
	    .getLoopbackAddress();
    /**
     * Nätverksporten {@value #DEFAULT_PORT} som klienten kommer försöka
     * anslutna mot om inget annat bestäms.
     */
    public final static int DEFAULT_PORT = 2000;

    /**
     * Instansierar ett nytt objekt utav klassen {@link Client}.
     *
     * @param host
     *            nätverksadressen som klienten kommer försöka anslutna mot
     * @param port
     *            nätverksporten {@value #DEFAULT_PORT} som klienten kommer
     *            försöka anslutna mot
     * @throws IOException
     *             vid problem orsakat när en socket instanserats för att lyssna
     *             på bestämd server
     * @throws InterruptedException
     *             om bygget utav det grafiska användargränssnittet avbröts
     * @throws ExecutionException
     *             om bygget utav det grafiska användargränssnittet inte kunde
     *             slutföras
     */
    public Client(InetAddress host, int port) throws IOException,
	    InterruptedException, ExecutionException {

	/*
	 * Instansierar ett nytt objekt utav tjänst-klassen.
	 */
	final Service service = new Service(host, port);

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
     * Huvudmetoden som anropas när applikationen körs.
     * 
     * @param args
     *            nätverksadressen och nätverksporten går att specifiera genom
     *            att skicka med denna som argument. I annat fall används
     *            nätverksadressen {@link #DEFAULT_HOST} och nätverksporten
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
		System.out.println("Användning:\nClient [ADRESS] [PORT]");
		break;
	    }
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
	} catch (ConnectException e) {
	    /*
	     * Ifall anslutningsförsöket mot servern inte besvaras eller avisas.
	     */
	    System.out
		    .println("Gick inte att nå servern - anslutningen avisades!");
	} catch (IOException e) {
	    /*
	     * Ifall kritiskt dock inte specifierat fel inträffat vind
	     * initialisering utav anslutningen.
	     */
	    System.out
		    .println("Ett allvarligt fel har inträffat vid initialiseringen utav klientens anslutning:");
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    /*
	     * Bygget utav det grafiska användargränssnittet avbröts.
	     */
	    System.out
		    .println("Uppbygget utav det grafiska användargränssnittet avbröts:");
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    /*
	     * Bygget utav det grafiska användargränssnittet inte kunde
	     * slutföras.
	     */
	    System.out
		    .println("Uppbygget utav det grafiska användargränssnittet kunde inte slutföras:");
	    e.printStackTrace();
	}
    }
}
