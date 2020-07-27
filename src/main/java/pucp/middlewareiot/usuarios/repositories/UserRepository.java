package pucp.middlewareiot.usuarios.repositories;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pucp.middlewareiot.usuarios.models.User;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    
    Mono<User> findByUsername(String username);
    
    @Query("{ 'ownerTokens': ?0 }")
    Mono<User> findByOwnerToken(String ownerToken);
}
