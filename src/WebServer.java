import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class WebServer {

	// ServerSocket to listen for connections
	private ServerSocket serverSocket;
	// port number to start SocketServer on
	private int portNumber;
	private Path webRoot;

	public WebServer(int portNumber, String webRoot) {
		this.portNumber = portNumber;
		this.webRoot = Paths.get(webRoot);
	}

	// start ServerSocket and listen for connections
	public void runServer() {
		System.out
				.println("Webserver starting on port: " + this.portNumber + " web root is: " + this.webRoot.toString());

		try {
			this.serverSocket = new ServerSocket(this.portNumber);
			while (true) {
				try {
					Socket client = serverSocket.accept();
					// create handler thread to deal with request
					Thread responseHandler = new RequestHandler(client, this.webRoot);
					responseHandler.start();

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Could not connect to client Socket.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not start ServerSocket.");
		} finally {
			try {
				this.serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class RequestHandler extends Thread {

		// Socket connect to receive request from and send data to
		private Socket socket;
		private Path webRoot;

		public RequestHandler(Socket socket, Path webRoot) {
			System.out.println("Found connection, created RequestHandler.");
			this.socket = socket;
			this.webRoot = webRoot;
		}

		@Override
		public void run() {
			System.out.println("handling request for " + this.socket);
			StringTokenizer token;
			String method;
			String fileRequest = null;
			PrintWriter out = null;
			BufferedReader in = null;
			// parse request
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				String input;

				// get binary data for client
				BufferedOutputStream data = new BufferedOutputStream(this.socket.getOutputStream());
				out.flush();
				input = in.readLine();
				// find file requested
				token = new StringTokenizer(input);
				method = token.nextToken();
				fileRequest = token.nextToken();
				if(fileRequest.equals("/")) {
					fileRequest = "/index.html";
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// create response
			HTTPResponse response = new HTTPResponse();

			File requested = Paths.get(this.webRoot.toString() + fileRequest).toFile();
			try {
				FileReader file = new FileReader(requested);
				BufferedReader read = new BufferedReader(file);
				StringBuilder content = new StringBuilder();
				if (requested.exists()) {
					// return 200 if file found
					// populate response content with file contents
				    String str;
					while((str = read.readLine()) != null) {
						content.append(str);
					}
					response.setContent(content.toString());
					response.code = 200;
				} else {
					// file doesn't exist, return 404
					// populate response content with 404 page
					response.setContent("<html><body><h1>Not found 404</h1></body></html>");
					response.code = 404;
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			// populate response headers
			response.setHeader("Server", "Java");
			response.setHeader("Date", new Date(System.currentTimeMillis()).toString());
			response.setHeader("Content-Type", "text/html");
			response.setHeader("Content-Length", Integer.toString(response.content.length()));

			// send response
			out.write(response.toString());
			// close connection
			try {
				out.flush();
				in.close();
				out.close();
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class HTTPResponse {
		// the map of HTTP headers to insert into the response
		private HashMap<String, String> headers;
		// the HTTP response code
		private int code;
		// the response content
		private String content;

		public HTTPResponse() {
			this.headers = new HashMap<String, String>();
		}

		public void setHeader(String key, String value) {
			this.headers.put(key, value);
		}

		public void setContent(String content) {
			this.content = content;
		}

		@Override
		public String toString() {
			String status;
			switch(this.code) {
				case 200:
					status = "OK";
					break;
				case 404:
					status = "Not found";
					break;
				default:
					status = "Forbidden";
			}
			StringBuilder res = new StringBuilder();
			res.append("HTTP/1.1 " + this.code + " " + status);
			// TODO convert response information to HTTP response string
			for(String key: this.headers.keySet()) {
				res.append(key+": " +this.headers.get(key) + "\n");
			}
			res.append("\n");
			res.append(this.content);
			return res.toString();
		}
	}

}
