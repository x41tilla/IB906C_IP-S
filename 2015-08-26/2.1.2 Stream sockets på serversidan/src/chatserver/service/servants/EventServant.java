package chatserver.service.servants;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import chatserver.service.misc.Event;

/**
 * TODO
 *
 * @author
 *
 */
public class EventServant extends AbstractServant {

    private final BlockingQueue<Event> firedEvents = new LinkedBlockingQueue<>();

    /**
     * TODO
     *
     */
    public EventServant() {
	this.task = new AbstractServantTask() {

	    @Override
	    void init() {
		/*
		 * Utebliven handling vid start.
		 */
	    }

	    @Override
	    synchronized void serve() throws InterruptedException {
		EventServant.this.firedEvents.take().execute();
	    }

	    @Override
	    void exit() {
		/*
		 * Utebliven handling vid avslut.
		 */
	    }
	};
    }

    /**
     * TODO
     *
     * @param event
     */
    public void dispatchEvent(Event event) {
	try {
	    this.firedEvents.put(event);
	} catch (InterruptedException e1) {
	    this.stopServing();
	}
    }
}
