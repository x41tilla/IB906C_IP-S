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
 * Klass som ansvarar för det grafiska användargränssnittet mot servern i form
 * av ett fönster.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class GUI {
    private Delegate delegate;
    private final JFrame frame;
    private JTextField inputField;
    private JTextArea outputArea;

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
	    public void onMessageSending(String message) {
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
			 * Skapar en etikett som uppmanar användaren till att
			 * skriva in ett meddelande i rätt textfält.
			 */
			JLabel inputFieldLabel = new JLabel("Enter text:");
			inputFieldLabel.setOpaque(true);
			inputFieldLabel.setBackground(Color.BLACK);
			inputFieldLabel.setForeground(Color.GREEN);

			/*
			 * Instansierar den textfält som skall användas.
			 */
			gui.inputField = new JTextField(30);

			/*
			 * Det textfält där meddelandena skall matas in för att
			 * sedan skickas till samtliga anslutna klienter.
			 * Interaktion med textfältet avaktiveras dock, för att
			 * senare återaktiveras när det grafiska gränssnittet
			 * väl speglar mot en körande tjänst.
			 */
			gui.enableInput(false);
			gui.inputField.setForeground(Color.GREEN);
			gui.inputField.setCaretColor(Color.WHITE);

			/*
			 * Textfältet för inmatning utav meddelanden får en
			 * händelselyssnare - trycker man på ENTER-tangenten
			 * medan textfältet är i fokus så skickas innehållet i
			 * textfältet, sedan töms det.
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
			 * innehålla bägge komponenter som nämnts ovan.
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
			 * Instansierar den textarea som skall användas.
			 */
			gui.outputArea = new JTextArea(5, 30);

			/*
			 * Anpassar egenskaperna för en text-area och placerar
			 * sedan denna i en scrollbar panel som i sin tur
			 * placeras i fönstret. I denna area ska mottagna
			 * meddelanden visas.
			 */
			gui.outputArea.setEditable(false);
			gui.outputArea.setBackground(Color.BLACK);
			gui.outputArea.setForeground(Color.GREEN);
			JScrollPane outputPane = new JScrollPane(gui.outputArea);
			tmpFrame.add(outputPane, BorderLayout.CENTER);

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
     * Metod för att tillåta eller förneka att användaren kan interagera med
     * inmatningsfältet. Vid tillåtelse så förfrågar även inmatningsfältet om
     * att hamna i fokus.
     * 
     * @param enable
     *            tillåtelse
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
     * Metod för att grafisk visa ett meddelande i text-arean.
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
		GUI.this.frame.setTitle(newTitle);
	    }
	});
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
	 * Den metod som internt anropas när ett meddelande skall sändas från
	 * servern.
	 * 
	 * @param message
	 *            det meddelande som skall skickas
	 */
	void onMessageSending(String message);
    }
}
