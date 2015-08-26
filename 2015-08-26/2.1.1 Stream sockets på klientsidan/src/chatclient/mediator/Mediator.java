package chatclient.mediator;

import java.util.Timer;
import java.util.TimerTask;

import chatclient.gui.GUI;
import chatclient.service.Service;

/**
 * Klass som agerar f�rmedlare mellan dem tv� komponenter som klienten
 * huvudsakligen best�r utav; sj�lvaste tj�nsten samt det grafiska gr�nssnittet.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class Mediator {
    private GUI gui;
    private Service service;

    /**
     * Skapar ett objekt utav klassen {@link Mediator} och instansierar
     * ihopbundnings-f�rs�kandet som har i syfte att synkronisera det grafiska
     * gr�nssnittet med den underliggande tj�nsten som driver klienten.
     */
    public Mediator() {
	initializeBundling();
    }

    /**
     * Metod som �r till f�r att l�ta ett objekt utav klassen {@link Service}
     * registera sig hos f�rmedlaren.
     * 
     * @param service
     *            det objekt som skall l�ta sig registreras som serverns
     *            bakomliggande tj�nst
     */
    public void registerService(Service service) {
	
	this.service = service;
	this.service.setDelegate(new Service.Delegate() {

	    /*
	     * Om ett meddelande mottas fr�n servern s� skrivs det ut i det grafiska anv�ndargr�nssnittet.
	     */
	    @Override
	    public void onMessageReceived(String message) {
		Mediator.this.gui.printMessage(message);
	    }

	    /*
	     * Om anslutningen r�kar ut f�r ett avbrott s� stoppas tj�nsten, interaktion med det grafiska anv�ndargr�nssnittet avaktiveras och anv�ndaren meddelas.
	     */
	    @Override
	    public void onConnectionError() {
		Mediator.this.service.stopServing();
		Mediator.this.gui.enableInput(false);
		Mediator.this.gui.printMessage("--- Anslutningen br�ts! ---");
	    }
	});
    }

    /**
     * Metod som �r till f�r att l�ta ett objekt utav klassen {@link GUI}
     * registera sig hos f�rmedlaren.
     * 
     * @param gui
     *            det objekt som skall l�ts sig registreras som klientens
     *            grafiska gr�nssnitt
     */
    public void registerGUI(GUI gui) {
	this.gui = gui;
	
	/*
	 * M�jligheten till interaktion med det grafiska anv�ndargr�nssnittet avaktiveras tills vidare.
	 */
	this.gui.enableInput(false);
	
	/*
	 * Ett delegat-objekt som definierar den eller dem metoder som skall delegeras, registreras.
	 */
	this.gui.registerDelegate(new GUI.Delegate() {

	    /*
	     * Inmatningsf�ltet i det grafiska anv�ndargr�nssnittet skickar ett meddelande till servern.
	     */
	    @Override
	    public void onMessageSending(String message) {
		Mediator.this.service.sendMessage(message);
	    }
	});
    }

    /**
     * Hj�lpmetod som uppdaterar f�nstrets titel i det grafiska
     * anv�ndargr�nssnittet.
     */
    private final void updateGuiWindowTitle() {
	this.gui.setTitle(this.service.toString());
    }

    /**
     * Hj�lpmetod f�r att vid sammankoppling av det grafiska
     * anv�ndargr�nssnittet med den underliggande klient-tj�nsten kunna se till
     * att b�gge mjukvaro-komponenter "v�ntar" p� att respektive part skall vara
     * redo innan anv�ndaren till�ts interagera med det grafiska gr�nssnittet,
     * d� detta annars kommer leda till avbrott av typen
     * {@link NullPointerException}.
     * 
     * Metoden startar en timer som med en intervall p� 500 millisekunder
     * kontrollerar ifall den underliggante klient-tj�nsten �r redo. Om den �r
     * det s� till�ts interaktion med det grafiska anv�ndargr�nssnittet och
     * klienten �r fullt redo.
     */
    private final void initializeBundling() {

	/*
	 * En timer startas. Den tr�d som exekverar timer:n namnges.
	 */
	new Timer("Mediator").scheduleAtFixedRate(new TimerTask() {

	    @Override
	    public void run() {

		/*
		 * En f�rsta kontroll utf�rs f�r att utv�rdera ifall b�gge sidor
		 * utav klienten - det vill s�ga b�de det grafiska gr�nssnittet
		 * och den underliggande klient-tj�nsten - har instansierats.
		 */
		if (Mediator.this.gui != null && Mediator.this.service != null) {

		    /*
		     * Om klient-tj�nsten inte redan �r startad s� startas
		     * denna.
		     */
		    if (!Mediator.this.service.isServing())
			Mediator.this.service.startServing();

		    /*
		     * Om ovanst�ende villkor uppfylls s� utf�rs en andra
		     * kontroll f�r att se om det grafiska anv�ndargr�nssnittet
		     * samt den underliggande klient-tj�nsten �r redo.
		     */
		    if (Mediator.this.gui.isReady()
			    && Mediator.this.service.isServing()) {

			/*
			 * Uppfylls �ven ovanst�ende villkor s� uppdateras
			 * f�nstrets titel i det grafiska anv�ndargr�nssnittet
			 * och interaktion till�ts.
			 */
			updateGuiWindowTitle();
			Mediator.this.gui.enableInput(true);

			/*
			 * Timer:n stoppas d� den inte l�ngre beh�vs.
			 */
			cancel();
		    }
		}
	    }
	}, 0, 500);
    }
}
