package net.yasme.android.entities;

import java.io.Serializable;

/**
 * Created by Stefan on 10.05.14.
 */

public class User implements Serializable {

	private long id;
	private String pw;
	private String email;
	private String name;

	public User(String pw, String name, String email) {
		this.pw = pw;
		this.name = name;
		this.email = email;
	}

	public User(String email, String pw) {
		this.email = email;
		this.pw = pw;
	}

	public User(String name, long id) {
		this.name = name;
		this.id = id;
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

	public long getId() {
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

	public void setId(long id) {
		this.id = id;
	}
}
