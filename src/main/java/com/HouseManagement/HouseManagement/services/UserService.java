package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.models.User;
import com.HouseManagement.HouseManagement.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByName(String username) {
        return userRepository.findByUsername(username);
    }
    public void updateUser(User user){userRepository.save(user);}
    public List<User> getUsersByEmailNotId(Integer id, String email) {return userRepository.findAllByEmailAndIdNot(id, email);}
    public void deleteUser(Integer id){userRepository.deleteById(id);}
}
