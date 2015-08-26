/**
 * Tr�dad-klass som �r implementerar gr�nssnittet {@link Runnable}
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class T2 implements Runnable {

    /**
     * Tr�d som skall k�ra objektet
     */
    private Thread k�rTr�d;

    /**
     * Boolsk instans-variabel som indikerar huruvida tr�den �r aktiv eller inte
     */
    private boolean aktiv;

    // Konstruerar och startar tr�den som "k�r" objektet
    /**
     * Initierar instans-tr�den {@link #k�rTr�d}, startar tr�den med objektet -
     * vars {@link #run()}-metod skall k�ras - som argument
     */
    public T2() {
	k�rTr�d = new Thread(this);
	k�rTr�d.start();
    }

    // �verskuggar den fr�n gr�nsnittet �rvda metoden run()
    @Override
    public void run() {
	
	// S�tter aktiv-flaggan till true
	this.aktiv = true;
	
	// S�l�nge som tr�den k�rs:
	while (this.aktiv) {
	    
	    // Skriver ut sin identitet
	    System.out.println("Tr�d 2");

	    // F�rs�ker sova i 1000 millisekunder, dvs i 1 sekund
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Metod f�r att kolla om tr�d-objektet �r aktivt eller inte
     * 
     * @return true om tr�den �r aktiv, annars false
     */
    public boolean isAktiv() {
	return this.aktiv;
    }

    /**
     * Metod f�r att aktivera eller avaktivera tr�d-objektet
     * 
     * @param tillst�nd
     *            dess nya tillst�nd
     */
    public void setAktiv(boolean tillst�nd) {
	this.aktiv = tillst�nd;
    }
}
