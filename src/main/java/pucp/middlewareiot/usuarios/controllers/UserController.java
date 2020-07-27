package pucp.middlewareiot.usuarios.controllers;

import org.springframework.dao.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pucp.middlewareiot.usuarios.models.User;
import pucp.middlewareiot.usuarios.objects.UserCreate;
import pucp.middlewareiot.usuarios.services.UserService;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping(value = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> getUserInformation(@AuthenticationPrincipal Jwt principal) {
        return userService.getUserByUsername(principal.getSubject())
                .map(user -> ResponseEntity.ok().body(user))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }
    
    @PostMapping(value = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> registerUser(@RequestBody UserCreate userCreate) {
        return userService.create(userCreate.getUsername(), userCreate.getPassword())
                .flatMap(user -> Mono.just(ResponseEntity.created(URI.create("")).body(user)))
                .cast(ResponseEntity.class)
                .onErrorResume(throwable -> throwable instanceof DuplicateKeyException,
                        throwable -> Mono.just(ResponseEntity.badRequest().body("El nombre de usuario ya existe")));
    }
    
    @GetMapping(value = "/createOwnerToken")
    public Mono<ResponseEntity<String>> createOwnerToken(@AuthenticationPrincipal Jwt principal) {
        return userService.addOwnerToken(principal.getSubject())
                .map(s -> ResponseEntity.ok().body(s))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }
    
    @GetMapping(value = "/getOwnerTokens",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Set<String>>> getOwnerTokens(@RequestParam String username) {
        return userService.getOwnerTokens(username)
                .flatMap(ownerTokens -> Mono.just(ResponseEntity.ok().body(ownerTokens)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    @GetMapping(value = "/checkOwnerToken")
    public Mono<ResponseEntity<Object>> checkOwnerToken(@RequestParam String ownerToken) {
        return userService.checkOwnerToken(ownerToken)
                .filter(Boolean::booleanValue)
                .map(aBoolean -> ResponseEntity.ok().build())
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }
    
}
