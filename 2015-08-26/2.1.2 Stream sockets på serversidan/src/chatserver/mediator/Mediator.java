package chatserver.mediator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import chatserver.gui.GUI;
import chatserver.service.Service;

/**
 * Klass som agerar f�rmedlare mellan dem tv� komponenter som servern
 * huvudsakligen best�r utav; sj�lvaste tj�nsten samt det grafiska gr�nssnittet.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class Mediator {
    private boolean bundlingInitiated = false;
    private GUI gui;
    private Service service;

    /**
     * Skapar ett objekt utav klassen {@link Mediator} och instansierar
     * ihopbundnings-f�rs�kandet som har i syfte att synkronisera det grafiska
     * anv�ndargr�nssnittet med den underliggande tj�nsten som driver servern.
     */
    public Mediator() {
    }

    /**
     * Metod som �r till f�r att l�ta ett objekt utav klassen {@link GUI}
     * registera sig hos f�rmedlaren.
     * 
     * @param gui
     *            det objekt som skall l�ts sig registreras som serverns
     *            grafiska anv�ndargr�nssnitt
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void registerGUI(GUI gui) throws InterruptedException,
	    ExecutionException {
	this.gui = gui;

	/*
	 * M�jligheten till interaktion med det grafiska anv�ndargr�nssnittet
	 * avaktiveras tills vidare.
	 */
	this.gui.enableInput(false);

	/*
	 * Ett delegat-objekt som definierar den eller dem metoder som skall
	 * delegeras, registreras.
	 */
	this.gui.registerDelegate(new GUI.Delegate() {

	    /*
	     * Inmatningsf�ltet i det grafiska anv�ndargr�nssnittet
	     * vidarebefodrar meddelanden till samtliga ansluta klienter.
	     */
	    @Override
	    public void onMessageSending(String message) {
		Mediator.this.service.broadcastMessage(message);
		Mediator.this.gui.printMessage(message);
	    }
	});

	if (this.service != null)
	    this.initializeBundling();
    }

    /**
     * Metod som �r till f�r att l�ta ett objekt utav klassen {@link Service}
     * registera sig hos f�rmedlaren.
     * 
     * @param service
     *            det objekt som skall l�ta sig registreras som serverns
     *            bakomliggande tj�nst
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void registerService(Service service) throws InterruptedException,
	    ExecutionException {

	this.service = service;
	this.service.addServiceListener(new Service.ServiceListener() {

	    /*
	     * Om termineringen utav en befintlig klient blir problematisk s�
	     * meddelas anv�ndaren - lite av ett 'easter-egg'.
	     */
	    @Override
	    public void onClientRemovalError(String author) {
		System.out.println("Klienten " + author
			+ " var jobbig att bli av med... Men det l�ste sig!");
	    }

	    /*
	     * Vid ett nytt men misslyckat anslutningsf�rs�k fr�n en klient s�
	     * meddelas anv�ndaren om detta, samt s� uppdateras titeln hos det
	     * grafiska anv�ndargr�nssnittet.
	     */
	    @Override
	    public void onConnectionInitializationError(String errorMessage) {
		System.out
			.println("En anslutning som f�rs�kte initialiseras med en klient, misslyckades:");
		System.out.println(errorMessage);
		System.out.println("----\tServern forst�tter att k�ras!\t----");

		updateGuiWindowTitle();
	    }

	    /*
	     * Vid f�rloran utav kontakten med en klient s� uppdateras titeln
	     * hos det grafiska anv�ndargr�nssnittet.
	     */
	    @Override
	    public void onConnectionLost() {
		updateGuiWindowTitle();
	    }

	    /*
	     * Om ett meddelande mottas fr�n en klient s� skrivs det ut i det
	     * grafiska anv�ndargr�nssnittet, samt s� vidarebefodras det till
	     * samtliga ansluta klienter.
	     */
	    @Override
	    public void onMessageRecieved(String author, String message) {
		Mediator.this.gui.printMessage(author + ": " + message);
		Mediator.this.service.broadcastMessage(author + ": " + message);
	    }

	    /*
	     * Vid en ny lyckas anslutning fr�n en klient s� uppdateras titeln
	     * hos det grafiska anv�ndargr�nssnittet.
	     */
	    @Override
	    public void onNewConnection() {
		updateGuiWindowTitle();
	    }
	});

	if (this.gui != null)
	    this.initializeBundling();
    }

    /**
     * Hj�lpmetod f�r att vid sammankoppling av det grafiska
     * anv�ndargr�nssnittet med den underliggande server-tj�nsten kunna se till
     * att b�gge mjukvaro-komponenter "v�ntar" p� att respektive part skall vara
     * redo innan anv�ndaren till�ts interagera med det grafiska
     * anv�ndargr�nssnittet, d� detta annars kommer leda till avbrott av typen
     * {@link NullPointerException}.
     *
     * Metoden startar en timer som med en intervall p� 500 millisekunder
     * kontrollerar ifall den underliggante server-tj�nsten �r redo. Om den �r
     * det s� till�ts interaktion med det grafiska gr�nssnittet och servern �r
     * d� fullt redo.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private final void initializeBundling() throws InterruptedException,
	    ExecutionException {

	if (this.bundlingInitiated)
	    return;

	/*
	 * En timer startas. Den tr�d som exekverar timer:n namnges.
	 */

	Callable<Boolean> timedTask = new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws InterruptedException {

		/*
		 * En f�rsta kontroll utf�rs f�r att utv�rdera ifall b�gge sidor
		 * utav servern - det vill s�ga b�de det grafiska gr�nssnittet
		 * och den underliggande server-tj�nsten - har instansierats.
		 */
		/*
		 * TODO Om ovanst�ende villkor uppfylls s� utf�rs en andra
		 * kontroll f�r att se om det grafiska anv�ndargr�nssnittet samt
		 * den underliggande server-tj�nsten �r redo.
		 */

		do {
		    Thread.sleep(500);
		    if (Mediator.this.gui.isReady())
			return true;

		} while (true);
	    }
	};

	Future<Boolean> isReadyForBundle = Executors.newSingleThreadExecutor()
		.submit(timedTask);
	isReadyForBundle.get();

	updateGuiWindowTitle();
	Mediator.this.gui.enableInput(true);
	Mediator.this.service.startServing();
    }

    /**
     * Hj�lpmetod som uppdaterar f�nstrets titel i det grafiska
     * anv�ndargr�nssnittet.
     */
    private final void updateGuiWindowTitle() {
	this.gui.setTitle(this.service.toString());
    }
}
