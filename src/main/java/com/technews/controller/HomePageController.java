package com.technews.controller;

import com.technews.model.Post;
import com.technews.model.User;
import com.technews.model.Comment;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class HomePageController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    CommentRepository commentRepository;

    // login endpoint
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {

        // Redirect to homepage once logged-in
        if (request.getSession(false) != null) {
            return "redirect:/";
        }

        // sends info to Thymeleaf templates
        // in this case, sends newly created user
        model.addAttribute("user", new User());
        return "login";
    }

    // logout endpoint
    @GetMapping("/users/logout")
    public String logout(HttpServletRequest request) {
        // If session exists, invalidate session
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        // Redirect to the login page
        return "redirect:/login";
    }

    // homepage endpoint
    @GetMapping("/")
    public String homepageSetup(Model model, HttpServletRequest request) {
        // Variable for logged-in user info for session
        User sessionUser = new User();

        // If user logged-in, set session variable of loggedIn
        if (request.getSession(false) != null) {
            sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        } else {
            model.addAttribute("loggedIn", false);
        }

        // Create variable postList for all posts in database
        List<Post> postList = postRepository.findAll();
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.getOne(p.getUserId());
            p.setUserName(user.getUsername());
        }

        // Add info to the user model
        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        model.addAttribute("point", "point");
        model.addAttribute("points", "points");

        // Render homepage template
        return "homepage";
    }

    // dashboard endpoint
    @GetMapping("/dashboard")
    public String dashboardPageSetup(Model model, HttpServletRequest request) throws Exception {

        // confirm user is logged in, setup dashboard, and render
        // otherwise, redirect to the login page
        if (request.getSession(false) != null) {
            setupDashboardPage(model, request);
            return "dashboard";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }

    // endpoint for when a post's title or link are missing
    @GetMapping("/dashboardEmptyTitleAndLink")
    public String dashboardEmptyTitleAndLinkHandler(Model model, HttpServletRequest request) throws Exception {
        // setup dashboard and pass the notice to the dashboard template
        setupDashboardPage(model, request);
        model.addAttribute("notice", "To create a post the Title and Link must be populated!");
        // render dashboard
        return "dashboard";
    }

    // endpoint for empty comment on post
    @GetMapping("/singlePostEmptyComment/{id}")
    public String singlePostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        // set up single post page and pass the notice to the single post page
        setupSinglePostPage(id, model, request);
        model.addAttribute("notice", "To add a comment you must enter the comment in the comment text area!");
        // render single post page
        return "single-post";
    }

    // single post endpoint
    @GetMapping("/post/{id}")
    public String singlePostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        return "single-post";
    }

    // empty comment on edit post page endpoint
    @GetMapping("/editPostEmptyComment/{id}")
    public String editPostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        // Render edit-post page with notice if user logged-in;
        // otherwise, redirect to login page
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            model.addAttribute("notice", "To add a comment you must enter the comment in the comment text area!");
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }

    // Edit a single post endpoint
    @GetMapping("/dashboard/edit/{id}")
    public String editPostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        // If user is logged-in, render edit-post page
        // otherwise, redirect to login page
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }


    // Method for setting up dashboard page
    public Model setupDashboardPage(Model model, HttpServletRequest request) throws Exception {
        // Assign current user info to variable via session
        User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");

        // Variable for current user id
        Integer userId = sessionUser.getId();

        // Get all the logged-in user's posts
        List<Post> postList = postRepository.findAllPostsByUserId(userId);
        // For each of the user's posts, set the vote count, user, and username
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.getOne(p.getUserId());
            p.setUserName(user.getUsername());
        }

        // Pass the following to the Thymeleaf template for rendering
        model.addAttribute("user", sessionUser);
        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        model.addAttribute("post", new Post());

        return model;
    }

    // Method for setting up single post page
    public Model setupSinglePostPage(int id, Model model, HttpServletRequest request) {
        // If session exists...
        if (request.getSession(false) != null) {
            // Set user info to variable
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            // Pass the following to the Thymeleaf template
            model.addAttribute("sessionUser", sessionUser);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        }

        // Variable from post by id from database
        Post post = postRepository.getOne(id);
        // Set vote count for the post
        post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));

        // Variable for author of post
        User postUser = userRepository.getOne(post.getUserId());
        // Set the post's username
        post.setUserName(postUser.getUsername());

        // Variable for list of comments for the post
        List<Comment> commentList = commentRepository.findAllCommentsByPostId(post.getId());

        // Pass the post to the template
        model.addAttribute("post", post);

        // Pass the comment list and new comment to template
        model.addAttribute("commentList", commentList);
        model.addAttribute("comment", new Comment());

        return model;
    }

    // Method to set up edit post page
    public Model setupEditPostPage(int id, Model model, HttpServletRequest request) {
        // If session exists...
        if (request.getSession(false) != null) {
            // Set user info to variable
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");

            // Variable for post to edit by id
            Post returnPost = postRepository.getOne(id);
            // Variable for post's author (user)
            User tempUser = userRepository.getOne(returnPost.getUserId());
            // Set post's username and vote count
            returnPost.setUserName(tempUser.getUsername());
            returnPost.setVoteCount(voteRepository.countVotesByPostId(returnPost.getId()));

            // Get all comments for the post to edit by id
            List<Comment> commentList = commentRepository.findAllCommentsByPostId(returnPost.getId());

            // Pass the following to the edit post template
            model.addAttribute("post", returnPost);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
            model.addAttribute("commentList", commentList);
            model.addAttribute("comment", new Comment());
        }

        return model;
    }
}
