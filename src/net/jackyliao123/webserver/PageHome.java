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
			o.write(getPageCode("Home", "/css/home.css", "", "<center><a href=\"/file/\"><font size=\"6\">File System</font></a></center>", address).getBytes());
		}
		catch (IOException e){
		}
	}
}