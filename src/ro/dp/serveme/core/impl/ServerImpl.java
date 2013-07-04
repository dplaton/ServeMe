package ro.dp.serveme.core.impl;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ro.dp.serveme.core.Handler;
import ro.dp.serveme.core.Connector;
import ro.dp.serveme.core.Server;

public class ServerImpl implements Server {
	private static Logger log = Logger.getLogger(ServerImpl.class);

	/**
	 * The handler used by this server to handle requests.
	 */
	private Handler handler;

	/**
	 * The list of connectors associated with this server
	 */
	private List<Connector> connectors = new ArrayList<Connector>();

	/**
	 * The thread pool of the server.<br>
	 * This is responsible for the dispatching of {@link Listener} and
	 * {@link Connection} jobs.
	 */
	private ThreadPoolExecutor threadPool = null;

	/**
	 * The initial thread pool size.<br>
	 * Default value is 5 threads.
	 */
	private static int CORE_POOL_SIZE = 10;

	/**
	 * The maximum pool size. <br>
	 * This is the maximum number of threads that this pool will hold. Any
	 * thread that go beyond this value will be rejected.<br>
	 * Default value is 50 threads.
	 */
	private static int MAXIMUM_POOL_SIZE = 50;

	/**
	 * The amount of time (in seconds) that a thread will stay idle before it is
	 * terminated by the thread pool.
	 */
	private static int KEEP_ALIVE_TYME = 60;

	/**
	 * Constructs a {@link ServerImpl} object. This method also initializes the thread pool.
	 */
	public ServerImpl() {
		
		threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
				KEEP_ALIVE_TYME, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE));
	}

	public List<Connector> getListeners() {
		return connectors;
	}

	public void setListeners(List<Connector> listeners) {
		this.connectors = listeners;
	}

	public void start() throws Exception {

		log.debug("Starting connectors...");
		for (Connector l : connectors) {
			l.start();
		}
		log.info("ServeMe v1.0 active and listening for incoming connections");
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
	}

	public void stop() throws Exception {
		log.info("Stopping ServeMe web server");
		log.debug("Stopping connectors...");
		for (Connector c : connectors) {
			c.stop();
		}
		log.info("Shutting down the thread pool...");
		threadPool.shutdown();
		log.info("Server stopped");
	}

	public void handle(Socket connection) throws Exception {
		if (handler != null) {
			handler.handle(connection);
		} else {
			log.fatal("Ho handlers associated with this server.");
		}
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void addConnector(Connector c) {
		connectors.add(c);
		c.setServer(this);
	}

	public void dispatch(Runnable job) {
		try {
			threadPool.execute(job);
		} catch (RejectedExecutionException e) {
			log.fatal("Cannot dispatch job because the thread pool queue is full");
		}
	}

	private class ShutdownHook extends Thread {
		private Server serverProcess;

		ShutdownHook(Server theServer) {
			this.serverProcess = theServer;
		}

		public void run() {
			try {
				log.info("Stop the server process...");
				serverProcess.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
