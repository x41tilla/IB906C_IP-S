package chatserver;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.ExecutionException;

import chatserver.gui.GUI;
import chatserver.mediator.Mediator;
import chatserver.service.Service;

/**
 * Till�mpningsprogram som knyter samman alla delaktiga komponenter till en
 * fungerande chat-server. Startas genom att antingen anropas utan argument,
 * vilket d� startar en chat-server som lyssnar p� port {@code 2000}, eller
 * ocks� kan till�mpningsprogrammet startas med ett argument som d� utg�r en
 * specifierad port chat-servern skall lyssna p�.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Server {
    /**
     * N�tverksporten {@value #DEFAULT_PORT} som servern kommer lyssna p� om
     * inget annat best�ms.
     */
    public final static int DEFAULT_PORT = 2000;

    /**
     * Instansierar ett nytt objekt utav klassen {@link Server}. N�tverksporten
     * som anv�nds specifieras utav anv�ndaren.
     * 
     * @param port
     *            den n�tverksport som skall anv�ndas av servern
     *
     * @throws BindException
     *             om n�tverksporten inte g�r att lyssna p�
     * @throws IllegalArgumentException
     *             om angiven n�tverksport inte �r en giltig s�dan
     * @throws IOException
     *             vid problem orsakat n�r en socket initieras f�r att lyssna p�
     *             best�md port
     * @throws InterruptedException
     *             om bygget utav det grafiska anv�ndargr�nssnittet avbr�ts
     * @throws ExecutionException
     *             om bygget utav det grafiska anv�ndargr�nssnittet inte kunde
     *             slutf�ras
     */
    public Server(int port) throws BindException, IllegalArgumentException,
	    IOException, InterruptedException, ExecutionException {

	/*
	 * Instansierar ett nytt objekt utav tj�nst-klassen.
	 */
	final Service service = new Service(port);

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
     *            n�tverksporten g�r att specifiera genom att skicka med denna
     *            som ett argument. I annat fall anv�nds n�tverksporten
     *            {@link #DEFAULT_PORT}.
     */
    public static void main(String[] args) {
	final int port;

	try {
	    switch (args.length) {

	    case 0:
		new Server(DEFAULT_PORT);
		break;

	    case 1:
		port = Integer.parseInt(args[0]);
		new Server(port);
		break;
	    default:
		System.out.println("Anv�ndning:\nServer [PORT]");
		break;
	    }
	} catch (IllegalArgumentException e) {
	    /*
	     * Ifall specifierad port inte g�r att �vers�tta till siffror eller
	     * inte �r en giltig port.
	     */
	    System.out
		    .println("Port-numret ska best� utav enbart siffror inom intervallet 1-65535!");
	} catch (BindException e) {
	    /*
	     * Ifall fel intr�ffar vid socket'ens bindande till porten
	     */
	    System.out
		    .println("Kunde inte anv�nda n�tverksport - kan vara p� grund utav att porten �r upptagen av annan tj�nst!");
	} catch (IOException e) {
	    /*
	     * Ifall kritiskt dock inte specifierat fel intr�ffat vind
	     * initialisering utav socket'en
	     */
	    System.out
		    .println("Ett allvarligt fel har intr�ffat vid initialiseringen utav servern:");
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    System.out
		    .println("Uppbygget utav det grafiska anv�ndargr�nssnittet avbr�ts:");
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    System.out
		    .println("Uppbygget utav det grafiska anv�ndargr�nssnittet kunde inte slutf�ras:");
	    e.printStackTrace();
	}
    }
}
