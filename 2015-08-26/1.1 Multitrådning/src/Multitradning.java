/**
 * Huvudklass/tillämpningsprogram som instansierar och kör objekt av klasserna
 * {@link T1} samt {@link T2}
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public class Multitradning {

    // Huvudklassens main-metod
    public static void main(String[] args) {

	// Instansierar ett objekt av trådklassen 'T1'. Observera att
	// konstruktorn
	// startar tråden!
	T1 tråd1 = new T1();

	// Huvudklassen försöker sova i 5000 millisekunder, dvs i 5 sekunder
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Skapar en instans av "quasi"-trådklassen 'T2'. Observera att
	// konstruktorn
	// startar tråden!
	T2 tråd2 = new T2();

	// Huvudklassen försöker sova i 5000 millisekunder, dvs i 5 sekunder
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Stoppar trådobjektet 'tråd1' av klassen 'T1'
	tråd1.setAktiv(false);

	// Huvudklassen försöker sova i 5000 millisekunder, dvs i 5 sekunder
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// Stoppar trådobjektet 'tråd2' av klassen 'T2'
	tråd2.setAktiv(false);
    }
}
