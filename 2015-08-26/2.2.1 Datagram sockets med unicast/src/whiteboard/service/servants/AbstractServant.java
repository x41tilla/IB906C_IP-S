package whiteboard.service.servants;

import java.util.EventListener;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Klass som utgör grunden till tjänar-klasser som har i syfte att specialiseras
 * på ett uppdrag.
 *
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 * @param <I>
 *            den delegat-klass som innehåller den eller dem metoder som internt
 *            skall anropas av det ägande objeket vid olika scenarion
 */
abstract class AbstractServant<I extends EventListener> {
    /**
     * En "atomär" referens till den tråd som skall exekvera denna tjänares
     * uppgift.
     */
    private volatile AtomicReference<Thread> thread = new AtomicReference<>();

    /**
     * Det delegat som innehåller den eller dem "call-back" metoder som internt
     * skall anropas av det ägande objeket vid olika scenarion.
     */
    protected I delegate;

    /**
     * Tjänarens körbara uppdrag.
     */
    private final Callable<Void> task;

    /**
     * Flagga för huruvida tjänaren betjänar eller inte. Variabeln är "volatile"
     * då åtkomst till denna kan komma att ske trådar emellan.
     */
    private volatile AtomicBoolean isServing = new AtomicBoolean(false);

    /**
     * Konstruktor som per automatik körs innan sub-klassers konstruktorer körs.
     * Denna konstruktor configuerar uppdraget och dess olika stadier samt
     * beteénden.
     */
    protected AbstractServant() {

	/*
	 * Instansierar uppdraget...
	 */
	this.task = new Callable<Void>() {

	    @Override
	    public Void call() throws Exception {

		/*
		 * Referensen uppdateras till den aktuella tråd som exekverar
		 * tjänar-objektets uppdrag.
		 */
		AbstractServant.this.thread.set(Thread.currentThread());

		/*
		 * Först anropas den interna metod som är till för
		 * initialisering utav betjäning..
		 */
		init();

		/*
		 * Sedan anropas den interna metod som är till för att betjäna.
		 * Denna metod anropas sålänge som tjänaren har tillåtelse att
		 * betjäna. Flaggan som indikerar om tjänaren betjänar
		 * uppdateras före det att betjäningen påbörjas.
		 */
		AbstractServant.this.isServing.set(true);
		while (!Thread.currentThread().isInterrupted()
			&& AbstractServant.this.isServing())
		    serve();

		/*
		 * Inget returneras vid avslut av betjäning.
		 */
		return null;
	    }
	};
    }

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas 1 gång
     * innan betjäning och har i syfte att användas för att utföra sådant som är
     * nödvändigt innan betjäning påbörjas.
     */
    abstract void init();

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas sålänge
     * som betjäning pågår.
     * 
     * @throws InterruptedException
     *             ifall tråden som exekverar betjäningen störs av ett avbrott.
     */
    abstract void serve() throws InterruptedException;

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas 1 gång
     * efter betjäning och har i syfte att användas för att "städa upp" ifall
     * det krävs av implementeraren.
     */
    abstract void exit();

    /**
     * Metod som indikerar huruvida tjänaren betjänar eller inte.
     *
     * @return {@code true} om betjäning pågår, i annat fall {@code false}
     */
    public final boolean isServing() {
	return this.isServing.get();
    }

    /**
     * Synkroniserad metod för att stoppa tjänaren och få denne att sluta
     * betjäna.
     */
    public synchronized final void stopServing() {

	/*
	 * Tråden som exekverar tjänar-objektets uppdrag avbryts - förutsatt att
	 * uppdraget redan exekveras utav en tråd.
	 */
	if (this.thread.get() != null)
	    this.thread.get().interrupt();

	/*
	 * Flaggan som indikerar körningens tillstånd ändras.
	 */
	this.isServing.set(false);

	/*
	 * Den interna metod som är till för avslut utav betjäning anropas.
	 */
	exit();
    }

    /**
     * 
     * Metod som returnerar tjänarens körbara uppdrag.
     *
     * @return tjänarens körbara uppdrag
     */
    public final Callable<Void> getTask() {
	return this.task;
    }

    /**
     * Metod för att registrera ett delegat-objekt som direkt eller indirekt
     * implementerar gränssnittet {@link EventListener}.
     *
     * @param delegate
     *            det delegat-objekt som skall registreras utav objektet
     */
    public void setDelegate(I delegate) {
	this.delegate = delegate;
    }
}