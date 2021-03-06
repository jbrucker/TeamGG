package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import forms.AddUserForm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Json;
import utils.Auth;

import javax.persistence.*;
import java.util.List;

@Entity
@JsonIgnoreProperties({"password"})
public class User extends Model {
    @Id
    public Integer id;

    @Constraints.Required
    @Column(unique = true)
    public String username;

    public String password;

    @Constraints.Required
    public TYPES type = TYPES.VOTER;

    public String name = "";
    public String organization = "";

    @ManyToOne()
    @JoinColumn(name="group_id", referencedColumnName="id")
    public Groups group;

    @OneToMany(cascade=CascadeType.REMOVE, mappedBy = "user")
    public List<Vote> votes;

    public String getName(){
        if(name != null && !name.isEmpty()){
            return name;
        }else{
            return username;
        }
    }

    public JsonNode toAdminJson(){
        ObjectNode out = (ObjectNode) Json.toJson(this);
        out.put("has_password", password != null && !password.isEmpty());
        out.put("name", name);
        out.remove("group");
        return out;
    }

    public void setPassword(String password){
        if(password.isEmpty()){
            this.password = "";
        }else {
            this.password = Auth.getHasher().encode(password);
        }
    }

    public boolean checkPassword(String password){
        return Auth.getHasher().matches(password, this.password);
    }

    public void fromForm(AddUserForm data) {
        fromForm(data, true);
    }

    public void fromForm(AddUserForm data, boolean usePassword){
        username = data.username;
        if(usePassword) {
            setPassword(data.password);
        }
        name = data.name;
        organization = data.organization;
        type = data.type;
    }

    public boolean hasPassword(){
        return password != null && !password.isEmpty();
    }

    public enum TYPES {
        VOTER, INSTRUCTOR, ORGANIZER
    }

    public static Model.Finder<Integer, User> find = new Model.Finder<Integer, User>(
            Integer.class, User.class
    );
}
