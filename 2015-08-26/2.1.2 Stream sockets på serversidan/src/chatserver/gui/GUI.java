package chatserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Klass som ansvarar f�r det grafiska anv�ndargr�nssnittet mot servern i form
 * av ett f�nster.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class GUI {
    private Delegate delegate;
    private final JFrame frame;
    private JTextField inputField;
    private JTextArea outputArea;

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
	    public void onMessageSending(String message) {
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
			 * Skapar en etikett som uppmanar anv�ndaren till att
			 * skriva in ett meddelande i r�tt textf�lt.
			 */
			JLabel inputFieldLabel = new JLabel("Enter text:");
			inputFieldLabel.setOpaque(true);
			inputFieldLabel.setBackground(Color.BLACK);
			inputFieldLabel.setForeground(Color.GREEN);

			/*
			 * Instansierar den textf�lt som skall anv�ndas.
			 */
			gui.inputField = new JTextField(30);

			/*
			 * Det textf�lt d�r meddelandena skall matas in f�r att
			 * sedan skickas till samtliga anslutna klienter.
			 * Interaktion med textf�ltet avaktiveras dock, f�r att
			 * senare �teraktiveras n�r det grafiska gr�nssnittet
			 * v�l speglar mot en k�rande tj�nst.
			 */
			gui.enableInput(false);
			gui.inputField.setForeground(Color.GREEN);
			gui.inputField.setCaretColor(Color.WHITE);

			/*
			 * Textf�ltet f�r inmatning utav meddelanden f�r en
			 * h�ndelselyssnare - trycker man p� ENTER-tangenten
			 * medan textf�ltet �r i fokus s� skickas inneh�llet i
			 * textf�ltet, sedan t�ms det.
			 */
			gui.inputField.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent e) {
				String message = gui.inputField.getText();
				gui.delegate.onMessageSending(message);
				gui.inputField.setText("");
			    }
			});

			/*
			 * Instansierar lokalt den grafiska komponent som ska
			 * inneh�lla b�gge komponenter som n�mnts ovan.
			 */
			JPanel northPanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
			northPanel.setBackground(Color.BLACK);
			northPanel.add(inputFieldLabel);
			northPanel.add(gui.inputField);
			northPanel.setBorder(BorderFactory.createMatteBorder(0,
				0, 3, 0, Color.WHITE));
			tmpFrame.add(northPanel, BorderLayout.NORTH);

			/*
			 * Instansierar den textarea som skall anv�ndas.
			 */
			gui.outputArea = new JTextArea(5, 30);

			/*
			 * Anpassar egenskaperna f�r en text-area och placerar
			 * sedan denna i en scrollbar panel som i sin tur
			 * placeras i f�nstret. I denna area ska mottagna
			 * meddelanden visas.
			 */
			gui.outputArea.setEditable(false);
			gui.outputArea.setBackground(Color.BLACK);
			gui.outputArea.setForeground(Color.GREEN);
			JScrollPane outputPane = new JScrollPane(gui.outputArea);
			tmpFrame.add(outputPane, BorderLayout.CENTER);

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
     * Metod f�r att till�ta eller f�rneka att anv�ndaren kan interagera med
     * inmatningsf�ltet. Vid till�telse s� f�rfr�gar �ven inmatningsf�ltet om
     * att hamna i fokus.
     * 
     * @param enable
     *            till�telse
     */
    public final void enableInput(boolean enable) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		GUI.this.inputField.setEnabled(enable);

		if (enable) {
		    GUI.this.inputField.requestFocusInWindow();
		    GUI.this.inputField.setBackground(Color.BLACK);
		} else {
		    GUI.this.inputField.setBackground(Color.GRAY);
		}
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
     * Metod f�r att grafisk visa ett meddelande i text-arean.
     * 
     * @param message
     *            meddelandet som skall visas
     */
    public final void printMessage(String message) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		GUI.this.outputArea.append(message + '\n');
	    }
	});
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
		GUI.this.frame.setTitle(newTitle);
	    }
	});
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
	 * Den metod som internt anropas n�r ett meddelande skall s�ndas fr�n
	 * servern.
	 * 
	 * @param message
	 *            det meddelande som skall skickas
	 */
	void onMessageSending(String message);
    }
}
