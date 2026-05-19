package main.java.com.psychotest.model;


import java.util.ArrayList;
import java.util.List;

public class Group {
    private int id;
    private String name;
    private String description;
    private List<User> members;

    public Group() {
        this.members = new ArrayList<>();
    }

    public Group(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<User> getMembers() { return members; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setMembers(List<User> members) { this.members = members; }

    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
        }
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    @Override
    public String toString() {
        return name;
    }
}
