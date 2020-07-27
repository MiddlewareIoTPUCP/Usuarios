package pucp.middlewareiot.usuarios.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @TextIndexed
    private String username;
    private String password;
    private Set<String> ownerTokens = new HashSet<>();
    
    public User() {
    
    }
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public Boolean addOwnerToken(String ownerToken) {
        if(ownerTokens.contains(ownerToken)) return false;
        return ownerTokens.add(ownerToken);
    }
    
    public Boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
