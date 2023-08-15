package com.ibtehaj.Ecom.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Models.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email); 
    User findByUsernameOrEmail(String username, String email);
    List<User> findByRolesContaining(UserRole role);
}
