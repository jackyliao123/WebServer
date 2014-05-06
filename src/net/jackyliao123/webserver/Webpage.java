package net.jackyliao123.webserver;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public abstract class Webpage{
	public static ArrayList<Webpage> pages = new ArrayList<Webpage>();
	public Webpage(){
		pages.add(this);
	}
	public abstract String getPageName();
	public abstract void writePageContent(ResponseParams param, String request, OutputStream o, Cookie cookie);
	
	protected String getContentType(String req){
		return "text/html";
	}
	public ResponseParams getHeader(String req, String[] reqParams, Cookie cookie){
		return new ResponseParams(getPageHeader(getCode(req), getContentType(req), getContentLength(req), cookie), new Object[]{});
	}
	public ResponseParams getHeader(String req, String[] reqParams, String method, String postData, Cookie cookie){
		return getHeader(req, reqParams, cookie);
	}
	protected String getCode(String req){
		return "200 OK";
	}
	protected long getContentLength(String req){
		return -1;
	}
	public static ArrayList<String> getPageHeader(String code, String contentType, long contentLength, Cookie cookie){
		ArrayList<String> list = new ArrayList<String>();
		list.add("HTTP/1.1 " + code);
		list.add("Date: " + new Date().toString());
		list.add("Content-Type: " + contentType);
		if(contentLength != -1)
			list.add("Content-Length: " + contentLength);
		if(cookie == null) {
			cookie = new Cookie("session", String.valueOf(new Random().nextLong()), 604800000);
			StringBuilder builder = new StringBuilder();
			builder.append("Set-Cookie: ");
			Date date = new Date(cookie.validFor + cookie.start);
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c.setTime(date);
			String dateFormat = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss z").format(date);
			builder.append(cookie.name + "=" + cookie.value + "; Expires=" + dateFormat);
			list.add(builder.toString());
		}
		list.add("Last-modified: " + new Date(System.currentTimeMillis()).toString());
		return list;
	}
	public static String getPageCode(String title, String cssPath, String head, String body, Cookie cookie){
		if(cssPath != null){
			head = "<link href=\"" + cssPath + "\" type=\"text/css\" rel=\"stylesheet\">" + head;
		}
		else{
			head = "<link href=\"/css/home.css\" type=\"text/css\" rel=\"stylesheet\">" + head;
		}
		String loginInfo = "";
		User user = WebServer.instance.manager.getUserFromCookie(cookie);
		if(user != null)
			loginInfo = "<div id=\"header\"><div id=\"spacing\" style=\"display: inline-block; float: left;\"><a href=\"/\">Home</a></div><div id=\"spacing\" style=\"display: inline-block; float: right\">Logged in as: " + user.username + " <div id=\"spacer\"></div><a href=\"/login/account\">Account</a><div id=\"spacer\"></div><a href=\"/login/logout/\">Logout</a></div></div>\r\n";
		else
			loginInfo = "<div id=\"header\"><div id=\"spacing\" style=\"display: inline-block; float: left;\"><a href=\"/\">Home</a></div><div id=\"spacing\" style=\"display: inline-block; float: right\"><a href=\"/login\">Login</a><div id=\"spacer\"></div><a href=\"/login/register\">Register</a></div></div>\r\n";
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
	public static String getPageContentCode(String body) {
		return "<div id=\"indent\">" + body + "</div>";
	}
}