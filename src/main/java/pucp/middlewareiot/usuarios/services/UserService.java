package pucp.middlewareiot.usuarios.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.stereotype.Service;
import pucp.middlewareiot.usuarios.models.User;
import pucp.middlewareiot.usuarios.repositories.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Flux<User> listAllUsers() {
        return userRepository.findAll();
    }
    
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Mono<?> create(String username, String password) {
        return userRepository.insert(new User(username, password)).onErrorResume(throwable -> throwable instanceof DuplicateKeyException, Mono::error);
    }
    
    public Mono<String> addOwnerToken(String username) {
        RandomValueStringGenerator rnd = new RandomValueStringGenerator(20);
        String newOwnerToken = rnd.generate();
        return userRepository.findByUsername(username).filter(user -> user.addOwnerToken(newOwnerToken)).flatMap(userRepository::save).map(user -> newOwnerToken);
    }
    
    public Mono<Boolean> checkPassword(String username, String password) {
        return userRepository.findByUsername(username).map(user -> user.checkPassword(password));
    }
    
    public Mono<Set<String>> getOwnerTokens(String username) {
        return userRepository.findByUsername(username).map(User::getOwnerTokens);
    }
    
    public Mono<Boolean> checkOwnerToken(String ownerToken) {
        return userRepository.findByOwnerToken(ownerToken)
                .map(user -> {
                    log.info(user.toString());
                    return Boolean.TRUE;
                })
                .switchIfEmpty(Mono.just(Boolean.FALSE));
    }
}
