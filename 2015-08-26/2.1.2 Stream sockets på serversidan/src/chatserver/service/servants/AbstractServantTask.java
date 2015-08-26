package chatserver.service.servants;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO
 *
 * @author
 *
 */
public abstract class AbstractServantTask implements Callable<Void> {

    /**
     * En "atomär" referens till den tråd som skall exekvera denna tjänares
     * uppgift.
     */
    private volatile AtomicReference<Thread> thread = new AtomicReference<>();

    @Override
    public Void call() {

	/*
	 * Referensen uppdateras till den aktuella tråd som exekverar
	 * tjänar-objektets uppdrag.
	 */
	AbstractServantTask.this.thread.set(Thread.currentThread());

	/*
	 * Först anropas den interna metod som är till för initialisering utav
	 * betjäning..
	 */
	init();

	/*
	 * Sedan anropas den interna metod som är till för att betjäna. Denna
	 * metod anropas sålänge som tjänaren har tillåtelse att betjäna.
	 * Flaggan som indikerar om tjänaren betjänar uppdateras före det att
	 * betjäningen påbörjas.
	 */
	try {
	    while (!AbstractServantTask.this.thread.get().isInterrupted())
		serve();
	} catch (InterruptedException e) {
	    exit();
	}

	/*
	 * Inget returneras vid avslut av betjäning.
	 */
	return null;
    }

    /**
     * Metod som indikerar huruvida tjänaren betjänar eller inte.
     *
     * @return {@code true} om betjäning pågår, i annat fall {@code false}
     */
    public final boolean isServing() {
	return this.thread.get().isAlive();
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
    }

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas 1 gång
     * efter betjäning och har i syfte att användas för att "städa upp" ifall
     * det krävs av implementeraren.
     */
    abstract void exit();

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
}
