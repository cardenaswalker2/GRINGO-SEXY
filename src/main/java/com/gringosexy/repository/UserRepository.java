package com.gringosexy.repository;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import com.gringosexy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    long countByStatus(UserStatus status);

    long countByDeviceType(DeviceType deviceType);

    long countByRole(Role role);

    @Query("{ $or: [ " +
           "{ 'fullName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'username': { $regex: ?0, $options: 'i' } }, " +
           "{ 'email': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<User> searchUsers(String query, Pageable pageable);
}
