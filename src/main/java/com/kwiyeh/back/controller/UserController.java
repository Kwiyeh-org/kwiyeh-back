package com.kwiyeh.back.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kwiyeh.back.service.UserService;

@RestController
public class UserController {
    public UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    /*@PostMapping ("/create")
    public String createUser(@RequestBody AppUser user) throws InterruptedException, ExecutionException{
        return userService.createUser(user);
    }

    @GetMapping ("/get")
    public AppUser getUser(@RequestParam String document_id) throws InterruptedException, ExecutionException{
        return userService.getUser(document_id);
    }

    @PutMapping ("/update")
    public String updateUser(@RequestBody AppUser user) throws InterruptedException, ExecutionException{
        return userService.updateUser(user);
    }

    @DeleteMapping ("/delete")
    public String deleteUser(@RequestParam String document_id){
        return userService.deleteUser(document_id);
    }*/
}
