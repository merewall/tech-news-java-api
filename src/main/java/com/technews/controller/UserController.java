package com.technews.controller;

import com.technews.model.Post;
import com.technews.model.User;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    // Autowired tells Spring to scan the project
    // for objects that will need to be instantiated
    // for a class or method to run
    @Autowired
    UserRepository repository;

    @Autowired
    VoteRepository voteRepository;

    // GET all users
    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        // Get a list of all users and assign to variable of userList
        List<User> userList = repository.findAll();

        // For every user...
        for (User u : userList) {
            // get all posts and assign to list variable
            List<Post> postList = u.getPosts();
            // For every post...
            for (Post p : postList) {
                // use post id to count the votes and set the vote count
                p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            }
        }
        return userList;
    }

    // GET a single user by id
    @GetMapping("/api/users/{id}")
    public User getUserById(@PathVariable Integer id) {
        User returnUser = repository.getOne(id);
        // include posts
        List<Post> postList = returnUser.getPosts();
        // and vote counts on posts
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
        }

        return returnUser;
    }

    // ADD a single user to database
    @PostMapping("/api/users")
    public User addUser(@RequestBody User user) {
        // Encrypt password
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        repository.save(user);
        return user;
    }

    // UPDATE a single user by id
    @PutMapping("/api/users/{id}")
    public User updateUser(@PathVariable int id, @RequestBody User user) {
        User tempUser = repository.getOne(id);

        if (!tempUser.equals(null)) {
            user.setId(tempUser.getId());
            repository.save(user);
        }
        return user;
    }

    // DELETE a user by id from database
    @DeleteMapping("/api/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int id) {
        repository.deleteById(id);
    }
}
