package net.yasme.android.entities;

/**
 * Created by Stefan on 10.05.14.
 */
public class User {

    private String id;
    private String pw;
    private String name;

    public User(String id) {
        this.id = id;
        //removed pw
    }
    
    public User(String name, String pw) {
    	this.name = name;
    	this.pw = pw;
    }

    public User(){}

    public String getPw() {
        return pw;
    }

    public String getId() {
        return id;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    // no setter for the id!!
}
