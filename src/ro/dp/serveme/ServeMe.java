package ro.dp.serveme;

import java.net.BindException;

import org.apache.log4j.Logger;

import ro.dp.serveme.core.Handler;
import ro.dp.serveme.core.Connector;
import ro.dp.serveme.core.Server;
import ro.dp.serveme.core.impl.HttpRequestHandler;
import ro.dp.serveme.core.impl.ConnectorImpl;
import ro.dp.serveme.core.impl.ServerImpl;

/**
 * The main class of the ServeMe(tm) web server.<br>
 * The server's architecture is inspired by a series of articles on
 * <code>http://www.dailyjavatips.com/</code>, which in turn are based on the
 * architecture of the Jetty server.<br>
 * 
 * @author Daniel Platon (dplaton@gmail.com)
 * 
 */
public class ServeMe {

	private static Logger log = Logger.getLogger(ServeMe.class);

	public static void main(String[] args) {
		// some defaults
		int port = 80;
		String host = "localhost";
		String docroot = "wwwroot";
		// parsing the arguments to determine the host and the port to which
		// this server is bound to
		// if no arguments were supplied then the defaults apply.
		if (args.length > 0 && args[0].indexOf(':') != -1) {
			String[] hp = args[0].split(":");
			host = hp[0];
			try {
				port = Integer.valueOf(hp[1]);
			} catch (NumberFormatException e) {
				log.error(e.getMessage());
				log.debug("Using default port number (" + port + ")");
			}
			if (args.length > 1) {
				docroot = args[1];
			}
		} else {
			log.warn("No arguments set, using defaults (Host: " + host + "; Port: " + port + "; Document root: " + docroot + ").");
		}
		log.info("ServeMe v1.0 starting...");
		Server theServer = new ServerImpl();
		try {
			log.debug("Creating connector...");
			// creating the connector
			Connector connector = new ConnectorImpl(port, host);
			theServer.addConnector(connector);
			log.debug("Creating the request handler");

			// creating the request handler
			Handler httpHandler = new HttpRequestHandler(docroot);
			theServer.setHandler(httpHandler);

			// starting server
			theServer.start();
		} catch (BindException e) {
			log.error("Error opening a connection on port " + port + ". Maybe some other process is using it?");
			log.debug(e.getMessage(), e);
		} catch (Exception e) {
			log.error("An error has occured. See the log file for details.");
			log.debug(e.getMessage(), e);
		}
	}
}
