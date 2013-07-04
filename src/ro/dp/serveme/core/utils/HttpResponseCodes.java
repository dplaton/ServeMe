package ro.dp.serveme.core.utils;

import java.io.File;

/**
 * Enum class that holds the definition of the HTTP response codes used by this web server.
 * @author daniel.platon
 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
public enum HttpResponseCodes {
	HTTP_OK(200,"OK"),
	HTTP_NOT_FOUND(404,"Not found"),
	HTTP_SERVER_ERROR(500,"Internal server error"),
	HTTP_NOT_IMPLEMENTED(501,"Not implemented");
	
	private int code;
	private String description;
	
	private HttpResponseCodes(int code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.getCode()) + " " + this.getDescription();
	}
	
	public String getResponseFileName() {
		return this.getCode()+".html";
	}
}