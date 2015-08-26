package chatserver.service.servants;

import java.util.concurrent.Callable;

/**
 * Klass som utgör grunden till tjänar-klasser som har i syfte att specialiseras
 * på ett uppdrag.
 *
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
abstract class AbstractServant {
    /**
     * Tjänarens körbara uppdrag.
     */
    protected AbstractServantTask task;

    /**
     * Metod som indikerar huruvida tjänaren betjänar eller inte.
     *
     * @return {@code true} om betjäning pågår, i annat fall {@code false}
     */
    public final boolean isServing() {
	return this.task.isServing();
    }

    /**
     * Synkroniserad metod för att stoppa tjänaren och få denne att sluta
     * betjäna.
     */
    public synchronized final void stopServing() {
	this.task.stopServing();
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
}