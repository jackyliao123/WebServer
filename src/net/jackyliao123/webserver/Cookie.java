package net.jackyliao123.webserver;

public class Cookie {
	public final String name;
	public final String value;
	public final long validFor;
	public final long start;
	public Cookie(String name, String value, long validTimeMS) {
		this.name = name;
		this.value = value;
		this.validFor = validTimeMS;
		this.start = System.currentTimeMillis();
	}
	public boolean checkValid() {
		return start + validFor > System.currentTimeMillis();
	}
	public boolean equals(Object o){
		return (name.equals(((Cookie)o).name) && value.equals(((Cookie)o).value));
	}
}
