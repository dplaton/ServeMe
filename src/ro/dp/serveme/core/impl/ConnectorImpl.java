package ro.dp.serveme.core.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ro.dp.serveme.core.Connector;
import ro.dp.serveme.core.Server;

/**
 * A concrete implementation of a server connector.<br>
 * A connector has a {@link Listener} that accepts incoming connections on the
 * connector's port. The listener triggers the dispatching of a
 * {@link Connection} process which is responsible for handling the actual
 * response.
 * 
 * @author Daniel Platon (dplaton@gmail.com)
 */
public class ConnectorImpl implements Connector {

	private static Logger log = Logger.getLogger(ConnectorImpl.class);
	/**
	 * The port that this connector is bound to. All the listeners associated
	 * with the connector will listen on this port.
	 */
	private int port;
	/**
	 * The hostname that this connector is bound to
	 */
	private String host;
	/**
	 * The reference to the server process that manages this connector.
	 */
	private Server server;

	/**
	 * The collection of listeners that this connector manages.
	 */
	private Set<Listener> listeners = new HashSet<Listener>();
	/**
	 * The connections opened by this connector.
	 */
	private Set<Connection> connections = new HashSet<Connection>();

	/**
	 * The server socket that this connector initializes.
	 */
	private ServerSocket serverSocket = null;

	/**
	 * Initial number of listener processes to dispatch.
	 */
	private int INIT_LISTENERS_COUNT = 3;

	/**
	 * Constructs a connector object using the specified port and hostname
	 * 
	 * @param p
	 *            the port that this connector is bound to
	 * @param host
	 *            the host that this connector is bound to
	 */
	public ConnectorImpl(int p, String host) {
		this.host = host;
		this.port = p;
	}

	/**
	 * A listener associated with this port. <br>
	 * The listener is responsible for accepting incoming connections and also
	 * instructs the server to dispatch {@link Connection} jobs
	 * 
	 * @author Daniel Platon (dplaton@gmail.com)
	 */
	private class Listener implements Runnable {

		Socket conn = null;

		Listener() {
			listeners.add(this);
		}

		public void run() {
			try {
				while (true) {
					Socket conn = serverSocket.accept();
					// we're telling the server to spawn a new connection thread
					server.dispatch(new Connection(conn));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void stop() throws IOException {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * A connection initialized by a {@link Listener}. <br>
	 * A connection is a job that handles the response to a request.
	 * 
	 * @author Daniel Platon (dplaton@gmail.com)
	 */
	private class Connection implements Runnable {

		private Socket socket;

		Connection(Socket socket) {
			this.socket = socket;
			connections.add(this);
		}

		public void run() {
			try {
				server.handle(socket);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.dp.serveme.core.Connector#start()
	 */
	public void start() throws Exception {
		log.info("Starting connector");
		log.debug("Initializing listening socket (hostname: " + host
				+ "; port: " + port + ")");
		serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));
		log.debug("Staring listeners");
		for (int idx = 0; idx < INIT_LISTENERS_COUNT; idx++) {
			server.dispatch(new Listener());
		}
		log.debug("Connector started.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.dp.serveme.core.Connector#stop()
	 */
	public void stop() throws Exception {
		log.debug("Stopping connector.");
		for (Listener l : listeners) {
			l.stop();
		}
		listeners.clear();
		log.debug("Connector stopped.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.dp.serveme.core.Connector#setServer(ro.dp.serveme.core.Server)
	 */
	public void setServer(Server server) {
		this.server = server;
	}

}
