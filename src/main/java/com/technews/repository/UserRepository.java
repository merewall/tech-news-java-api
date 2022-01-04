package com.technews.repository;

import com.technews.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// A repository in Java is any class that fulfills the role of data access object (DAO)
// A DAO contains data retrieval, storage and search functionality
// Repository CRUD methods and SQL are available via inheritance
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserByEmail(String email) throws Exception;
}
