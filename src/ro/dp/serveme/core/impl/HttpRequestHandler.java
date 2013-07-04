package ro.dp.serveme.core.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ro.dp.serveme.core.Handler;
import ro.dp.serveme.core.utils.HttpResponseCodes;

/**
 * Concrete implementation of a request handler. <br>
 * This handler is responsible for serving the incoming requests.
 * 
 * @author daniel.platon
 * 
 */
public class HttpRequestHandler implements Handler {

	private static Logger log = Logger.getLogger(HttpRequestHandler.class);

	/**
	 * The list of MIME types.
	 * 
	 * @see http://www.iana.org/assignments/media-types/index.html
	 */
	private static Map<String, String> MIME_TYPES = new HashMap<String, String>();
	static {
		MIME_TYPES.put("", "content/unknown");
		MIME_TYPES.put(".pdf", "application/pdf");
		MIME_TYPES.put(".html", "text/html");
		MIME_TYPES.put(".htm", "text/html");
		MIME_TYPES.put(".xml", "text/xml");
		MIME_TYPES.put(".txt", "text/plain");
		MIME_TYPES.put(".css", "text/css");
		MIME_TYPES.put(".png", "image/png");
		MIME_TYPES.put(".jpg", "image/jpeg");
		MIME_TYPES.put(".gif", "image/gif");
	}

	/**
	 * The document root. This parameter is configurable.
	 */
	private String documentRoot;

	/**
	 * The location of the error pages
	 */
	private static String ERROR_PAGES_LOCATION = "errpages" + File.separator;

