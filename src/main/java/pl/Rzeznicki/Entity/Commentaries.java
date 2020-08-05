package pl.Rzeznicki.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "commentaries")
public class Commentaries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "recipes_id")
    private Recipes recipes;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "users_id")
    private Users users;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    public Recipes getRecipes() {
        return recipes;
    }

    public void setRecipes(Recipes recipes) {
        this.recipes = recipes;
    }

    public Commentaries(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
