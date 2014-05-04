import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer{
	private ServerSocket serverSocket;
	public boolean running = true;
	public UserManager manager;
	public static WebServer instance;
	public WebServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		manager = new UserManager(new File("users.dat"));
		instance = this;
	}
	public void start() {
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				RequestThread requestThread = new RequestThread(socket);
				requestThread.start();
				manager.updateUsers();
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	public static void main(String[] args) {
		try {
			WebServer server = new WebServer(80);
			new PageCSS();
			new PageFile();
			new PageIcon();
			new PageLogin();
			new PageHome();
			server.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}