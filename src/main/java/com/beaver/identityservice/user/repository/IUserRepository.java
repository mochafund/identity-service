package com.beaver.identityservice.user.repository;

import com.beaver.identityservice.user.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface IUserRepository extends ReactiveCrudRepository<User, UUID> {
    Mono<User> findByEmail(String email);
}
