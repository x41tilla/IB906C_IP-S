package whiteboard.service.servants;

import java.util.EventListener;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Klass som utg�r grunden till tj�nar-klasser som har i syfte att specialiseras
 * p� ett uppdrag.
 *
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 * @param <I>
 *            den delegat-klass som inneh�ller den eller dem metoder som internt
 *            skall anropas av det �gande objeket vid olika scenarion
 */
abstract class AbstractServant<I extends EventListener> {
    /**
     * En "atom�r" referens till den tr�d som skall exekvera denna tj�nares
     * uppgift.
     */
    private volatile AtomicReference<Thread> thread = new AtomicReference<>();

    /**
     * Det delegat som inneh�ller den eller dem "call-back" metoder som internt
     * skall anropas av det �gande objeket vid olika scenarion.
     */
    protected I delegate;

    /**
     * Tj�narens k�rbara uppdrag.
     */
    private final Callable<Void> task;

    /**
     * Flagga f�r huruvida tj�naren betj�nar eller inte. Variabeln �r "volatile"
     * d� �tkomst till denna kan komma att ske tr�dar emellan.
     */
    private volatile AtomicBoolean isServing = new AtomicBoolean(false);

    /**
     * Konstruktor som per automatik k�rs innan sub-klassers konstruktorer k�rs.
     * Denna konstruktor configuerar uppdraget och dess olika stadier samt
     * bete�nden.
     */
    protected AbstractServant() {

	/*
	 * Instansierar uppdraget...
	 */
	this.task = new Callable<Void>() {

	    @Override
	    public Void call() throws Exception {

		/*
		 * Referensen uppdateras till den aktuella tr�d som exekverar
		 * tj�nar-objektets uppdrag.
		 */
		AbstractServant.this.thread.set(Thread.currentThread());

		/*
		 * F�rst anropas den interna metod som �r till f�r
		 * initialisering utav betj�ning..
		 */
		init();

		/*
		 * Sedan anropas den interna metod som �r till f�r att betj�na.
		 * Denna metod anropas s�l�nge som tj�naren har till�telse att
		 * betj�na. Flaggan som indikerar om tj�naren betj�nar
		 * uppdateras f�re det att betj�ningen p�b�rjas.
		 */
		AbstractServant.this.isServing.set(true);
		while (!Thread.currentThread().isInterrupted()
			&& AbstractServant.this.isServing())
		    serve();

		/*
		 * Inget returneras vid avslut av betj�ning.
		 */
		return null;
	    }
	};
    }

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

    /**
     * Intern metod som skall deklareras utav objekt. Metoden anropas 1 g�ng
     * efter betj�ning och har i syfte att anv�ndas f�r att "st�da upp" ifall
     * det kr�vs av implementeraren.
     */
    abstract void exit();

    /**
     * Metod som indikerar huruvida tj�naren betj�nar eller inte.
     *
     * @return {@code true} om betj�ning p�g�r, i annat fall {@code false}
     */
    public final boolean isServing() {
	return this.isServing.get();
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

	/*
	 * Flaggan som indikerar k�rningens tillst�nd �ndras.
	 */
	this.isServing.set(false);

	/*
	 * Den interna metod som �r till f�r avslut utav betj�ning anropas.
	 */
	exit();
    }

    /**
     * 
     * Metod som returnerar tj�narens k�rbara uppdrag.
     *
     * @return tj�narens k�rbara uppdrag
     */
    public final Callable<Void> getTask() {
	return this.task;
    }

    /**
     * Metod f�r att registrera ett delegat-objekt som direkt eller indirekt
     * implementerar gr�nssnittet {@link EventListener}.
     *
     * @param delegate
     *            det delegat-objekt som skall registreras utav objektet
     */
    public void setDelegate(I delegate) {
	this.delegate = delegate;
    }
}