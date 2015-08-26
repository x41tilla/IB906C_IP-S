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
 * Klass som ansvarar f�r det grafiska anv�ndargr�nssnittet mot applikationen i
 * form av ett f�nster med ett ritbart "papper".
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class GUI {
    private Delegate delegate;
    private Paper paper;
    private final JFrame frame;

    /**
     * Skapar ett objekt utav det grafiska anv�ndargr�nssnittet. Anv�nder sig
     * utav {@link EventQueue} under instansieringen.
     * 
     * @throws InterruptedException
     *             om bygget utav det grafiska anv�ndargr�nssnittet avbr�ts
     * @throws ExecutionException
     *             om bygget utav det grafiska anv�ndargr�nssnittet inte kunde
     *             slutf�ras
     */
    public GUI() throws InterruptedException, ExecutionException {

	/*
	 * Objektets delegerade "call-back"-metod(er) instanseras till att inte
	 * g�ra n�got vid n�gon h�ndelse �ver huvud taget. Detta f�r att undvika
	 * att instansvariabeln ist�llet pekar mot null.
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
	 * Instansierar det grafiska f�nstret via ett anrop till en privat
	 * hj�lpmetod som utf�r grovg�rat f�r att sedan returnera ett
	 * f�rdigbyggt f�nster.
	 */
	this.frame = initializeFrame(this);

	/*
	 * TODO
	 */
	this.setEnabled(false);
    }

    /**
     * Metod f�r att registrera ett delegat-objekt som implementerar
     * gr�nssnittet {@link Delegate}.
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
     * Metod f�r att till�ta eller f�rneka att anv�ndaren kan interagera med
     * inmatningsf�ltet. Vid till�telse s� f�rfr�gar �ven inmatningsf�ltet om
     * att hamna i fokus. TODO
     * 
     * @param enable
     *            till�telse
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
     * Metod f�r att byta titeln hos f�nstret i det grafiska
     * anv�ndargr�nssnittet.
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
     * Metod f�r att kolla upp huruvida det grafiska anv�ndargr�nssnittet �r
     * redo eller inte. Vad som menas med redo �r ifall den �r instansierad och
     * fullt uppbyggd.
     * 
     * @return {@code true} om redo, i annat fall {@code false}
     */
    public final boolean isReady() {
	return (this.frame != null);
    }

    /**
     * Metod som �r till f�r att instansiera f�nstret i det grafiska
     * anv�ndargr�nssnittet. Initialiseringen sker i en annan tr�d f�r att inte
     * det skall leda till att servern upplevs som icke-responsiv.
     * 
     * @return ett instansierat och f�rdigbyggt objekt utav klassen
     *         {@link JFrame}
     * @throws InterruptedException
     *             om bygget utav det grafiska anv�ndargr�nssnittet avbr�ts
     * @throws ExecutionException
     *             om bygget utav det grafiska anv�ndargr�nssnittet inte kunde
     *             slutf�ras
     */
    private static final JFrame initializeFrame(GUI gui)
	    throws InterruptedException, ExecutionException {

	/*
	 * Definierar instruktionerna f�r arbetet som kr�vs f�r att s�tta ihop
	 * f�nstret...
	 */
	FutureTask<JFrame> frameBuildingTask = new FutureTask<>(
		new Callable<JFrame>() {

		    @Override
		    public JFrame call() {
			JFrame tmpFrame = new JFrame("Initializing service...");

			/*
			 * Best�mmer den layout-hanterare som f�nstret skall ha.
			 */
			tmpFrame.setLayout(new BorderLayout());

			/*
			 * Instansierar objektets 'papper'.
			 */
			gui.paper = new Paper();

			/*
			 * L�gger till en lyssnare som registrerar mus-klick.
			 * F�rutsatt att interaktion med det grafiska
			 * anv�ndargr�nssnittet �r till�tet s� kommer mus-klick
			 * producera en ny, ritad punkt p� 'pappret'. Dessutom
			 * kommer l�mplig delegat-metod anropas.
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
			 * L�gger till en lyssnare som registrerar mus-r�relser.
			 * F�rutsatt att interaktion med det grafiska
			 * anv�ndargr�nssnittet �r till�tet s� kommer mus-drag
			 * producera nya, ritade punkter p� 'pappret'. Dessutom
			 * kommer l�mplig delegat-metod anropas.
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
			 * Placerar 'pappret' centralt i f�nstret.
			 */
			tmpFrame.add(gui.paper, BorderLayout.CENTER);

			/*
			 * Anpassar egenskaper f�r f�nstret.
			 */
			tmpFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			tmpFrame.pack();
			tmpFrame.setMinimumSize(tmpFrame.getPreferredSize());
			tmpFrame.setResizable(false);
			tmpFrame.setVisible(true);

			/*
			 * Returnerar det ihopsatta f�nstret.
			 */
			return tmpFrame;
		    }
		});

	/*
	 * Utf�r arbetet...
	 */
	EventQueue.invokeLater(frameBuildingTask);

	/*
	 * Returnerar det nyligen ihopsatta f�nstret. V�ntar om det beh�vs.
	 */
	return frameBuildingTask.get();
    }

    /**
     * Det till denna klass tillh�rande gr�nssnitt som beskriver den eller dem
     * delegerade "call-back" metod(er) som internt skall anropas av det �gande
     * objeket vid olika scenarion.
     * 
     * @author Atilla �zkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface Delegate {
	/**
	 * Den metod som internt anropas n�r en f�r�ndring (ny ritning) sker
	 * lokalt.
	 *
	 * @param x
	 *            x-koordinaten av f�r�ndringen
	 * @param y
	 *            y-koordinaten av f�r�ndringen
	 */
	void onLocalEdit(short x, short y);
    }
}
