import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RequestThread extends Thread {
	public static final String encoding = "UTF-8";
	public static File log;
	private Socket socket;
	private BufferedReader input;
	private OutputStream output;
	public ThreadCount tc;
	public boolean keepAliveConnection = WebServer.defaultKeepAlive;
	public RequestThread(Socket socket, ThreadCount tc) {
		this.socket = socket;
		this.tc = tc;
	}
	public void run(){
		++ tc.threadCount;
		try {
			socket.setSoTimeout(0);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = socket.getOutputStream();
			
			do{
				HashMap<String, String> requestHeader = new HashMap<String, String>();
				String request = input.readLine();
				while(true){
					String s = input.readLine();
					if(s == null){
						-- tc.threadCount;
						return;
					}
					if(s.length() == 0)
						break;
					if(s.contains(": ")){
						requestHeader.put(s.split(": ")[0], s.split(": ")[1]);
					}
				}
				if (request == null || !(request.startsWith("GET ") || request.startsWith("HEAD ") || request.startsWith("POST ")) || !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))){
					String message = "Invalid Method.";
					ArrayList<String> headers = Webpage.getPageHeader("500 Internal Server Error", "text/html", message.length());
					for(String header : headers){
						output.write((header + "\r\n").getBytes());
					}
					output.write("\r\n".getBytes());
					output.write(message.getBytes());
					continue;
				}
				setName(request);
				String path = request.split(" ", 2)[1];
				path = URLDecoder.decode(path.substring(0, path.length() - 9), "UTF-8");
				String requestMethod = request.split(" ", 2)[0];
				
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(log, true));
				bwriter.write(new Date().toString() + ": [" + socket.getInetAddress().getHostAddress() + "]" + "(" + socket.getInetAddress().getCanonicalHostName() + ") " + socket.getLocalPort());
				bwriter.newLine();
				bwriter.write(request);
				bwriter.newLine();
				bwriter.write(requestHeader.toString());
				bwriter.newLine();
				bwriter.newLine();
				bwriter.close();
				
				HashMap<String, String> postData = null;
				if(requestMethod.equals("POST") && requestHeader.containsKey("Content-Length")){
					int length = Integer.parseInt(requestHeader.get("Content-Length"));
					char[] data = new char[length];
					int bytesRead = 0;
					while(bytesRead < length){
						bytesRead += input.read(data, bytesRead, length - bytesRead);
					}
					postData = new HashMap<String, String>();
					String[] fields = URLDecoder.decode(new String(data), "UTF-8").split("&");
					for(String s : fields){
						if(s.split("=").length == 1)
							postData.put(s.split("=")[0], "");
						else
							postData.put(s.split("=")[0], s.split("=")[1]);
					}
				}
				int index = path.indexOf('?');
				if(index != -1 && postData == null){
					String data = path.substring(index + 1);
					postData = new HashMap<String, String>();
					String[] fields = URLDecoder.decode(new String(data), "UTF-8").split("&");
					for(String s : fields){
						if(s.split("=").length == 1)
							postData.put(s.split("=")[0], "");
						else
							postData.put(s.split("=")[0], s.split("=")[1]);
					}
					path = path.substring(0, index);
				}
				if(path.replace(" ", "").replace("\b", "").replace("\n", "").replace("\r", "").replace("\t", "").replace("/f", "").contains("..")){
					String message = "Hacker.";
					ArrayList<String> headers = Webpage.getPageHeader("403 Forbidden", "text/html", message.length());
					for(String header : headers){
						output.write((header + "\r\n").getBytes());
					}
					output.write("\r\n".getBytes());
					output.write(message.getBytes());
					continue;
				}
				if(!path.endsWith("/")){
					path += "/";
				}
				if(!path.contains(".")){
					path += ".jht";
				}
				Webpage.writePageContent(this, requestMethod, requestHeader, postData, path, output, socket.getInetAddress());
			}
			while(keepAliveConnection);
			output.close();
		}
		catch (IOException e){
		}
		-- tc.threadCount;
	}
}