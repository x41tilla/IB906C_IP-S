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
     * En "atom�r" referens till den tr�d som skall exekvera denna tj�nares
     * uppgift.
     */
    private volatile AtomicReference<Thread> thread = new AtomicReference<>();

    @Override
    public Void call() {

	/*
	 * Referensen uppdateras till den aktuella tr�d som exekverar
	 * tj�nar-objektets uppdrag.
	 */
	AbstractServantTask.this.thread.set(Thread.currentThread());

	/*
	 * F�rst anropas den interna metod som �r till f�r initialisering utav
	 * betj�ning..
	 */
	init();

	/*
	 * Sedan anropas den interna metod som �r till f�r att betj�na. Denna
	 * metod anropas s�l�nge som tj�naren har till�telse att betj�na.
	 * Flaggan som indikerar om tj�naren betj�nar uppdateras f�re det att
	 * betj�ningen p�b�rjas.
	 */
	try {
	    while (!AbstractServantTask.this.thread.get().isInterrupted())
		serve();
	} catch (InterruptedException e) {
	    exit();
	}

	/*
	 * Inget returneras vid avslut av betj�ning.
	 */
	return null;
    }

    /**
     * Metod som indikerar huruvida tj�naren betj�nar eller inte.
     *
     * @return {@code true} om betj�ning p�g�r, i annat fall {@code false}
     */
    public final boolean isServing() {
	return this.thread.get().isAlive();
    }

    /**
     * Synkroniserad metod f�r att stoppa tj�naren och f� denne att sluta
     * betj�na.
     */
    public synchronized final void stopServing() {
	/*
	 * Tr�den som exekverar tj�nar-objektets uppdrag avbryts - f�rutsatt att
	 * uppdraget redan exekveras utav en tr�d.
	 */
	if (this.thread.get() != null)
	    this.thread.get().interrupt();
    }

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas 1 g�ng
     * efter betj�ning och har i syfte att anv�ndas f�r att "st�da upp" ifall
     * det kr�vs av implementeraren.
     */
    abstract void exit();

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas 1 g�ng
     * innan betj�ning och har i syfte att anv�ndas f�r att utf�ra s�dant som �r
     * n�dv�ndigt innan betj�ning p�b�rjas.
     */
    abstract void init();

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas s�l�nge
     * som betj�ning p�g�r.
     * 
     * @throws InterruptedException
     *             ifall tr�den som exekverar betj�ningen st�rs av ett avbrott.
     */
    abstract void serve() throws InterruptedException;
}
