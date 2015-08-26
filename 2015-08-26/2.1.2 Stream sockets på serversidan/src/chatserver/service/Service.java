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
 * Klass utg�r den tj�nst som omfattar den data och kommunikation som �r
 * relevant till anslutningarna mellan klient(er) och servern.
 * 
 * @author Atilla �zkan | 930304-4474 | atoz0393
 * @version 1.0
 */
public final class Service {

    private final HashMap<Client, ClientServant> clients = new HashMap<>();
    private final ConnectionServant connectionServant;
    private ServiceListener eventListener;

    private final EventServant eventServant;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Skapar ett objekt av klassen {@link Service} som lyssnar p� en
     * specifierad port.
     * 
     * @param port
     *            n�tverksporten som servern ska lyssna p�
     * @throws IllegalArgumentException
     *             om angiven n�tverksport inte �r en giltig s�dan
     * @throws BindException
     *             om n�tverksporten inte g�r att lyssna p�
     * @throws IOException
     *             vid problem orsakat n�r en socket instanserats f�r att lyssna
     *             p� best�md port
     */
    public Service(int port) throws IllegalArgumentException, BindException,
	    IOException {

	this.eventServant = new EventServant();

	/*
	 * Delegat-objektet instanseras till att inte g�ra n�got vid n�gon
	 * h�ndelse �ver huvud taget. Detta f�r att undvika att instansvariabeln
	 * ist�llet pekar mot null.
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
	 * Instansierar instansvariabeln som �r utav klassen ConnectionServant
	 * och som har i syfte att lyssna p� samt hantera nya anslutningar fr�n
	 * klienter. Instansieringen kr�ver att man f�rutom n�tverks-porten �ven
	 * skickar med definierade "call-back"-metoder som best�mmer vad som
	 * skall g�ras i olika scenarion.
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
     * Metod f�r att registrera det delegat-objekt med den eller de metoder som
     * skall delegeras till de tv� tj�nar-objekt - varav den ena lyssnar p� nya
     * inkommande anslutningsf�rs�k medan den andra massutskickar meddelanden
     * till samtliga anslutna klienter.
     *
     * @param listener
     *            det objekt med den eller dem delegerade metoder
     */
    public final void addServiceListener(ServiceListener listener) {
	this.eventListener = listener;
    }

    /**
     * Metod f�r att skapa nya massutskicksjobb som instansvariabeln utav
     * klassen BroadcastingServant skall ta hand om. Vid skapandet av dessa jobb
     * s� definieras �ven "call-back"-metoden som best�mmer vilka �tg�rder som
     * skall tas vid olika scenarion. Har denna metod redan definierats i
     * samband med ett tidigare jobb s� �verskrids det gamla med det nya.
     * 
     * @param message
     *            det meddalande som skall skickas till samtliga anslutna
     *            klienter i ett massutskick
     */
    public final void broadcastMessage(String message) {

	/*
	 * F�r varje klient registrerade hos datasamlingen �ver alla anslutna
	 * klienter...
	 */
	for (Client recievingClient : this.clients.keySet()) {
	    /*
	     * S� skapas ett utskicksuppdrag adresserat till klienten ifr�ga.
	     */
	    BroadcastTask.Events broadcastDelegate = new BroadcastTask.Events() {

		@Override
		public void onConnectionLost() {
		    Service.this.removeClient(recievingClient);
		    Service.this.eventListener.onConnectionLost();
		}
	    };

	    /*
	     * Skapar ett nytt objekt som �r utav klassen BroadcastTask som
	     * utg�r ett utskicksjobb addreserat till klienten som ber�rs under
	     * denna iteration i loopen.
	     */
	    BroadcastTask broadcastTask = new BroadcastTask(broadcastDelegate,
		    recievingClient.getWriter(), message);

	    /*
	     * N�r objeketet �r instansierat s� placeras den i tr�dpoolen vid
	     * l�mplig tidpunkt.
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
     * Blockerande metod som startar tj�nsten och vidtar �tg�rder vid h�ndelser.
     * Avbryts applikationen under k�rning s� slutar tj�nsten att k�ras.
     *
     */
    public void startServing() {
	this.threadPool.submit(this.connectionServant.getTask());
	this.threadPool.submit(this.eventServant.getTask());
    }

    /**
     * Metod som bygger upp och returnerar en str�ng som representerar serverns
     * aktuella status.
     * 
     * @return str�ng som representerar serverns status
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
     * Hj�lpmetod som hanterar en ny klient.
     * 
     * @param connectionToNewClient
     *            anslutning till den klient som skall adderas
     */
    private final void addClient(Socket connectionToNewClient) {

	try {
	    /*
	     * F�rs�ker skapa en ny klient som lutar mot den nya anslutningen
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
	     * Den nya klienten placeras i datasamlingen �ver anslutna klienter.
	     */
	    this.clients.put(newClient, newClientsServant);

	    /*
	     * Klientens lyssnar-objekt placeras i tr�dpoolen f�r parallell
	     * exekvering.
	     */
	    this.threadPool.submit(newClientsServant.getTask());
	} catch (IOException e) {
	    /*
	     * Misslyckas initialisering utav anslutningen s� anropas l�mplig
	     * delegerad metod.
	     */
	    this.eventListener.onConnectionInitializationError(e.getMessage());
	}

    }

    /**
     * Hj�lpmetod f�r att terminera en klient. Sker ett problem vid st�ngning av
     * anslutningen s� finns inte mycket att g�ra �t detta, utan klienten tas
     * bort fr�n datasamlingen.
     * 
     * @param client
     *            klienten som skall tas bort
     * 
     */
    private void removeClient(Client client) {
	/*
	 * Klientens tj�nare h�mtas fr�n datasamligen f�r att sedan stoppas.
	 */
	ClientServant clientsServant = this.clients.get(client);
	clientsServant.stopServing();

	try {
	    /*
	     * Klientens tillh�rande anslutningar stoppas och termineras, genom
	     * att anropa dess l�mpliga metod.
	     */
	    client.kill();
	} catch (IOException e) {
	    /*
	     * Lyckas inte klienten terminera dess l�sare/skrivare s� finns inte
	     * mycket annat att g�ra �t detta. Vid det h�r laget har �nd�
	     * klienten tagits bort fr�n datasamlingen �ver klienter, s� den b�r
	     * st�das bort av skr�phanteraren inom rimlig tid. L�mplig delegerad
	     * metod anropas.
	     */
	    this.eventListener.onClientRemovalError(client.toString());
	} finally {
	    /*
	     * Slutligen s� tas clienten och dess associerade tj�nare bort fr�n
	     * datasamligen.
	     */
	    this.clients.remove(client);
	}
    }

    /**
     * Det till denna klass tillh�rande gr�nssnitt som beskriver de delegerade
     * metoder som skall deklareras externt, beroende p� hur det objekt som
     * anv�nder sig utav denna klass vill ha det.
     *
     * @author Atilla �zkan | 930304-4474 | atoz0393
     * @version 1.0
     */
    public interface ServiceListener extends EventListener {
	/**
	 * Syftet av denna metod �r att utf�ra l�mplig operation n�r tj�nsten
	 * st�tt p� ett problem vid terminering utav en befintlig klient.
	 * 
	 * @param author
	 *            en str�ng som representerar den klient som orsakat
	 *            problemet
	 */
	public void onClientRemovalError(String author);

	/**
	 * Syftet av denna metod �r att indikera att det skett ett fel vid
	 * initialisering utav ett anslutningsf�rs�k fr�n en klient.
	 *
	 * @param errorMessage
	 *            ett felmeddelande som skickas vidare till det objekt som
	 *            deklarerat metoden
	 */
	public void onConnectionInitializationError(String errorMessage);

	/**
	 * Syftet av denna metod �r att indikera att anslutningen till en klient
	 * har brutits.
	 */
	public void onConnectionLost();

	/**
	 * Syftet av denna metod �r att utf�ra l�mplig operation n�r ett
	 * meddelande har mottagits utav ett tj�nar-objekt som lyssnar p� nya
	 * inkommande meddelanden fr�n en klient.
	 *
	 * @param author
	 *            en str�ng som representerar den klient som skickat
	 *            meddelandet
	 * @param message
	 *            meddelandet som mottagits
	 */
	public void onMessageRecieved(String author, String message);

	/**
	 * Syftet av denna metod �r att indikera att en ny klient har anslutit
	 * till servern.
	 */
	public void onNewConnection();
    }
}
