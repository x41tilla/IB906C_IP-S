package chatserver.service;

import java.io.IOException;
import java.net.BindException;
import java.net.Socket;
import java.util.EventListener;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chatserver.service.misc.BroadcastTask;
import chatserver.service.misc.Client;
import chatserver.service.misc.Event;
import chatserver.service.servants.ClientServant;
import chatserver.service.servants.ConnectionServant;
import chatserver.service.servants.EventServant;

/**
 * Klass utgör den tjänst som omfattar den data och kommunikation som är
 * relevant till anslutningarna mellan klient(er) och servern.
 * 
 * @author Atilla Özkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class Service {

    private final HashMap<Client, ClientServant> clients = new HashMap<>();
    private final ConnectionServant connectionServant;
    private ServiceListener eventListener;

    private final EventServant eventServant;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Skapar ett objekt av klassen {@link Service} som lyssnar på en
     * specifierad port.
     * 
     * @param port
     *            nätverksporten som servern ska lyssna på
     * @throws IllegalArgumentException
     *             om angiven nätverksport inte är en giltig sådan
     * @throws BindException
     *             om nätverksporten inte går att lyssna på
     * @throws IOException
     *             vid problem orsakat när en socket instanserats för att lyssna
     *             på bestämd port
     */
    public Service(int port) throws IllegalArgumentException, BindException,
	    IOException {

	this.eventServant = new EventServant();

	/*
	 * Delegat-objektet instanseras till att inte göra något vid någon
	 * händelse över huvud taget. Detta för att undvika att instansvariabeln
	 * istället pekar mot null.
	 */
	this.eventListener = new ServiceListener() {

	    @Override
	    public void onClientRemovalError(String author) {
		/*
		 * Utebliven handling.
		 */
	    }

	    @Override
	    public void onConnectionInitializationError(String message) {
		/*
		 * Utebliven handling.
		 */
	    }

	    @Override
	    public void onConnectionLost() {
		/*
		 * Utebliven handling.
		 */
	    }

	    @Override
	    public void onMessageRecieved(String author, String message) {
		/*
		 * Utebliven handling.
		 */
	    }

	    @Override
	    public void onNewConnection() {
		/*
		 * Utebliven handling.
		 */
	    }
	};

	/*
	 * Instansierar instansvariabeln som är utav klassen ConnectionServant
	 * och som har i syfte att lyssna på samt hantera nya anslutningar från
	 * klienter. Instansieringen kräver att man förutom nätverks-porten även
	 * skickar med definierade "call-back"-metoder som bestämmer vad som
	 * skall göras i olika scenarion.
	 */
	this.connectionServant = new ConnectionServant(port,
		new ConnectionServant.Listener() {

		    @Override
		    public void onConnectionInitializationError(
			    String errorMessage) {
			Service.this.eventServant.dispatchEvent(new Event() {

			    @Override
			    public void execute() {
				Service.this.eventListener
					.onConnectionInitializationError(errorMessage);
			    }
			});
		    }

		    @Override
		    public void onNewConnection(Socket newConnection) {
			Service.this.eventServant.dispatchEvent(new Event() {

			    @Override
			    public void execute() {
				Service.this.addClient(newConnection);
				Service.this.eventListener.onNewConnection();
			    }
			});
		    }
		});
    }

    /**
     * Metod för att registrera det delegat-objekt med den eller de metoder som
     * skall delegeras till de två tjänar-objekt - varav den ena lyssnar på nya
     * inkommande anslutningsförsök medan den andra massutskickar meddelanden
     * till samtliga anslutna klienter.
     *
     * @param listener
     *            det objekt med den eller dem delegerade metoder
     */
    public final void addServiceListener(ServiceListener listener) {
	this.eventListener = listener;
    }

    /**
     * Metod för att skapa nya massutskicksjobb som instansvariabeln utav
     * klassen BroadcastingServant skall ta hand om. Vid skapandet av dessa jobb
     * så definieras även "call-back"-metoden som bestämmer vilka åtgärder som
     * skall tas vid olika scenarion. Har denna metod redan definierats i
     * samband med ett tidigare jobb så överskrids det gamla med det nya.
     * 
     * @param message
     *            det meddalande som skall skickas till samtliga anslutna
     *            klienter i ett massutskick
     */
    public final void broadcastMessage(String message) {

	/*
	 * För varje klient registrerade hos datasamlingen över alla anslutna
	 * klienter...
	 */
	for (Client recievingClient : this.clients.keySet()) {
	    /*
	     * Så skapas ett utskicksuppdrag adresserat till klienten ifråga.
	     */
	    BroadcastTask.Events broadcastDelegate = new BroadcastTask.Events() {

		@Override
		public void onConnectionLost() {
		    Service.this.removeClient(recievingClient);
		    Service.this.eventListener.onConnectionLost();
		}
	    };

	    /*
	     * Skapar ett nytt objekt som är utav klassen BroadcastTask som
	     * utgör ett utskicksjobb addreserat till klienten som berörs under
	     * denna iteration i loopen.
	     */
	    BroadcastTask broadcastTask = new BroadcastTask(broadcastDelegate,
		    recievingClient.getWriter(), message);

	    /*
	     * När objeketet är instansierat så placeras den i trådpoolen vid
	     * lämplig tidpunkt.
	     */
	    this.eventServant.dispatchEvent(new Event() {
		@Override
		public void execute() {
		    Service.this.threadPool.submit(broadcastTask);
		}
	    });
	}
    }

    /**
     * Blockerande metod som startar tjänsten och vidtar åtgärder vid händelser.
     * Avbryts applikationen under körning så slutar tjänsten att köras.
     *
     */
    public void startServing() {
	this.threadPool.submit(this.connectionServant.getTask());
	this.threadPool.submit(this.eventServant.getTask());
    }

    /**
     * Metod som bygger upp och returnerar en sträng som representerar serverns
     * aktuella status.
     * 
     * @return sträng som representerar serverns status
     */
    @Override
    public String toString() {
	StringBuilder representation = new StringBuilder();
	representation.append(this.connectionServant.toString());
	representation.append(" ---> ");
	representation.append(" Clients: ");
	representation.append(this.clients.size());

	return representation.toString();
    }

    /**
     * Hjälpmetod som hanterar en ny klient.
     * 
     * @param connectionToNewClient
     *            anslutning till den klient som skall adderas
     */
    private final void addClient(Socket connectionToNewClient) {

	try {
	    /*
	     * Försöker skapa en ny klient som lutar mot den nya anslutningen
	     * som skickats som argument till metoden.
	     */
	    Client newClient = new Client(connectionToNewClient);

	    /*
	     * Ett ny klientlyssnare (objekt utav klassen ClientServant) skapas
	     * och dess "call-back"-metoder definieras.
	     */
	    final ClientServant newClientsServant;
	    newClientsServant = new ClientServant(newClient.getReader(),
		    new ClientServant.Listener() {

			@Override
			public void onConnectionLost() {

			    Service.this.removeClient(newClient);
			    Service.this.eventListener.onConnectionLost();

			}

			@Override
			public void onMessageReceived(String message) {

			    Service.this.eventListener.onMessageRecieved(
				    newClient.toString(), message);

			}
		    });

	    /*
	     * Den nya klienten placeras i datasamlingen över anslutna klienter.
	     */
	    this.clients.put(newClient, newClientsServant);

	    /*
	     * Klientens lyssnar-objekt placeras i trådpoolen för parallell
	     * exekvering.
	     */
	    this.threadPool.submit(newClientsServant.getTask());
	} catch (IOException e) {
	    /*
	     * Misslyckas initialisering utav anslutningen så anropas lämplig
	     * delegerad metod.
	     */
	    this.eventListener.onConnectionInitializationError(e.getMessage());
	}

    }

    /**
     * Hjälpmetod för att terminera en klient. Sker ett problem vid stängning av
     * anslutningen så finns inte mycket att göra åt detta, utan klienten tas
     * bort från datasamlingen.
     * 
     * @param client
     *            klienten som skall tas bort
     * 
     */
    private void removeClient(Client client) {
	/*
	 * Klientens tjänare hämtas från datasamligen för att sedan stoppas.
	 */
	ClientServant clientsServant = this.clients.get(client);
	clientsServant.stopServing();

	try {
	    /*
	     * Klientens tillhörande anslutningar stoppas och termineras, genom
	     * att anropa dess lämpliga metod.
	     */
	    client.kill();
	} catch (IOException e) {
	    /*
	     * Lyckas inte klienten terminera dess läsare/skrivare så finns inte
	     * mycket annat att göra åt detta. Vid det här laget har ändå
	     * klienten tagits bort från datasamlingen över klienter, så den bör
	     * städas bort av skräphanteraren inom rimlig tid. Lämplig delegerad
	     * metod anropas.
	     */
	    this.eventListener.onClientRemovalError(client.toString());
	} finally {
	    /*
	     * Slutligen så tas clienten och dess associerade tjänare bort från
	     * datasamligen.
	     */
	    this.clients.remove(client);
	}
    }

    /**
     * Det till denna klass tillhörande gränssnitt som beskriver de delegerade
     * metoder som skall deklareras externt, beroende på hur det objekt som
     * använder sig utav denna klass vill ha det.
     *
     * @author Atilla Özkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface ServiceListener extends EventListener {
	/**
	 * Syftet av denna metod är att utföra lämplig operation när tjänsten
	 * stött på ett problem vid terminering utav en befintlig klient.
	 * 
	 * @param author
	 *            en sträng som representerar den klient som orsakat
	 *            problemet
	 */
	public void onClientRemovalError(String author);

	/**
	 * Syftet av denna metod är att indikera att det skett ett fel vid
	 * initialisering utav ett anslutningsförsök från en klient.
	 *
	 * @param errorMessage
	 *            ett felmeddelande som skickas vidare till det objekt som
	 *            deklarerat metoden
	 */
	public void onConnectionInitializationError(String errorMessage);

	/**
	 * Syftet av denna metod är att indikera att anslutningen till en klient
	 * har brutits.
	 */
	public void onConnectionLost();

	/**
	 * Syftet av denna metod är att utföra lämplig operation när ett
	 * meddelande har mottagits utav ett tjänar-objekt som lyssnar på nya
	 * inkommande meddelanden från en klient.
	 *
	 * @param author
	 *            en sträng som representerar den klient som skickat
	 *            meddelandet
	 * @param message
	 *            meddelandet som mottagits
	 */
	public void onMessageRecieved(String author, String message);

	/**
	 * Syftet av denna metod är att indikera att en ny klient har anslutit
	 * till servern.
	 */
	public void onNewConnection();
    }
}
