ServeMe - the simple webserver, version 1.0
-------------------------------------------

Introduction:
--------------
ServeMe is a java-based webserver which takes advantage of the benefits of thread pooling. The architecture is inspired by a series of articles on http://www.dailyjavatips.com, which in turn are based on the Jetty Webserver architecture. 

The contents of this archive:
-----------------------------
	ServeMe
	|
	|_bin - the jar containing all the bytecode and the log4j.properties file
	|_lib - the location of the dependencies
	|_src - the source code
	|_errpages - the standard error pages which the webserver returns if an error has occured
	|_wwwroot - the document root; this is the root folder from which the pages are served
	|_serveme.bat - the executable file
	|_build.xml - an ANT buildfile, if you want to rebuild ServeMe from the source code
	
	
Prerequisites:
--------------
ServeMe requires Java 1.5 or greater. The JAVA_HOME environment variable must be present and must point to a valid JDK/JRE installation.
The only dependency is log4j, which is provided.

Starting ServeMe:
-----------------
The command to start ServeMe is
	serveme.bat [bind_address:port [document root]]
where
	bind_address:port is the IP address and the port on which ServeMe will listen for incoming connections
	document root is the root folder from which the pages are served.
	
E.g.	serveme.bat 192.168.0.5:80 d:\\wwwroot

	
If you don't supply any arguments ServeMe will run with the defaults, like 
		serveme.bat localhost:80 wwwroot
		
Shutting down ServeMe:
----------------------
In order to shut down ServeMe just press Ctrl+C on the console.

Limitations
-------------------------
 * ServeMe serves only GET requests. All other request methods will be answered with the "501 Not Implemented" HTTP code.