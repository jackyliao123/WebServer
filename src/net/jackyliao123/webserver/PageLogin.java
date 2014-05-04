package net.jackyliao123.webserver;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.regex.Pattern;

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
					else if(!Pattern.compile(".+@.+\\.[a-z]+").matcher(email).matches()) {
						successful = false;
						failText = "Invalid email address!";
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
						o.write(getPageCode("Registration Successful", null, "<meta http-equiv=\"refresh\" content=\"5; url=/\" />",
								getPageContentCode(
								"<div style=\"text-align:center\">" +
								"<h2 stlye=\"color:008000;\">Registration Successful</h2><br>" + 
								"Welcome " + user + "<br>" +
								"You will be redirected in 5 seconds<br>" +
								"<a href=\"/\">Not redirecting? Click here</a>" +
								"</div>"
								), address).getBytes());
					}
					else
						o.write(getPageCode("Registration failed", null, "",
								getPageContentCode(
								"<h2 style=\"text-align:center\">Register</h2>" + 
								"<form style=\"padding-left:40%\" name=\"login\" action=\"/login/register\" method=\"post\">" +
								"Username: <input type=\"text\" name=\"username\">" +
								"<br>" +
								"Email: <input type=\"text\" name=\"email\">" +
								"<br>" +
								"Password: <input type=\"password\" name=\"password\">" +
								"<br>" +
								"Confirm Password: <input type=\"password\" name=\"confirm\">" +
								"<br>" +
								"<font color=\"ff0000\">" + failText + "</font><br>" +
								"<input type=\"submit\" value=\"Register\"/>" +
								"</form>"
								), address).getBytes());
				}
				else{
					String user = userArray[0].substring(9);
					String pass = userArray[1].substring(9);
					
					writeLoginResponseCode(o, address, user, pass, manager.loginUser(user, pass, address));
				}
			}
			else if(request.startsWith("/register"))
				writeRegisterCode(o, address);
			else if (request.startsWith("/account")) {
				User user = manager.getUserFromAddress(address);
				if (user != null)
					writeAccountManagementCode(o, address);
				else
					writeLoginPageCode(o, address);
			}
			else
				writeLoginPageCode(o, address);
		}
		catch (Exception e) {
		}
	}
	private void writeLoginResponseCode(OutputStream o, InetAddress address, String user, String pass, boolean wasSuccessful) throws IOException {
		if(wasSuccessful){
			o.write(getPageCode("Login Successful", null, "<meta http-equiv=\"refresh\" content=\"5; url=/\" />",
					getPageContentCode(
					"<div style=\"text-align:center\">" +
					"<h1 style=\"color:008000; margin:0px\">Login Successful</h1>" +
					"<br>" + 
					"Welcome " + user + "<br>" +
					"You will be redirected in 5 seconds<br>" +
					"<a href=\"/\">Not redirecting? Click here</a>" +
					"</div>"
					), address).getBytes());
		}
		else{
			o.write(getPageCode("Login Failed", null, "",
					getPageContentCode(
					"<h2 style=\"text-align:center\">Login Failed</h2>" + 
					"<form style=\"padding-left:40%\" name=\"login\" action=\"/login\" method=\"post\">" +
					"Username: <input type=\"text\" name=\"username\" value=\"" + user + "\"><br>" +
					"Password: <input type=\"password\" name=\"password\"><br>" +
					"<h4 style=\"color:ff0000; margin-top:0px;\">Invalid Username/Password combination</font><br>" +
					"<input type=\"submit\" value=\"Login\"/>" +
					"</form>"
					), address).getBytes());
		}
	}
	private void writeRegisterCode(OutputStream o, InetAddress address) throws IOException {
		o.write(getPageCode("Register", null, "",
				getPageContentCode(
				"<h2 style=\"text-align:center\">Register</h2>" + 
				"<form style=\"padding-left:40%\" name=\"login\" action=\"/login/register\" method=\"post\">" +
				"Username: <input type=\"text\" name=\"username\">" +
				"<br>" +
				"Email: <input type=\"text\" name=\"email\">" +
				"<br>" +
				"Password: <input type=\"password\" name=\"password\">" +
				"<br>" +
				"Confirm Password: <input type=\"password\" name=\"confirm\">" +
				"<br>" +
				"<input type=\"submit\" value=\"Register\"/>" +
				"</form>"
				), address).getBytes());
	}
	private void writeAccountManagementCode(OutputStream o, InetAddress address) throws IOException {
		o.write(getPageCode("Account Management", null, "", getPageContentCode(
				"<h2 style=\"text-align:center\">Account</h2></div>"
				), address).getBytes());
	}
	private void writeLoginPageCode(OutputStream o, InetAddress address) throws IOException {
		o.write(getPageCode("Login", null, "",
				getPageContentCode(
				"<h2 style=\"text-align:center\">Login</h2>" +
				"<form style=\"padding-left:40%\" name=\"login\" action=\"/login\" method=\"post\">" +
				"Username: <input type=\"text\" name=\"username\">" +
				"<br>" +
				"Password: <input type=\"password\" name=\"password\">" +
				"<br>" +
				"<input type=\"submit\" value=\"Login\"/>" +
				"</form>"
				), address).getBytes());
	}
}