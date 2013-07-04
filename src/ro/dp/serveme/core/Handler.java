package ro.dp.serveme.core;

import java.net.Socket;

/**
 * Interface for a generic handler associated with the server.<br>
 * A handler is used by the server to generate and send the response to a request.
 * 
 * @author daniel.platon
 */
public interface Handler {
	/**
	 * Handles the response for the socket connection.
	 * @param socket - the {@link Socket} object which represents the actual connection
	 * @throws Exception
	 */
	public void handle(Socket socket) throws Exception;
}
