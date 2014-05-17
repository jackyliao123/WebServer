import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer extends Thread{
	protected ServerSocket serverSocket;
	protected boolean running;
	protected int maxThread;
	public static boolean defaultKeepAlive;
	protected WebServer(){
	}
	public WebServer(int port){
		try{
			serverSocket = new ServerSocket(port);
			running = true;
			System.out.println("HTTP server started on port " + port);
		}
		catch(Exception e){
			System.err.println("HTTP server failed to start");
			e.printStackTrace();
		}
	}
	public void run() {
		ThreadCount tc = new ThreadCount();
		while (running) {
			if(tc.threadCount < maxThread || maxThread == -1){
				try {
					Socket socket = serverSocket.accept();
					RequestThread requestThread = new RequestThread(socket, tc);
					requestThread.start();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				try{
					Thread.sleep(100L);
				}
				catch(Exception e){
				}
			}
		}
	}
	public static void main(String[] args) {
		ConfigLoader loader = new ConfigLoader(new File("server.cfg"));
		Webpage.file = new File(loader.loadString("server-directory", "website"));
		RequestThread.log = new File(loader.loadString("server-log", "connections.log"));
		Webpage.fileBufferSize = loader.loadInt("file-buffer-size", 4096);
		defaultKeepAlive = loader.loadString("default-connection", "keep-alive").equalsIgnoreCase("keep-alive");
		if(loader.loadBoolean("http", true)){
			WebServer server = new WebServer(loader.loadInt("http-port", 80));
			server.maxThread = loader.loadInt("http-max-thread", -1);
			server.start();
		}
		if(loader.loadBoolean("https", false)){
			WebServerSSL ssl = new WebServerSSL(loader.loadInt("https-port", 443), new File(loader.loadString("https-keystore", "")), loader.loadString("https-keystore-pass", ""));
			ssl.maxThread = loader.loadInt("https-max-thread", -1);
			ssl.start();
		}
	}
}