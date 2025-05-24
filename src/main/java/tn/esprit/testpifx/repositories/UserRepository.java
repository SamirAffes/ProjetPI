package tn.esprit.testpifx.repositories;

import tn.esprit.testpifx.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void deleteById(String userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsById(String userId);
    List<User> findByTeamId(String teamId);
}