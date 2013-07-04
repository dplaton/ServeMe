package ro.dp.serveme.core;

import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * The actual server process, which takes care of starting / stopping the connectors and also handles the incoming HTTP requests.<br>
 * The server has an associated connection pool which is responsible for managing {@link Listener} and {@link Connection} tasks.
 * 
 * @author Daniel Platon (dplaton@gmail.com)
 */
public interface Server {

	/**
	 * Starts the server. <br>
	 * Starting the server actually means starting the connectors associated with it.
	 * @throws Exception
	 */
	public void start() throws Exception;
	
	/**
	 * Stops the server pocess by stopping all the connectors and shutting down the thread pool, causing all the active threads (if any) to be terminated.
	 * s
	 * @throws Exception
	 */
	public void stop() throws Exception;
	
	/**
	 *  Handles an incoming connection. This method is called by the {@link Connection} task.
	 *  @param connection - the {@link Socket} object which represents the actual connection  
	 */
	public void handle(Socket connection) throws Exception;
	
	/**
	 * Adds a connector to this server's list of connectorss.
	 * @param connector - the {@link Connector} being added
	 */
	public void addConnector(Connector connectors);

	/**
	 * Sets the handler that this server will use to handle HTTP requests.
	 * @param httpHandler - the {@link Handler} being set
	 */
	public void setHandler(Handler httpHandler);
	
	/**
	 * Submits a job to the server's associated {@link ThreadPoolExecutor}. 
	 * @param job - the job to be ran. This can be either a {@link Listener} or a {@link Connection} task.
	 */
	public void dispatch(Runnable job);
}
