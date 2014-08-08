package de.fau.cs.mad.yasme.android.entities;

/**
 * Created by florianwinklmeier on 16.06.14.
 */
public class DiffieHellmanPart {

    private long id;    // Unique id
    private Device device;
    private long dhId = -1;
    private String dh_g = "";
    private String dh_p = "";
    private String dh_A = "";

    public DiffieHellmanPart() {
    }

    public DiffieHellmanPart(Device device, long dhId, String dh_g, String dh_p, String dh_A) {
        this.device = device;
        this.dhId = dhId;
        this.dh_g = dh_g;
        this.dh_p = dh_p;
        this.dh_A = dh_A;
    }

    /**
     * Getters *
     */
    public long getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public long getDhId() {
        return dhId;
    }

    public String getDh_g() {
        return dh_g;
    }

    public String getDh_p() {
        return dh_p;
    }

    public String getDh_A() {
        return dh_A;
    }


    /**
     * Setters *
     */
    public void setId(long id) {
        this.id = id;
    }

    public void setDev(Device device) {
        this.device = device;
    }

    public void setDhId(long dhId) {
        this.dhId = dhId;
    }

    public void setDh_g(String dh_g) {
        this.dh_g = dh_g;
    }

    public void setDh_p(String dh_p) {
        this.dh_p = dh_p;
    }

    public void setDh_A(String dh_A) {
        this.dh_A = dh_A;
    }
}

