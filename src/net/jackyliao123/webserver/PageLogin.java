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
	public ResponseParams getHeader(String req, String[] reqParams, String method, String postData, Cookie cookie){
		return new ResponseParams(getPageHeader(getCode(req), getContentType(req), getContentLength(req), cookie), new Object[]{postData != null, postData});
	}
	public void writePageContent(ResponseParams param, String request, OutputStream o, Cookie cookie){
		UserManager manager = WebServer.instance.manager;
		try {
			if(request.startsWith("/logout")){
				manager.userLogout(manager.getUserFromCookie(cookie));
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
						failText = "Passwords do not match";
					}
					else if(pass.length() < 6){
						successful = false;
						failText = "Passwords must be at least 6 characters in length";
					}
					
					if(successful){
						manager.registerUser(user, email, pass);
						manager.loginUser(user, pass, cookie);
						o.write(getPageCode("Registration Successful", null, "<meta http-equiv=\"refresh\" content=\"5; url=/\" />",
								getPageContentCode(
								"<div style=\"text-align:center\">" +
								"<h2 stlye=\"color:008000;\">Registration Successful</h2><br>" + 
								"Welcome " + user + "<br>" +
								"You will be redirected in 5 seconds<br>" +
								"<a href=\"/\">Not redirecting? Click here</a>" +
								"</div>"
								), cookie).getBytes());
					}
					else
						o.write(getPageCode("Registration failed", null, "",
								getPageContentCode(
								"<h2 style=\"text-align:center;\">Register</h2>" + 
								"<h4 style=\"color:ff0000; text-align:center; margin:0;\">" + failText + "</h4><br>" +
								"<form style=\"padding-left:40%\" name=\"login\" action=\"/login/register\" method=\"post\">" +
								"<table>" +
								"<tr> <td>" +
								"<label for=\"user\">Username: </label>" +
								"</td> <td>" +
								"<input id=\"user\" type=\"text\" name=\"username\">" +
								"</td> </tr>" +
								"<tr> <td>" +
								"<label for=\"email\">Email: </label>" +
								"</td> <td>" +
								"<input id=\"email\" type=\"text\" name=\"email\">" +
								"</td> </tr>" +
								"<tr> <td>" +
								"<label for=\"password\">Password: </label>" +
								"</td> <td>" +
								"<input id=\"password\" type=\"password\" name=\"password\">" +
								"</td> </tr>" +
								"<tr> <td>" +
								"<label for=\"confirm\">Confirm Password: </label>" +
								"</td> <td>" +
								"<input id=\"confirm\" type=\"password\" name=\"confirm\">" +
								"</td> </tr>" +
								"<tr> <td>" +
								"<input type=\"submit\" value=\"Register\"/>" +
								"</td> </tr>" +
								"</table>" +
								"</form>"
								), cookie).getBytes());
				} else if(request.startsWith("/changepass")){
					User user = manager.getUserFromCookie(cookie);
					if (user != null) {
						String pass = userArray[0].substring(9);
						String confirmPass = userArray[1].substring(8);
						String oldPass = userArray[2].substring(8);
						String failText = "";
						boolean successful = true;
						if (!pass.equals(confirmPass)) {
							successful = false;
							failText = "Passwords do not match";
						} else if (pass.length() < 6) {
							successful = false;
							failText = "Passwords must be at least 6 characters in length";
						} else if (manager.checkUserPassword(user.username, oldPass) == null) {
							successful = false;
							failText = "Old password does not match";
						}
						if (successful) {
							manager.userChangePassword(user.username, pass);
							o.write(getPageCode("Change Password Successful", null, "<meta http-equiv=\"refresh\" content=\"5; url=/\" />",
									getPageContentCode(
									"<div style=\"text-align:center\">" +
									"<h2 stlye=\"color:008000;\">Change Password Successful</h2><br>" + 
									"Welcome " + user.username + "<br>" +
									"You will be redirected in 5 seconds<br>" +
									"<a href=\"/\">Not redirecting? Click here</a>" +
									"</div>"
									), cookie).getBytes());
						} else
							o.write(getPageCode("Change Password Failed", null, "", 
									getPageContentCode(
									"<h2 style=\"text-align:center\">Account</h2></div>" +
									"<h4 style=\"color:ff0000; text-align:center; margin:0;\">" + failText + "</h4><br>" +
									"<form style=\"padding-left:40%\" name=\"changepass\" action=\"/login/changepass\" method=\"post\">" +
								    "<table> <tr> <td>" +
									"<label for=\"newpass\">New Password: </label>" +
									"</td> <td>" +
									"<input id=\"newpass\" type=\"password\" name=\"password\">" +
								    "</td> </tr>" +
									"<tr> <td>" +
								    "<label for=\"confirm\">Confirm: </label>" +
								    "</td> <td>" +
									"<input id=\"confirm\" type=\"password\" name=\"confirm\">" +
								    "</td> </tr>" +
									"<tr> <td>" +
								    "<label for=\"oldpass\">Old password: </label>" +
									"</td> <td>" +
								    "<input id=\"oldpass\" type=\"password\" name=\"oldpass\">" +
								    "</td> </tr>" +
									"<tr> <td>" +
								    "<input type=\"submit\" value=\"Change Password\"/>" +
									"</td> </tr> </table> </form>"
									), cookie).getBytes());
					}
				} else{
					String user = userArray[0].substring(9);
					String pass = userArray[1].substring(9);
					
					writeLoginResponseCode(o, cookie, user, pass, manager.loginUser(user, pass, cookie));
				}
			}
			else if(request.startsWith("/register"))
				writeRegisterCode(o, cookie);
			else if (request.startsWith("/account")) {
				User user = manager.getUserFromCookie(cookie);
				if (user != null)
					writeAccountManagementCode(o, cookie);
				else
					writeLoginPageCode(o, cookie);
			}
			else
				writeLoginPageCode(o, cookie);
		}
		catch (Exception e) {
		}
	}
	private void writeLoginResponseCode(OutputStream o, Cookie cookie, String user, String pass, boolean wasSuccessful) throws IOException {
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
					), cookie).getBytes());
		}
		else{
			o.write(getPageCode("Login Failed", null, "",
					getPageContentCode(
					"<h2 style=\"text-align:center\">Login</h2>" +
					"<h4 style=\"text-align:center; color:ff0000; margin:0px;\">Invalid Username/Password combination</h4><br>" +
					"<form style=\"padding-left:40%\" name=\"login\" action=\"/login\" method=\"post\">" +
					"<table>" +
					"<tr><td>" +
					"<label for=\"user\">Username: </label>" +
					"</td><td>" +
					"<input id=\"user\" type=\"text\" name=\"username\">" +
					"</td></tr>" +
					"<tr><td>" +
					"<label for=\"pass\">Password: </label>" +
					"</td><td>" +
					"<input type=\"password\" name=\"password\">" +
					"</td></tr>" +
					"<tr><td>" +
					"<input type=\"submit\" value=\"Login\"/>" +
					"</td></tr>" +
					"</table>" +
					"</form>"
					), cookie).getBytes());
		}
	}
	private void writeRegisterCode(OutputStream o, Cookie cookie) throws IOException {
		o.write(getPageCode("Register", null, "",
				getPageContentCode(
				"<h2 style=\"text-align:center\">Register</h2>" + 
				"<form style=\"padding-left:40%\" name=\"login\" action=\"/login/register\" method=\"post\">" +
				"<table>" +
				"<tr> <td>" +
				"<label for=\"user\">Username: </label>" +
				"</td> <td>" +
				"<input id=\"user\" type=\"text\" name=\"username\">" +
				"</td> </tr>" +
				"<tr> <td>" +
				"<label for=\"email\">Email: </label>" +
				"</td> <td>" +
				"<input id=\"email\" type=\"text\" name=\"email\">" +
				"</td> </tr>" +
				"<tr> <td>" +
				"<label for=\"password\">Password: </label>" +
				"</td> <td>" +
				"<input id=\"password\" type=\"password\" name=\"password\">" +
				"</td> </tr>" +
				"<tr> <td>" +
				"<label for=\"confirm\">Confirm Password: </label>" +
				"</td> <td>" +
				"<input id=\"confirm\" type=\"password\" name=\"confirm\">" +
				"</td> </tr>" +
				"<tr> <td>" +
				"<input type=\"submit\" value=\"Register\"/>" +
				"</td> </tr>" +
				"</table>" +
				"</form>"
				), cookie).getBytes());
	}
	private void writeAccountManagementCode(OutputStream o, Cookie cookie) throws IOException {
		o.write(getPageCode("Account Management", null, "", getPageContentCode(
				"<h2 style=\"text-align:center\">Account</h2></div>" +
				"<form style=\"padding-left:40%\" name=\"changepass\" action=\"/login/changepass\" method=\"post\">" +
			    "<table> <tr> <td>" +
				"<label for=\"newpass\">New Password: </label>" +
			    "</td> <td>" +
				"<input id=\"newpass\" type=\"password\" name=\"password\">" +
			    "</td> </tr>" +
				"<tr> <td>" +
			    "<label for=\"confirm\">Confirm: </label>" +
			    "</td> <td>" +
				"<input id=\"confirm\" type=\"password\" name=\"confirm\">" +
			    "</td> </tr>" +
				"<tr> <td>" +
			    "<label for=\"oldpass\">Old password: </label>" +
				"</td> <td>" +
			    "<input id=\"oldpass\" type=\"password\" name=\"oldpass\">" +
			    "</td> </tr>" +
				"<tr> <td>" +
			    "<input type=\"submit\" value=\"Change Password\"/>" +
				"</td> </tr> </table> </form>" 
				), cookie).getBytes());
	}
	private void writeLoginPageCode(OutputStream o, Cookie cookie) throws IOException {
		o.write(getPageCode("Login", null, "",
				getPageContentCode(
				"<h2 style=\"text-align:center\">Login</h2>" +
				"<form style=\"padding-left:40%\" name=\"login\" action=\"/login\" method=\"post\">" +
				"<table>" +
				"<tr><td>" +
				"<label for=\"user\">Username: </label>" +
				"</td><td>" +
				"<input id=\"user\" type=\"text\" name=\"username\">" +
				"</td></tr>" +
				"<tr><td>" +
				"<label for=\"pass\">Password: </label>" +
				"</td><td>" +
				"<input type=\"password\" name=\"password\">" +
				"</td></tr>" +
				"<tr><td>" +
				"<input type=\"submit\" value=\"Login\"/>" +
				"</td></tr>" +
				"</table>" +
				"</form>"
				), cookie).getBytes());
	}
}