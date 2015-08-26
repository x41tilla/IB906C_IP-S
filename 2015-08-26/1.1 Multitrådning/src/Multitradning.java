/**
 * Huvudklass/till�mpningsprogram som instansierar och k�r objekt av klasserna
 * {@link T1} samt {@link T2}
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Multitradning {

    // Huvudklassens main-metod
    public static void main(String[] args) {

	// Instansierar ett objekt av tr�dklassen 'T1'. Observera att
	// konstruktorn
	// startar tr�den!
	T1 tr�d1 = new T1();

	// Huvudklassen f�rs�ker sova i 5000 millisekunder, dvs i 5 sekunder
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Skapar en instans av "quasi"-tr�dklassen 'T2'. Observera att
	// konstruktorn
	// startar tr�den!
	T2 tr�d2 = new T2();

	// Huvudklassen f�rs�ker sova i 5000 millisekunder, dvs i 5 sekunder
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Stoppar tr�dobjektet 'tr�d1' av klassen 'T1'
	tr�d1.setAktiv(false);

	// Huvudklassen f�rs�ker sova i 5000 millisekunder, dvs i 5 sekunder
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Stoppar tr�dobjektet 'tr�d2' av klassen 'T2'
	tr�d2.setAktiv(false);
    }
}
