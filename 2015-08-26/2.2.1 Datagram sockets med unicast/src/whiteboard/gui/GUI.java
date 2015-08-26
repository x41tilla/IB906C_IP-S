package whiteboard.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Klass som ansvarar för det grafiska användargränssnittet mot applikationen i
 * form av ett fönster med ett ritbart "papper".
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class GUI {
    private Delegate delegate;
    private Paper paper;
    private final JFrame frame;

    /**
     * Skapar ett objekt utav det grafiska användargränssnittet. Använder sig
     * utav {@link EventQueue} under instansieringen.
     * 
     * @throws InterruptedException
     *             om bygget utav det grafiska användargränssnittet avbröts
     * @throws ExecutionException
     *             om bygget utav det grafiska användargränssnittet inte kunde
     *             slutföras
     */
    public GUI() throws InterruptedException, ExecutionException {

	/*
	 * Objektets delegerade "call-back"-metod(er) instanseras till att inte
	 * göra något vid någon händelse över huvud taget. Detta för att undvika
	 * att instansvariabeln istället pekar mot null.
	 */
	this.delegate = new Delegate() {
	    @Override
	    public void onLocalEdit(short x, short y) {
		/*
		 * Utebliven handling.
		 */
	    }
	};

	/*
	 * Instansierar det grafiska fönstret via ett anrop till en privat
	 * hjälpmetod som utför grovgörat för att sedan returnera ett
	 * färdigbyggt fönster.
	 */
	this.frame = initializeFrame(this);

	/*
	 * TODO
	 */
	this.setEnabled(false);
    }

    /**
     * Metod för att registrera ett delegat-objekt som implementerar
     * gränssnittet {@link Delegate}.
     *
     * @param delegate
     *            det delegat-objekt som skall registreras utav objektet
     */
    public void registerDelegate(Delegate delegate) {
	this.delegate = delegate;
    }

    /**
     * TODO
     *
     * @param x
     * @param y
     */
    public void addPoint(int x, int y) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		GUI.this.paper.addPoint(x, y);
	    }
	});
    }

    /**
     * Metod för att tillåta eller förneka att användaren kan interagera med
     * inmatningsfältet. Vid tillåtelse så förfrågar även inmatningsfältet om
     * att hamna i fokus. TODO
     * 
     * @param enable
     *            tillåtelse
     */
    public final void setEnabled(boolean enable) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		GUI.this.paper.setEnabled(enable);
	    }
	});
    }

    /**
     * Metod för att byta titeln hos fönstret i det grafiska
     * användargränssnittet.
     * 
     * @param newTitle
     *            den nya titeln
     */
    public final void setTitle(String newTitle) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		GUI.this.frame.setTitle("Whiteboard " + newTitle);
	    }
	});
    }

    /**
     * Metod för att kolla upp huruvida det grafiska användargränssnittet är
     * redo eller inte. Vad som menas med redo är ifall den är instansierad och
     * fullt uppbyggd.
     * 
     * @return {@code true} om redo, i annat fall {@code false}
     */
    public final boolean isReady() {
	return (this.frame != null);
    }

    /**
     * Metod som är till för att instansiera fönstret i det grafiska
     * användargränssnittet. Initialiseringen sker i en annan tråd för att inte
     * det skall leda till att servern upplevs som icke-responsiv.
     * 
     * @return ett instansierat och färdigbyggt objekt utav klassen
     *         {@link JFrame}
     * @throws InterruptedException
     *             om bygget utav det grafiska användargränssnittet avbröts
     * @throws ExecutionException
     *             om bygget utav det grafiska användargränssnittet inte kunde
     *             slutföras
     */
    private static final JFrame initializeFrame(GUI gui)
	    throws InterruptedException, ExecutionException {

	/*
	 * Definierar instruktionerna för arbetet som krävs för att sätta ihop
	 * fönstret...
	 */
	FutureTask<JFrame> frameBuildingTask = new FutureTask<>(
		new Callable<JFrame>() {

		    @Override
		    public JFrame call() {
			JFrame tmpFrame = new JFrame("Initializing service...");

			/*
			 * Bestämmer den layout-hanterare som fönstret skall ha.
			 */
			tmpFrame.setLayout(new BorderLayout());

			/*
			 * Instansierar objektets 'papper'.
			 */
			gui.paper = new Paper();

			/*
			 * Lägger till en lyssnare som registrerar mus-klick.
			 * Förutsatt att interaktion med det grafiska
			 * användargränssnittet är tillåtet så kommer mus-klick
			 * producera en ny, ritad punkt på 'pappret'. Dessutom
			 * kommer lämplig delegat-metod anropas.
			 */
			gui.paper.addMouseListener(new MouseAdapter() {
			    @Override
			    public void mousePressed(MouseEvent me) {
				if (gui.paper.isEnabled()) {
				    gui.paper.addPoint(me.getX(), me.getY());
				    gui.delegate.onLocalEdit((short) me.getX(),
					    (short) me.getY());
				}
			    }
			});

			/*
			 * Lägger till en lyssnare som registrerar mus-rörelser.
			 * Förutsatt att interaktion med det grafiska
			 * användargränssnittet är tillåtet så kommer mus-drag
			 * producera nya, ritade punkter på 'pappret'. Dessutom
			 * kommer lämplig delegat-metod anropas.
			 */
			gui.paper
				.addMouseMotionListener(new MouseMotionAdapter() {
				    @Override
				    public void mouseDragged(MouseEvent me) {
					if (gui.paper.isEnabled()) {
					    gui.paper.addPoint(me.getX(),
						    me.getY());
					    gui.delegate.onLocalEdit(
						    (short) me.getX(),
						    (short) me.getY());
					}
				    }
				});

			/*
			 * Placerar 'pappret' centralt i fönstret.
			 */
			tmpFrame.add(gui.paper, BorderLayout.CENTER);

			/*
			 * Anpassar egenskaper för fönstret.
			 */
			tmpFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			tmpFrame.pack();
			tmpFrame.setMinimumSize(tmpFrame.getPreferredSize());
			tmpFrame.setResizable(false);
			tmpFrame.setVisible(true);

			/*
			 * Returnerar det ihopsatta fönstret.
			 */
			return tmpFrame;
		    }
		});

	/*
	 * Utför arbetet...
	 */
	EventQueue.invokeLater(frameBuildingTask);

	/*
	 * Returnerar det nyligen ihopsatta fönstret. Väntar om det behövs.
	 */
	return frameBuildingTask.get();
    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som internt skall anropas av det ägande
     * objeket vid olika scenarion.
     * 
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate {
	/**
	 * Den metod som internt anropas när en förändring (ny ritning) sker
	 * lokalt.
	 *
	 * @param x
	 *            x-koordinaten av förändringen
	 * @param y
	 *            y-koordinaten av förändringen
	 */
	void onLocalEdit(short x, short y);
    }
}
