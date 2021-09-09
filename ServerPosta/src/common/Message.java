package common;

import java.io.Serializable;

public class Message implements Serializable {

    private final Integer operation;
    private final String clientName;
    private final String email;
    private final Integer id;

    public Message(Integer operation, String clientName) {
        this.operation = operation;
        this.clientName = clientName;
        this.email = null;
        this.id = null;
    }

    public Message(Integer operation, String clientName, String email) {
        this.operation = operation;
        this.clientName = clientName;
        this.email = email;
        this.id = null;
    }

    public Message(Integer operation, String clientName, Integer id) {
        this.operation = operation;
        this.clientName = clientName;
        this.email = null;
        this.id = id;
    }

    public Integer getOperation() {
        return operation;
    }

    public String getClientName() {
        return clientName;
    }

    public String getEmail() {
        return email;
    }

    public Integer getId() {
        return id;
    }
}
