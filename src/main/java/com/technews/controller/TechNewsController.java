// TechNewsController manages the internal API processing calls
// for the many requests that a user can make to the API
// as well as display/retrieval of data to/from the user
package com.technews.controller;

import com.technews.model.Post;
import com.technews.model.User;
import com.technews.model.Vote;
import com.technews.model.Comment;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class TechNewsController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    // login POST endpoint
    @PostMapping("/users/login")
    public String login(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {

        // Verify all login input fields are valid...
        if ((user.getPassword().equals(null) || user.getPassword().isEmpty()) || (user.getEmail().equals(null) || user.getPassword().isEmpty())) {
            // If invalid, pass the error notice and re-render login page
            model.addAttribute("notice", "Email address and password must be populated in order to login!");
            return "login";
        }

        // If inputs all valid, assign user info found in database to session variable
        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        // Catch block for if no user found in database
        try {
            if (sessionUser.equals(null)) {

            }
        } catch (NullPointerException e) {
            model.addAttribute("notice", "Email address is not recognized!");
            return "login";
        }

        // Validate Password
        String sessionUserPassword = sessionUser.getPassword();
        boolean isPasswordValid = BCrypt.checkpw(user.getPassword(), sessionUserPassword);
        if(isPasswordValid == false) {
            model.addAttribute("notice", "Password is not valid!");
            return "login";
        }

        // Set loggedIn variable to true once user logged-in
        // and set user variable to session
        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        // redirect to user's dashboard after logged-in
        return "redirect:/dashboard";
    }

    // end point for POST to create new user
    @PostMapping("/users")
    public String signup(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {

        // Error catch for if signup input fields not valids
        if ((user.getUsername().equals(null) || user.getUsername().isEmpty()) || (user.getPassword().equals(null) || user.getPassword().isEmpty()) || (user.getEmail().equals(null) || user.getPassword().isEmpty())) {
            model.addAttribute("notice", "In order to signup username, email address and password must be populated!");
            return "login";
        }

        // Save new user and hash password
        // if email already in database, throw error
        try {
            // Encrypt password
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("notice", "Email address is not available! Please choose a different unique email address.");
            return "login";
        }

        // Variable for user's info for session
        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        // Error catch if user not found
        try {
            if (sessionUser.equals(null)) {

            }
        } catch (NullPointerException e) {
            model.addAttribute("notice", "User is not recognized!");
            return "login";
        }

        // Once logged-in, set session variable of loggedIn and user's info to session variable
        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        // Once logged-in, redirect to the user's dashboard
        return "redirect:/dashboard";
    }

    // GET endpoint for the dashboard
    @PostMapping("/posts")
    public String addPostDashboardPage(@ModelAttribute Post post, Model model, HttpServletRequest request) {
        // Validation for post input fields
        if ((post.getTitle().equals(null) || post.getTitle().isEmpty()) || (post.getPostUrl().equals(null) || post.getPostUrl().isEmpty())) {
            return "redirect:/dashboardEmptyTitleAndLink";
        }

        // If user not logged in, redirect to login page
        // otherwise, direct to dashboard
        if (request.getSession(false) == null) {
            return "redirect:/login";
        } else {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            post.setUserId(sessionUser.getId());
            postRepository.save(post);

            return "redirect:/dashboard";
        }
    }

    // POST endpoint fo single post
    @PostMapping("/posts/{id}")
    public String updatePostDashboardPage(@PathVariable int id, @ModelAttribute Post post, Model model, HttpServletRequest request) {
        // If session doesn't exists...
        if (request.getSession(false) == null) {
            // Pass user info and redirect to dashboard or login page
            model.addAttribute("user", new User());
            return "redirect/dashboard";
        } else {
            // if session exists, get the post by id from the database
            Post tempPost = postRepository.getOne(id);
            // set the updated title to post and save the post in database
            tempPost.setTitle(post.getTitle());
            postRepository.save(tempPost);

            return "redirect:/dashboard";
        }
    }

    // POST endpoint to add comments
    @PostMapping("/comments")
    public String createCommentCommentsPage(@ModelAttribute Comment comment, Model model, HttpServletRequest request) {
        // If empty input fields in comment, throw exception and re-render edit-post template
        if (comment.getCommentText().isEmpty() || comment.getCommentText().equals(null)) {
            return "redirect:/singlePostEmptyComment/" + comment.getPostId();
        } else {
            // if session is valid...
            if (request.getSession(false) != null) {
                // add user info to variable for session
                User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
                // Set author to logged-in user and save comment
                comment.setUserId(sessionUser.getId());
                commentRepository.save(comment);
                // redirect to the post  with the new comment
                return "redirect:/post/" + comment.getPostId();
            } else {
                // if session is invalid, redirect to login page
                return "login";
            }
        }
    }

    // POST endpoint for editing comments
    @PostMapping("/comments/edit")
    public String createCommentEditPage(@ModelAttribute Comment comment, HttpServletRequest request) {

        // If input fields for comment are invalid, throw exception and re-render page
        if (comment.getCommentText().equals("") || comment.getCommentText().equals(null)) {
            return "redirect:/editPostEmptyComment/" + comment.getPostId();
        } else {
            // If session is valid
            if (request.getSession(false) != null) {
                // Set user info to session variable
                User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
                // Update and save the comment
                comment.setUserId(sessionUser.getId());
                commentRepository.save(comment);

                // redirect to the edit dashboard with the updated comment
                return "redirect:/dashboard/edit/" + comment.getPostId();
            } else {
                // if session is invalid, redirect to login page
                return "redirect:/login";
            }
        }
    }

    // PUT route for up-voting post
    @PutMapping("/posts/upvote")
    public void addVoteCommentsPage(@RequestBody Vote vote, HttpServletRequest request, HttpServletResponse response) {
        // if session is valid...
        if (request.getSession(false) != null) {
            // Set user info to session variable and placeholder variable for post
            Post returnPost = null;
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            // Set the logged-in user to the voter and save the vote
            vote.setUserId(sessionUser.getId());
            voteRepository.save(vote);

            // Find the post by id, and set the vote count
            returnPost = postRepository.getOne(vote.getPostId());
            returnPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));
        }
    }
}
