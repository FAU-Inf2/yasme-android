package net.yasme.android.entities;

import java.io.Serializable;

public class ChatProperties implements Serializable {

    private String status;

    private String name;


    /**
     * Constructors *
     */
    public ChatProperties(String name, String status) {
        this.status = status;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }
}
