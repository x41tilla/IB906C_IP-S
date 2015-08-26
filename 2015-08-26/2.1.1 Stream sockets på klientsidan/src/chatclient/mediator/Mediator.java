package chatclient.mediator;

import java.util.Timer;
import java.util.TimerTask;

import chatclient.gui.GUI;
import chatclient.service.Service;

/**
 * Klass som agerar förmedlare mellan dem två komponenter som klienten
 * huvudsakligen består utav; självaste tjänsten samt det grafiska gränssnittet.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class Mediator {
    private GUI gui;
    private Service service;

    /**
     * Skapar ett objekt utav klassen {@link Mediator} och instansierar
     * ihopbundnings-försökandet som har i syfte att synkronisera det grafiska
     * gränssnittet med den underliggande tjänsten som driver klienten.
     */
    public Mediator() {
	initializeBundling();
    }

    /**
     * Metod som är till för att låta ett objekt utav klassen {@link Service}
     * registera sig hos förmedlaren.
     * 
     * @param service
     *            det objekt som skall låta sig registreras som serverns
     *            bakomliggande tjänst
     */
    public void registerService(Service service) {
	
	this.service = service;
	this.service.setDelegate(new Service.Delegate() {

	    /*
	     * Om ett meddelande mottas från servern så skrivs det ut i det grafiska användargränssnittet.
	     */
	    @Override
	    public void onMessageReceived(String message) {
		Mediator.this.gui.printMessage(message);
	    }

	    /*
	     * Om anslutningen råkar ut för ett avbrott så stoppas tjänsten, interaktion med det grafiska användargränssnittet avaktiveras och användaren meddelas.
	     */
	    @Override
	    public void onConnectionError() {
		Mediator.this.service.stopServing();
		Mediator.this.gui.enableInput(false);
		Mediator.this.gui.printMessage("--- Anslutningen bröts! ---");
	    }
	});
    }

    /**
     * Metod som är till för att låta ett objekt utav klassen {@link GUI}
     * registera sig hos förmedlaren.
     * 
     * @param gui
     *            det objekt som skall låts sig registreras som klientens
     *            grafiska gränssnitt
     */
    public void registerGUI(GUI gui) {
	this.gui = gui;
	
	/*
	 * Möjligheten till interaktion med det grafiska användargränssnittet avaktiveras tills vidare.
	 */
	this.gui.enableInput(false);
	
	/*
	 * Ett delegat-objekt som definierar den eller dem metoder som skall delegeras, registreras.
	 */
	this.gui.registerDelegate(new GUI.Delegate() {

	    /*
	     * Inmatningsfältet i det grafiska användargränssnittet skickar ett meddelande till servern.
	     */
	    @Override
	    public void onMessageSending(String message) {
		Mediator.this.service.sendMessage(message);
	    }
	});
    }

    /**
     * Hjälpmetod som uppdaterar fönstrets titel i det grafiska
     * användargränssnittet.
     */
    private final void updateGuiWindowTitle() {
	this.gui.setTitle(this.service.toString());
    }

    /**
     * Hjälpmetod för att vid sammankoppling av det grafiska
     * användargränssnittet med den underliggande klient-tjänsten kunna se till
     * att bägge mjukvaro-komponenter "väntar" på att respektive part skall vara
     * redo innan användaren tillåts interagera med det grafiska gränssnittet,
     * då detta annars kommer leda till avbrott av typen
     * {@link NullPointerException}.
     * 
     * Metoden startar en timer som med en intervall på 500 millisekunder
     * kontrollerar ifall den underliggante klient-tjänsten är redo. Om den är
     * det så tillåts interaktion med det grafiska användargränssnittet och
     * klienten är fullt redo.
     */
    private final void initializeBundling() {

	/*
	 * En timer startas. Den tråd som exekverar timer:n namnges.
	 */
	new Timer("Mediator").scheduleAtFixedRate(new TimerTask() {

	    @Override
	    public void run() {

		/*
		 * En första kontroll utförs för att utvärdera ifall bägge sidor
		 * utav klienten - det vill säga både det grafiska gränssnittet
		 * och den underliggande klient-tjänsten - har instansierats.
		 */
		if (Mediator.this.gui != null && Mediator.this.service != null) {

		    /*
		     * Om klient-tjänsten inte redan är startad så startas
		     * denna.
		     */
		    if (!Mediator.this.service.isServing())
			Mediator.this.service.startServing();

		    /*
		     * Om ovanstående villkor uppfylls så utförs en andra
		     * kontroll för att se om det grafiska användargränssnittet
		     * samt den underliggande klient-tjänsten är redo.
		     */
		    if (Mediator.this.gui.isReady()
			    && Mediator.this.service.isServing()) {

			/*
			 * Uppfylls även ovanstående villkor så uppdateras
			 * fönstrets titel i det grafiska användargränssnittet
			 * och interaktion tillåts.
			 */
			updateGuiWindowTitle();
			Mediator.this.gui.enableInput(true);

			/*
			 * Timer:n stoppas då den inte längre behövs.
			 */
			cancel();
		    }
		}
	    }
	}, 0, 500);
    }
}
