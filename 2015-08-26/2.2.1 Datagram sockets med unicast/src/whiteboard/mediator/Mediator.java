package whiteboard.mediator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import whiteboard.gui.GUI;
import whiteboard.service.Service;

/**
 * Klass som agerar f�rmedlare mellan dem tv� komponenter som applikationen
 * huvudsakligen best�r utav; sj�lvaste tj�nsten samt det grafiska gr�nssnittet.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Mediator {
    private Service service;
    private GUI gui;

    /**
     * Skapar ett objekt utav klassen {@link Mediator} och instansierar
     * ihopbundnings-f�rs�kandet som har i syfte att synkronisera det grafiska
     * anv�ndargr�nssnittet med den underliggande tj�nsten som driver
     * applikationen.
     */
    public Mediator() {
	initializeBundling();
    }

    /**
     * Metod som �r till f�r att l�ta ett objekt utav klassen {@link Service}
     * registera sig hos f�rmedlaren.
     * 
     * @param service
     *            det objekt som skall l�ta sig registreras som applikationens
     *            bakomliggande tj�nst
     */
    public void registerService(Service service) {

	this.service = service;

	this.service.setDelegate(new Service.Delegate() {

	    @Override
	    public void onDataReceived(byte[] data) {
		ByteBuffer decoder = ByteBuffer.wrap(data);
		Mediator.this.gui.addPoint(decoder.getShort(),
			decoder.getShort());
	    }

	    @Override
	    public void onIOException(IOException e) {
		// TODO
		System.out.println("HALLOJ - FEL!");
	    }
	});
    }

    /**
     * Metod som �r till f�r att l�ta ett objekt utav klassen {@link GUI}
     * registera sig hos f�rmedlaren.
     * 
     * @param gui
     *            det objekt som skall l�ts sig registreras som applikationens
     *            grafiska anv�ndargr�nssnitt
     */
    public void registerGUI(GUI gui) {

	this.gui = gui;
	this.gui.registerDelegate(new GUI.Delegate() {

	    @Override
	    public void onLocalEdit(short x, short y) {
		ByteBuffer decoder = ByteBuffer.allocate(4);
		decoder.putShort(x);
		decoder.putShort(y);
		byte[] data = decoder.array();
		Mediator.this.service.transmitData(data);
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
     * anv�ndargr�nssnittet med den underliggande tj�nsten kunna se till att
     * b�gge mjukvaro-komponenter "v�ntar" p� att respektive part skall vara
     * redo innan anv�ndaren till�ts interagera med det grafiska
     * anv�ndargr�nssnittet, d� detta annars kommer leda till avbrott av typen
     * {@link NullPointerException}.
     * 
     * Metoden startar en timer som med en intervall p� 500 millisekunder
     * kontrollerar ifall den underliggante tj�nsten �r redo. Om den �r det s�
     * till�ts interaktion med det grafiska anv�ndargr�nssnittet och
     * applikationen �r d� fullt redo.
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
		 * utav servern - det vill s�ga b�de det grafiska gr�nssnittet
		 * och den underliggande server-tj�nsten - har instansierats.
		 */
		if (Mediator.this.gui != null && Mediator.this.service != null) {

		    /*
		     * Om ovanst�ende villkor uppfylls s� utf�rs en andra
		     * kontroll f�r att se om det grafiska anv�ndargr�nssnittet
		     * samt den underliggande server-tj�nsten �r redo.
		     */
		    if (Mediator.this.gui.isReady()
			    && Mediator.this.service.isServing()) {

			/*
			 * Uppfylls �ven ovanst�ende villkor s� uppdateras
			 * f�nstrets titel i det grafiska anv�ndargr�nssnittet
			 * och interaktion till�ts.
			 */
			updateGuiWindowTitle();
			Mediator.this.gui.setEnabled(true);

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
