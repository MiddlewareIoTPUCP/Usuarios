package pucp.middlewareiot.usuarios.objects;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserCreate {
    @NonNull
    private String username;
    @NonNull
    private String password;
}
