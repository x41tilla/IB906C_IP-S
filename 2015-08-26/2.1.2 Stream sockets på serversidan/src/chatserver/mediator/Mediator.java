package chatserver.mediator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import chatserver.gui.GUI;
import chatserver.service.Service;

/**
 * Klass som agerar förmedlare mellan dem två komponenter som servern
 * huvudsakligen består utav; självaste tjänsten samt det grafiska gränssnittet.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class Mediator {
    private boolean bundlingInitiated = false;
    private GUI gui;
    private Service service;

    /**
     * Skapar ett objekt utav klassen {@link Mediator} och instansierar
     * ihopbundnings-försökandet som har i syfte att synkronisera det grafiska
     * användargränssnittet med den underliggande tjänsten som driver servern.
     */
    public Mediator() {
    }

    /**
     * Metod som är till för att låta ett objekt utav klassen {@link GUI}
     * registera sig hos förmedlaren.
     * 
     * @param gui
     *            det objekt som skall låts sig registreras som serverns
     *            grafiska användargränssnitt
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void registerGUI(GUI gui) throws InterruptedException,
	    ExecutionException {
	this.gui = gui;

	/*
	 * Möjligheten till interaktion med det grafiska användargränssnittet
	 * avaktiveras tills vidare.
	 */
	this.gui.enableInput(false);

	/*
	 * Ett delegat-objekt som definierar den eller dem metoder som skall
	 * delegeras, registreras.
	 */
	this.gui.registerDelegate(new GUI.Delegate() {

	    /*
	     * Inmatningsfältet i det grafiska användargränssnittet
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
     * Metod som är till för att låta ett objekt utav klassen {@link Service}
     * registera sig hos förmedlaren.
     * 
     * @param service
     *            det objekt som skall låta sig registreras som serverns
     *            bakomliggande tjänst
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void registerService(Service service) throws InterruptedException,
	    ExecutionException {

	this.service = service;
	this.service.addServiceListener(new Service.ServiceListener() {

	    /*
	     * Om termineringen utav en befintlig klient blir problematisk så
	     * meddelas användaren - lite av ett 'easter-egg'.
	     */
	    @Override
	    public void onClientRemovalError(String author) {
		System.out.println("Klienten " + author
			+ " var jobbig att bli av med... Men det löste sig!");
	    }

	    /*
	     * Vid ett nytt men misslyckat anslutningsförsök från en klient så
	     * meddelas användaren om detta, samt så uppdateras titeln hos det
	     * grafiska användargränssnittet.
	     */
	    @Override
	    public void onConnectionInitializationError(String errorMessage) {
		System.out
			.println("En anslutning som försökte initialiseras med en klient, misslyckades:");
		System.out.println(errorMessage);
		System.out.println("----\tServern forstätter att köras!\t----");

		updateGuiWindowTitle();
	    }

	    /*
	     * Vid förloran utav kontakten med en klient så uppdateras titeln
	     * hos det grafiska användargränssnittet.
	     */
	    @Override
	    public void onConnectionLost() {
		updateGuiWindowTitle();
	    }

	    /*
	     * Om ett meddelande mottas från en klient så skrivs det ut i det
	     * grafiska användargränssnittet, samt så vidarebefodras det till
	     * samtliga ansluta klienter.
	     */
	    @Override
	    public void onMessageRecieved(String author, String message) {
		Mediator.this.gui.printMessage(author + ": " + message);
		Mediator.this.service.broadcastMessage(author + ": " + message);
	    }

	    /*
	     * Vid en ny lyckas anslutning från en klient så uppdateras titeln
	     * hos det grafiska användargränssnittet.
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
     * Hjälpmetod för att vid sammankoppling av det grafiska
     * användargränssnittet med den underliggande server-tjänsten kunna se till
     * att bägge mjukvaro-komponenter "väntar" på att respektive part skall vara
     * redo innan användaren tillåts interagera med det grafiska
     * användargränssnittet, då detta annars kommer leda till avbrott av typen
     * {@link NullPointerException}.
     *
     * Metoden startar en timer som med en intervall på 500 millisekunder
     * kontrollerar ifall den underliggante server-tjänsten är redo. Om den är
     * det så tillåts interaktion med det grafiska gränssnittet och servern är
     * då fullt redo.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private final void initializeBundling() throws InterruptedException,
	    ExecutionException {

	if (this.bundlingInitiated)
	    return;

	/*
	 * En timer startas. Den tråd som exekverar timer:n namnges.
	 */

	Callable<Boolean> timedTask = new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws InterruptedException {

		/*
		 * En första kontroll utförs för att utvärdera ifall bägge sidor
		 * utav servern - det vill säga både det grafiska gränssnittet
		 * och den underliggande server-tjänsten - har instansierats.
		 */
		/*
		 * TODO Om ovanstående villkor uppfylls så utförs en andra
		 * kontroll för att se om det grafiska användargränssnittet samt
		 * den underliggande server-tjänsten är redo.
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
     * Hjälpmetod som uppdaterar fönstrets titel i det grafiska
     * användargränssnittet.
     */
    private final void updateGuiWindowTitle() {
	this.gui.setTitle(this.service.toString());
    }
}
