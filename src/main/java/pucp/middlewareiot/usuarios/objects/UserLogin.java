package pucp.middlewareiot.usuarios.objects;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserLogin {
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private String login_challenge;
}
