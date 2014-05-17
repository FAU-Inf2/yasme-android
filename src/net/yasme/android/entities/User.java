package net.yasme.android.entities;

/**
 * Created by Stefan on 10.05.14.
 */
public class User {

	private String id;
	private String pw;
	private String email;
	private String name;

	public User(String pw, String name, String email) {
		this.pw = pw;
		this.name = name;
		this.email = email;

	}

	public User() {

	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPw() {
		return pw;
	}

	public String getId() {
		return id;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}
}
