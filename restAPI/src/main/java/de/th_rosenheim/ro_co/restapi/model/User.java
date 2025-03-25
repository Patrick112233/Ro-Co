package de.th_rosenheim.ro_co.restapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;

@Document("Member")
public class User {

    @Id
    private long id;
    private String firstName;
    private String lastName;
    //@ToDo: add communicator!
    //private ArrayList<Communicator> communicators;
    private HashSet<Skill> skills;

    public User(int id, String name, String email) {
        super();
        this.id = id;
        this.firstName = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {return lastName;}

    public HashSet<Skill> getSkills() {return skills;}

    public void setSkills(HashSet<Skill> skills) {this.skills = skills;}

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public void setLastName(String lastName) {this.lastName = lastName;}

}