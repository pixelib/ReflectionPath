package dev.pixelib.reflectionpath.models;


public class Connection {
    private final ConnectionType type;

    public Connection(ConnectionType type) {
        this.type = type;
    }

    public ConnectionType getType() {
        return type;
    }
}
