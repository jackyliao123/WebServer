package net.jackyliao123.webserver;

public class User {
	public final String username;
	public final long lastLogin;
	public final long validFor;
	public final Cookie cookie;
	public User(String username, long validForMS, Cookie cookie){
		this.username = username;
		lastLogin = System.currentTimeMillis();
		validFor = validForMS;
		this.cookie = new Cookie(cookie.name, cookie.value, validFor);
	}
	public boolean checkValid(){
		return cookie.checkValid();
	}
	public boolean equals(Object o){
		return username.equals(((User)o).username);
	}
}