	/**
	 * Constructs the request handler using the specified document root
	 * 
	 * @param root
	 */
	public HttpRequestHandler(String root) {
		this.documentRoot = root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.dp.serveme.core.Handler#handle(java.net.Socket)
	 */
	public void handle(Socket connection) throws Exception {
		handleGet(connection);
	}

	/**
	 * This method does the actual handling of the request.
	 * 
	 * @param connection
	 *            The incoming connection
	 * @throws IOException
	 */
	private void handleGet(Socket connection) throws Exception {
		InputStream in = null;
		PrintStream out = null;
		try {
			log.debug("Handling request from "
					+ connection.getRemoteSocketAddress());
			in = connection.getInputStream();
			out = new PrintStream(connection.getOutputStream());

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = reader.readLine();
			if (line == null) {
				return;
			}
			// we're not responding to requests that have a method other than
			// GET
			if (!line.startsWith("GET ")) {
				sendNotImplemented(out);
				return;
			}

			String uri = line.substring(line.indexOf(' ') + 1,
					line.indexOf("HTTP") - 1);

			String filename = "";
			filename = uri.replace("/", File.separator);
			// if the filename is just a \ then we are at the document root 
			// so we return the default page
			if (filename.equals(File.separator)) {
				filename="index.html";
			}
			File theFile = new File(documentRoot + File.separator + filename);
			if (!theFile.exists()) {
				log.warn("Not found: " + theFile.getAbsolutePath());
				sendNotFound(out);
				return;
			}

			if (theFile.isDirectory()) {
				sendDirectoryListing(out, theFile);
			} else {
				sendOk(out, theFile);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (out != null) {
				sendInternalError(out);
			}
		} finally {

			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * Sends the directory listing for a folder
	 * 
	 * @param out
	 *            the {@link PrintStream} associated with the response output
	 *            stream
	 * @param folder
	 *            the {@link File} object represeting the directory to list
	 */
	private void sendDirectoryListing(PrintStream out, File folder) {
		StringBuilder listing = new StringBuilder(
				"<html><head><title>Directory listing for ").append(
				folder.getName()).append("</title></head><body>");
		listing.append("<a href=\"..\"").append("\">..</a><br/>");
		for (String fileName : folder.list()) {

			File file = new File(folder, fileName);

			if (file.isDirectory()) {
				listing.append("<a href=\"").append(fileName).append("/\"")
						.append(">").append(fileName).append("</a>");
			} else {
				listing.append("<a href=\"").append(fileName).append("\"")
						.append(">").append(fileName).append("</a>");
			}
			listing.append("<br/>");
		}
		listing.append("</body></html>");
		printHeader(out, HttpResponseCodes.HTTP_OK, "text/html",
				listing.length());
		out.print(listing.toString());
	}

	/**
	 * Sents the "501 Not Implemented" response to the output stream. This
	 * response code is sent if the request has another method than "GET"
	 * 
	 * @param out
	 *            the {@link PrintStream} associated with the response output
	 *            stream
	 * @throws IOException
	 */
	private void sendNotImplemented(PrintStream out) throws IOException {
		File errFile = new File(ERROR_PAGES_LOCATION
				+ HttpResponseCodes.HTTP_NOT_IMPLEMENTED.getResponseFileName());
		printHeader(out, HttpResponseCodes.HTTP_NOT_IMPLEMENTED, "text/html",
				errFile.length());
		sendStream(out, errFile);
	}

	/**
	 * Sents the "500 Internal Server Error" response to the output stream. This
	 * response code is sent if the request has another method than "GET"
	 * 
	 * @param out
	 *            the {@link PrintStream} associated with the response output
	 *            stream
	 * @throws IOException
	 */
	private void sendInternalError(PrintStream out) throws IOException {
		File errFile = new File(ERROR_PAGES_LOCATION,
				HttpResponseCodes.HTTP_SERVER_ERROR.getResponseFileName());
		printHeader(out, HttpResponseCodes.HTTP_SERVER_ERROR, "text/html",
				errFile.length());
		sendStream(out, errFile);
	}

	/**
	 * Sends the "404 Not Found" response to the output stream
	 * 
	 * @param out
	 *            the {@link PrintStream} associated with the response output
	 *            stream
	 * @throws IOException
	 */
	private void sendNotFound(PrintStream out) throws IOException {
		File errFile = new File(ERROR_PAGES_LOCATION + "404.html");
		printHeader(out, HttpResponseCodes.HTTP_NOT_FOUND, "text/html",
				errFile.length());
		sendStream(out, errFile);
	}

	/**
	 * Sends the response header to the socket's output stream.
	 * 
	 * @param out
	 *            the {@link PrintStream} associated with the response output
	 *            stream
	 * @param responseCode
	 *            a {@link HttpResponseCodes} object representing the response
	 *            code
	 * @param contentType
	 *            the content type of the response
	 */
	private void printHeader(PrintStream out, HttpResponseCodes responseCode,
			String contentType, long contentLength) {
		StringBuilder response = new StringBuilder("HTTP/1.1 ");
		response.append(responseCode).append("\r\n");
		response.append("Content-Type:").append(contentType)
				.append("; Charset=UTF-8\r\n");
		response.append("Content-Length: ").append(contentLength)
				.append("\r\n");
		response.append("\r\n");
		out.print(response.toString());

	}

	/**
	 * Sends the requested html file along with the response header
	 * 
	 * @param out
	 *            the {@link PrintStream} associated with the response output
	 *            stream
	 * @param responseFile
	 *            the requested html file
	 * @throws IOException
	 */
	private void sendOk(PrintStream out, File responseFile) throws IOException {
		String fileExtension = responseFile.getName().substring(
				responseFile.getName().lastIndexOf('.'),
				responseFile.getName().length());
		printHeader(
				out,
				HttpResponseCodes.HTTP_OK,
				MIME_TYPES.get(fileExtension) != null ? MIME_TYPES
						.get(fileExtension) : "text/plain",
				responseFile.length());
		sendStream(out, responseFile);
	}

	/**
	 * Sends a response file to the output stream.
	 * 
	 * @param out
	 *            the {@link PrintStream} to which the file is sent
	 * @param responseFile
	 *            the response file
	 * @throws IOException
	 *             if something goes wrong
	 */
	private void sendStream(PrintStream out, File responseFile)
			throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(responseFile);
			byte[] buffer = new byte[2048];
			int c;
			while ((c = fis.read(buffer)) != -1) {
				out.write(buffer, 0, c);
			}
		} finally {
			fis.close();
		}
	}
}
