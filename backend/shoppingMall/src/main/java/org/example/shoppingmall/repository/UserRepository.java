package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :principal OR u.email = :principal OR u.phone = :principal")
    Optional<User> findByPrincipal(@Param("principal") String principal); // 急切加载角色

    Optional<User> findByUsername(String username); // Spring Security 可能直接用这个
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}