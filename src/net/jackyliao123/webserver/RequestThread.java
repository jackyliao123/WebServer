package net.jackyliao123.webserver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class RequestThread extends Thread {
	public static final String encoding = "UTF-8";
	private Socket socket;
	private BufferedReader input;
	private BufferedWriter output;
	public RequestThread(Socket socket) {
		this.socket = socket;
	}
	public void run(){
		try {
			socket.setSoTimeout(30000);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			final String request = input.readLine();
			ArrayList<String> lines = new ArrayList<String>();
			while(true){
				String s = input.readLine();
				if(s == null)
					return;
				if(s.length() == 0)
					break;
				lines.add(s);
			}
			if (request == null || !(request.startsWith("GET ") || request.startsWith("HEAD ") || request.startsWith("POST ")) || !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))){
				Webpage.getPageHeader("500 Internal Server Error", "text/html", -1, null);
				output.write("Invalid Method.");
				output.flush();
				output.close();
				return;
			}

			final ArrayList<String> threadLines = lines;
			new Thread(){
	            public void run() {
	    			try {
		    			BufferedWriter bwriter = new BufferedWriter(new FileWriter("connections.log", true));
		    			bwriter.write(new Date().toString() + ": [" + socket.getInetAddress().getHostAddress() + "]" + "(" + socket.getInetAddress().getCanonicalHostName() + ")");
						bwriter.newLine();
		    			bwriter.write(request);
		    			bwriter.newLine();
		    			bwriter.write(threadLines.toString());
		    			bwriter.newLine();
		    			bwriter.newLine();
		    			bwriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	            };
	        }.start();

			String[] requestLines = new String[lines.size()];
			String postData = null;
			Cookie cookie = null;
			for(int i = 0; i < requestLines.length; i ++){
				requestLines[i] = lines.get(i);
				if(request.startsWith("POST")){
					if(requestLines[i].startsWith("Content-Length: ")){
						int dataLength = Integer.parseInt(requestLines[i].replace("Content-Length: ", ""));
						char[] data = new char[dataLength];
						int bytesRead = 0;
						while(bytesRead < dataLength){
							bytesRead += input.read(data, bytesRead, dataLength - bytesRead);
						}
						postData = new String(data);
					}
				}
				if (requestLines[i].startsWith("Cookie: ")){
					String[] cookies = requestLines[i].replace("Cookie: ", "").split("; ");
					for (String cookieLine : cookies) {
						String[] values = cookieLine.replaceAll(" ", "").split("=");
						if (values[0].equals("session"))
							cookie = new Cookie(values[0], values[1], 0);
					}
				}
			}
			boolean headOnly = request.startsWith("HEAD ");
			String path = URLDecoder.decode(request.substring(0, request.length() - 9).split(" ", 2)[1], encoding);
			setName(path);
			for(int i = 0; i < Webpage.pages.size(); i++){
				if(path.startsWith(Webpage.pages.get(i).getPageName())){
					String req = path.substring(Webpage.pages.get(i).getPageName().length());
					ResponseParams params = Webpage.pages.get(i).getHeader(req, requestLines, request.split(" ")[0], postData, cookie);
					for(int j = 0; j < params.getResponse().size(); j ++){
						output.write(params.getResponse().get(j) + "\r\n");
					}
					output.write("\r\n");
					output.flush();
					if(!headOnly){
						if(params.noContent())
							output.write(params.getError());
						else{
							if (cookie == null)
								cookie = new Cookie("session", String.valueOf(new Random().nextLong()), 604800000);
							Webpage.pages.get(i).writePageContent(params, req, socket.getOutputStream(), cookie);
						}
					}
					break;
				}
			}
			output.flush();
			output.close();
		}
		catch (IOException e){
		}
	}
}