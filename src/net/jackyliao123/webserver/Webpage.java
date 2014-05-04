package net.jackyliao123.webserver;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

public abstract class Webpage{
	public static ArrayList<Webpage> pages = new ArrayList<Webpage>();
	public Webpage(){
		pages.add(this);
	}
	public abstract String getPageName();
	public abstract void writePageContent(ResponseParams param, String request, OutputStream o, InetAddress address);
	
	protected String getContentType(String req){
		return "text/html";
	}
	public ResponseParams getHeader(String req, String[] reqParams){
		return new ResponseParams(getPageHeader(getCode(req), getContentType(req), getContentLength(req)), new Object[]{});
	}
	public ResponseParams getHeader(String req, String[] reqParams, String method, String postData, InetAddress address){
		return getHeader(req, reqParams);
	}
	protected String getCode(String req){
		return "200 OK";
	}
	protected long getContentLength(String req){
		return -1;
	}
	public static ArrayList<String> getPageHeader(String code, String contentType, long contentLength){
		ArrayList<String> list = new ArrayList<String>();
		list.add("HTTP/1.1 " + code);
		list.add("Date: " + new Date().toString());
		list.add("Content-Type: " + contentType);
		if(contentLength != -1)
			list.add("Content-Length: " + contentLength);
		list.add("Last-modified: " + new Date(System.currentTimeMillis()).toString());
		return list;
	}
	public static String getPageCode(String title, String cssPath, String head, String body, InetAddress address){
		if(cssPath != null){
			head = "<link href=\"" + cssPath + "\" type=\"text/css\" rel=\"stylesheet\">" + head;
		}
		else{
			head = "<link href=\"/css/home.css\" type=\"text/css\" rel=\"stylesheet\">" + head;
		}
		String loginInfo = "";
		User user = WebServer.instance.manager.getUserFromAddress(address);
		if(user != null)
			loginInfo = "    <div id=\"header\"><div align=\"right\" id=\"spacing\">Logged in as: " + user.username + " <div id=\"spacer\"></div><a href=\"/login/account\">Account</a><div id=\"spacer\"></div><a href=\"/login/logout/\">Logout</a></div></div>\r\n";
		else
			loginInfo = "    <div id=\"header\"><div align=\"right\" id=\"spacing\"><a href=\"/login\">Login</a><div id=\"spacer\"></div><a href=\"/login/register\">Register</a></div></div>\r\n";
		return  "<html>\r\n" +
				"  <head>\r\n" +
				"    <title>" + title + "</title>\r\n" +
						head +
				"  </head>\r\n" +
				"  <body>\r\n" +
						loginInfo + 
						body +
				"  </body>\r\n" +
				"</html>\r\n";
	}
}