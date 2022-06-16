package io.github.ilkou.springbootchallenge.repository;

import io.github.ilkou.springbootchallenge.repository.entity.CtUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<CtUser, String> {

    Optional<CtUser> findOneByEmail(String email);
    Optional<CtUser> findOneByUsername(String username);
}
