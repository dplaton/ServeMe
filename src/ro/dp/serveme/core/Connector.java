package ro.dp.serveme.core;

/**
 * A basic connector interface. <br>
 * A connector is bound to a specific port (defaults to 80) and spawns one or
 * more {@link Listener} processes that listen for incoming connections. When an
 * incoming request is received the {@link Listener} instructs the associated
 * {@link Server} instance to dispatch a {@link Connection} that will handle the
 * response.
 * 
 * @author Daniel Platon (dplaton@gmail.com)
 * 
 */
public interface Connector {

	/**
	 * Starts this connector.<br>
	 * Starting the connector also starts the listeners associated with the
	 * connector.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception;

	/**
	 * Sets the server that the connector belongs to.
	 * 
	 * @param server
	 */
	public void setServer(Server server);

	/**
	 * Stops this connector and closes all the opened connections.
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;
}
