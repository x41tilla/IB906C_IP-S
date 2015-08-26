package chatserver;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.ExecutionException;

import chatserver.gui.GUI;
import chatserver.mediator.Mediator;
import chatserver.service.Service;

/**
 * Tillämpningsprogram som knyter samman alla delaktiga komponenter till en
 * fungerande chat-server. Startas genom att antingen anropas utan argument,
 * vilket då startar en chat-server som lyssnar på port {@code 2000}, eller
 * också kan tillämpningsprogrammet startas med ett argument som då utgör en
 * specifierad port chat-servern skall lyssna på.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Server {
    /**
     * Nätverksporten {@value #DEFAULT_PORT} som servern kommer lyssna på om
     * inget annat bestäms.
     */
    public final static int DEFAULT_PORT = 2000;

    /**
     * Instansierar ett nytt objekt utav klassen {@link Server}. Nätverksporten
     * som används specifieras utav användaren.
     * 
     * @param port
     *            den nätverksport som skall användas av servern
     *
     * @throws BindException
     *             om nätverksporten inte går att lyssna på
     * @throws IllegalArgumentException
     *             om angiven nätverksport inte är en giltig sådan
     * @throws IOException
     *             vid problem orsakat när en socket initieras för att lyssna på
     *             bestämd port
     * @throws InterruptedException
     *             om bygget utav det grafiska användargränssnittet avbröts
     * @throws ExecutionException
     *             om bygget utav det grafiska användargränssnittet inte kunde
     *             slutföras
     */
    public Server(int port) throws BindException, IllegalArgumentException,
	    IOException, InterruptedException, ExecutionException {

	/*
	 * Instansierar ett nytt objekt utav tjänst-klassen.
	 */
	final Service service = new Service(port);

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
     *            nätverksporten går att specifiera genom att skicka med denna
     *            som ett argument. I annat fall används nätverksporten
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
		System.out.println("Användning:\nServer [PORT]");
		break;
	    }
	} catch (IllegalArgumentException e) {
	    /*
	     * Ifall specifierad port inte går att översätta till siffror eller
	     * inte är en giltig port.
	     */
	    System.out
		    .println("Port-numret ska bestå utav enbart siffror inom intervallet 1-65535!");
	} catch (BindException e) {
	    /*
	     * Ifall fel inträffar vid socket'ens bindande till porten
	     */
	    System.out
		    .println("Kunde inte använda nätverksport - kan vara på grund utav att porten är upptagen av annan tjänst!");
	} catch (IOException e) {
	    /*
	     * Ifall kritiskt dock inte specifierat fel inträffat vind
	     * initialisering utav socket'en
	     */
	    System.out
		    .println("Ett allvarligt fel har inträffat vid initialiseringen utav servern:");
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    System.out
		    .println("Uppbygget utav det grafiska användargränssnittet avbröts:");
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    System.out
		    .println("Uppbygget utav det grafiska användargränssnittet kunde inte slutföras:");
	    e.printStackTrace();
	}
    }
}
