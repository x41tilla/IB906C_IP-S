package whiteboard.mediator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import whiteboard.gui.GUI;
import whiteboard.service.Service;

/**
 * Klass som agerar förmedlare mellan dem två komponenter som applikationen
 * huvudsakligen består utav; självaste tjänsten samt det grafiska gränssnittet.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Mediator {
    private Service service;
    private GUI gui;

    /**
     * Skapar ett objekt utav klassen {@link Mediator} och instansierar
     * ihopbundnings-försökandet som har i syfte att synkronisera det grafiska
     * användargränssnittet med den underliggande tjänsten som driver
     * applikationen.
     */
    public Mediator() {
	initializeBundling();
    }

    /**
     * Metod som är till för att låta ett objekt utav klassen {@link Service}
     * registera sig hos förmedlaren.
     * 
     * @param service
     *            det objekt som skall låta sig registreras som applikationens
     *            bakomliggande tjänst
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
     * Metod som är till för att låta ett objekt utav klassen {@link GUI}
     * registera sig hos förmedlaren.
     * 
     * @param gui
     *            det objekt som skall låts sig registreras som applikationens
     *            grafiska användargränssnitt
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
     * Hjälpmetod som uppdaterar fönstrets titel i det grafiska
     * användargränssnittet.
     */
    private final void updateGuiWindowTitle() {
	this.gui.setTitle(this.service.toString());
    }

    /**
     * Hjälpmetod för att vid sammankoppling av det grafiska
     * användargränssnittet med den underliggande tjänsten kunna se till att
     * bägge mjukvaro-komponenter "väntar" på att respektive part skall vara
     * redo innan användaren tillåts interagera med det grafiska
     * användargränssnittet, då detta annars kommer leda till avbrott av typen
     * {@link NullPointerException}.
     * 
     * Metoden startar en timer som med en intervall på 500 millisekunder
     * kontrollerar ifall den underliggante tjänsten är redo. Om den är det så
     * tillåts interaktion med det grafiska användargränssnittet och
     * applikationen är då fullt redo.
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
		 * utav servern - det vill säga både det grafiska gränssnittet
		 * och den underliggande server-tjänsten - har instansierats.
		 */
		if (Mediator.this.gui != null && Mediator.this.service != null) {

		    /*
		     * Om ovanstående villkor uppfylls så utförs en andra
		     * kontroll för att se om det grafiska användargränssnittet
		     * samt den underliggande server-tjänsten är redo.
		     */
		    if (Mediator.this.gui.isReady()
			    && Mediator.this.service.isServing()) {

			/*
			 * Uppfylls även ovanstående villkor så uppdateras
			 * fönstrets titel i det grafiska användargränssnittet
			 * och interaktion tillåts.
			 */
			updateGuiWindowTitle();
			Mediator.this.gui.setEnabled(true);

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
