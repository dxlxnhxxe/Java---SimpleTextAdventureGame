package edu.uob;

public abstract class GameEntity {
    public String name;
    public String description;

    public GameEntity(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    @Override
    public String toString(){
        return String.format("%s : %s", this.getName(), this.getDescription());
    }
}



