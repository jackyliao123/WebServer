import java.net.InetAddress;

public class User {
	public final String username;
	public final long lastLogin;
	public final long validFor;
	public final InetAddress address;
	public User(String username, long validForMS, InetAddress address){
		this.username = username;
		lastLogin = System.currentTimeMillis();
		validFor = validForMS;
		this.address = address;
	}
	public boolean checkValid(){
		return lastLogin + validFor > System.currentTimeMillis();
	}
	public boolean equals(Object o){
		return username.equals(((User)o).username);
	}
}
