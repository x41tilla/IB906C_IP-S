package chatserver.service.misc;

/**
 * Gränssnitt som är till för att implementeras utav klasser som utgör
 * händelser.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public interface Event {
    /**
     * Den metod som skall exekveras vid hantering utav händelsen.
     */
    void execute();
}
