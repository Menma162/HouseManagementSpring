package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    @Query("select b from User b where b.username = :username and b.id != :id")
    List<User> findAllByEmailAndIdNot(Integer id, String username);
}
