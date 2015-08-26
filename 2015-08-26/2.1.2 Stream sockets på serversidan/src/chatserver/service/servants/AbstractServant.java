package chatserver.service.servants;

import java.util.concurrent.Callable;

/**
 * Klass som utg�r grunden till tj�nar-klasser som har i syfte att specialiseras
 * p� ett uppdrag.
 *
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
abstract class AbstractServant {
    /**
     * Tj�narens k�rbara uppdrag.
     */
    protected AbstractServantTask task;

    /**
     * Metod som indikerar huruvida tj�naren betj�nar eller inte.
     *
     * @return {@code true} om betj�ning p�g�r, i annat fall {@code false}
     */
    public final boolean isServing() {
	return this.task.isServing();
    }

    /**
     * Synkroniserad metod f�r att stoppa tj�naren och f� denne att sluta
     * betj�na.
     */
    public synchronized final void stopServing() {
	this.task.stopServing();
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
}