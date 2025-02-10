package dev.pixelib.reflectionpath.models;

public class TestPlayer {
    private final String name;
    private Connection connection;
    private String[] inventory;

    public TestPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setInventory(String[] inventory) {
        this.inventory = inventory;
    }
}
