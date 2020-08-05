package pl.Rzeznicki.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "categories_id")
    private Categories categories;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @JsonIgnore
    @OneToMany(mappedBy = "recipes")
    private List<Images> images;

    private String name;
    @Column(columnDefinition = "LONGTEXT")
    private String description;

   /* private int mark; */
    private int prepare_time;

    // 0 - easy, 1 - normal, 2 - hard
    private int level;

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    @Column(columnDefinition = "LONGTEXT")
    private String ingredients;


   /* public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }
*/

    public Users getUser() {
        return users;
    }

    public void setUser(Users user) {
        this.users = user;
    }

    public Recipes() {
    }

    public Categories getCategories() {
        return categories;
    }

    public List<Images> getImages() {
        return images;
    }

    public void setImages(List<Images> images) {
        this.images = images;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPrepare_time() {
        return prepare_time;
    }

    public void setPrepare_time(int prepare_time) {
        this.prepare_time = prepare_time;
    }


}
