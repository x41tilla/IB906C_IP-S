/**
 * Trådad-klass som är ärver av klassen {@link Thread}
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class T1 extends Thread {

    /**
     * Boolsk instans-variabel som indikerar huruvida tråden är aktiv eller inte
     */
    private boolean aktiv;

    /**
     * Konstruerar och startar tråden
     */
    public T1() {
	this.start();
    }

    // Överskuggar den från superklassen ärvda metoden run()
    @Override
    public void run() {
	
	// Sätter aktiv-flaggan till true
	this.setAktiv(true);
	
	// Sålänge som tråden körs:
	while (this.aktiv) {
	    
	    // Skriver ut sin identitet
	    System.out.println("Tråd 1");

	    // Försöker sova i 1000 millisekunder, dvs i 1 sekund
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Metod för att kolla om tråd-objektet är aktivt eller inte
     * 
     * @return true om tråden är aktiv, annars false
     */
    public boolean isAktiv() {
	return this.aktiv;
    }

    /**
     * Metod för att aktivera eller avaktivera tråd-objektet
     * 
     * @param tillstånd
     *            dess nya tillstånd
     */
    public void setAktiv(boolean tillstånd) {
	this.aktiv = tillstånd;
    }
}
