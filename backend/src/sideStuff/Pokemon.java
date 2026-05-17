package sideStuff;

public class Pokemon {
    private String name;
    private String type;
    private int level;

    public Pokemon(String name, String type, int level) {
        this.name = name;
        this.type = type;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return name + " (Type: " + type + ", Level: " + level + ")";
    }
}
