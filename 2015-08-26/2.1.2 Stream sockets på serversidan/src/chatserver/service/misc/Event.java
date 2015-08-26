package chatserver.service.misc;

/**
 * Gr�nssnitt som �r till f�r att implementeras utav klasser som utg�r
 * h�ndelser.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public interface Event {
    /**
     * Den metod som skall exekveras vid hantering utav h�ndelsen.
     */
    void execute();
}
