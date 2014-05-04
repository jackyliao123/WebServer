package net.jackyliao123.webserver;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URLDecoder;

public class PageLogin extends Webpage{
	public String getPageName(){
		return "/login";
	}
	public ResponseParams getHeader(String req, String[] reqParams, String method, String postData, InetAddress address){
		return new ResponseParams(getPageHeader(getCode(req), getContentType(req), getContentLength(req)), new Object[]{postData != null, postData});
	}
	public void writePageContent(ResponseParams param, String request, OutputStream o, InetAddress address){
		UserManager manager = WebServer.instance.manager;
		try {
			if(request.startsWith("/logout")){
				manager.userLogout(manager.getUserFromAddress(address));
			}
			if((Boolean)param.getParam()[0]){
				String userpass = URLDecoder.decode((String)param.getParam()[1], "UTF-8");
				String[] userArray = userpass.split("&");
				if(request.startsWith("/register")){
					boolean successful = true;
					String failText = "";
					String user = userArray[0].substring(9);
					String email = userArray[1].substring(6);
					String pass = userArray[2].substring(9);
					String confirm = userArray[3].substring(8);
					if(manager.getUserFromName(user) != null){
						successful = false;
						failText = "User already exists";
					}
					else if(user.length() < 6){
						successful = false;
						failText = "Usernames must be at least 6 characters in length";
					}
					else if(manager.getUserFromEmail(email) != null){
						successful = false;
						failText = "Email address already used";
					}
					else if(!pass.equals(confirm)){
						successful = false;
						failText = "Passwords does not match";
					}
					else if(pass.length() < 6){
						successful = false;
						failText = "Passwords must be at least 6 characters in length";
					}
					
					if(successful){
						manager.registerUser(user, email, pass);
						manager.loginUser(user, pass, address);
						o.write(getPageCode("Registeration Successful", null, "<meta http-equiv=\"refresh\" content=\"5; url=/\" />\r\n",
								"<center>\r\n" +
								"<font size=\"6\" color=\"008000\">Registeration Successful</font><br>\r\n" + 
								"Welcome " + user + "<br>\r\n" +
								"You will be redirected in 5 seconds<br>\r\n" +
								"<a href=\"/\">Not redirecting? Click here</a>\r\n" +
								"</center>\r\n", address).getBytes());
					}
					else
						o.write(getPageCode("Registeration failed", null, "",
								"<center>\r\n" +
								"<font size=\"6\" color=\"ff0000\">Registeration Failed</font>\r\n" + 
								"<form name=\"login\" action=\"/login/register\" method=\"post\">\r\n" +
								"Username: <input type=\"text\" name=\"username\" value=\"" + user + "\"><br>\r\n" +
								"Email: <input type=\"text\" name=\"email\" value=\"" + email + "\"><br>\r\n" +
								"Password: <input type=\"password\" name=\"password\"><br>\r\n" +
								"Confirm Password: <input type=\"password\" name=\"confirm\"><br>\r\n" +
								"<font color=\"ff0000\">" + failText + "</font><br>\r\n" +
								"<input type=\"submit\" value=\"Register\"/>\r\n" +
								"</form>\r\n" +
								"</center>\r\n", address).getBytes());
				}
				else{
					String user = userArray[0].substring(9);
					String pass = userArray[1].substring(9);
					
					if(manager.loginUser(user, pass, address)){
						o.write(getPageCode("Login Successful", null, "<meta http-equiv=\"refresh\" content=\"5; url=/\" />\r\n",
								"<center>\r\n" +
								"<font size=\"6\" color=\"008000\">Login Successful</font><br>\r\n" + 
								"Welcome " + user + "<br>\r\n" +
								"You will be redirected in 5 seconds<br>\r\n" +
								"<a href=\"/\">Not redirecting? Click here</a>\r\n" +
								"</center>\r\n", address).getBytes());
					}
					else{
						o.write(getPageCode("Login Failed", null, "",
								"<center>\r\n" +
								"<font size=\"6\" color=\"ff0000\">Login Failed</font>\r\n" + 
								"<form name=\"login\" action=\"/login\" method=\"post\">\r\n" +
								"Username: <input type=\"text\" name=\"username\" value=\"" + user + "\"><br>\r\n" +
								"Password: <input type=\"password\" name=\"password\"><br>\r\n" +
								"<font color=\"ff0000\">Invalid Username/Password combination</font><br>\r\n" +
								"<input type=\"submit\" value=\"Login\"/>\r\n" +
								"</form>\r\n" +
								"</center>\r\n", address).getBytes());
					}
				}
			}
			else if(request.startsWith("/register")){
				o.write(getPageCode("Register", null, "",
						"<center>\r\n" +
						"<font size=\"6\" color=\"000000\">Register</font>\r\n" + 
						"<form name=\"login\" action=\"/login/register\" method=\"post\">\r\n" +
						"Username: <input type=\"text\" name=\"username\"><br>\r\n" +
						"Email: <input type=\"text\" name=\"email\"><br>\r\n" +
						"Password: <input type=\"password\" name=\"password\"><br>\r\n" +
						"Confirm Password: <input type=\"password\" name=\"confirm\">\r\n" +
						"<br>\r\n" +
						"<input type=\"submit\" value=\"Register\"/>\r\n" +
						"</form>\r\n" +
						"</center>\r\n", address).getBytes());
			}
			else{
				o.write(getPageCode("Login", null, "",
						"<center>\r\n" +
						"<font size=\"6\" color=\"000000\">Login</font>\r\n" + 
						"<form name=\"login\" action=\"/login\" method=\"post\">\r\n" +
						"Username: <input type=\"text\" name=\"username\"><br>\r\n" +
						"Password: <input type=\"password\" name=\"password\">\r\n" +
						"<br>\r\n" +
						"<input type=\"submit\" value=\"Login\"/>\r\n" +
						"</form>\r\n" +
						"</center>\r\n", address).getBytes());
			}
		}
		catch (Exception e) {
		}
	}
}