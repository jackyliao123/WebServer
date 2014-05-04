package net.jackyliao123.webserver;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

public class PageHome extends Webpage{
	public String getPageName(){
		return "/";
	}
	public void writePageContent(ResponseParams param, String request, OutputStream o, InetAddress address){
		try {
			o.write(getPageCode("Home", "/css/home.css", "", 
					getPageContentCode(
					"<a href=\"/file/\">" +
					"<h1 style=\"text-align:center\">File System</h2>" +
					"</a>"
					), address).getBytes());
		}
		catch (IOException e){
		}
	}
}