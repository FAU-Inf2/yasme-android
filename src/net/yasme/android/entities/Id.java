package net.yasme.android.entities;

public class Id {

	private long id = -1;

	/** Constructors **/
	public Id(long id) {
		this.id = id;
	}

	public Id() {

	}

	/** Getters **/
	public long getId() {
		return id;
	}

	/** Setters **/
	public void setId(long id) {
		this.id = id;
	}

	public Boolean equals(Id other) {
		return other.getId() == this.getId();
	}
}