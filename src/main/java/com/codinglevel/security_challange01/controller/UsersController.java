package com.codinglevel.security_challange01.controller;

import com.codinglevel.security_challange01.entities.Users;
import com.codinglevel.security_challange01.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UsersController {

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String[] ADMIN_ACCESS = {"ROLE_ADMIN", "ROLE_MANAGER"};
    private static final String[] MANAGER_ACCESS = {"ROLE_MANAGER"};

    private final UserRepository userRepository;
    private BCryptPasswordEncoder cryptPasswordEncoder;

    @PostMapping("/join")
    public String registerUser(@RequestBody Users users) {
        users.setRoles(DEFAULT_ROLE); //
        String encodedPassword = cryptPasswordEncoder.encode(users.getPassword());
        users.setPassword(encodedPassword);
        userRepository.save(users);
        return "User register success";
    }

    //TODO: First the application gonna have three ROLES, USER, ADMIN AND MANAGER
    //TODO: The default user has the role USER
    //TODO: if logged user has role ADMIN then he can give ADMIN OR MANAGER role to other users
    //TODO: if logged user has role MANAGER he can just give MANAGER role to other users

    //todo: give access to registered user
    @GetMapping("/access/{userId}/{userRole}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public String giveAccess(@PathVariable Long userId,
                             @PathVariable String userRole,
                             Principal principal) {
        Users users = userRepository.findById(userId).get();
        List<String> activeRoles = getRolesByLoggedUser(principal);
        String newRole = "";
        if(activeRoles.contains(userRole)) {
            newRole = users.getRoles() + "," + userRole;
            users.setRoles(newRole);
        }
        userRepository.save(users);
        return "Hallo " + users.getUserName() + " New role assign to you by " + principal.getName();
    }

    //Access only by ADMIN
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Users> getUsers() {
        return userRepository.findAll();
    }

    //Access only by MANAGER AND ADMIN
    @GetMapping("/manager_admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public String accessByManagerAndAdmin() {
        return "Hello here is just accessed by MANAGER and ADMIN";
    }

    //Access only by MANAGER
    @GetMapping("/manager_admin")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public String accessByManager() {
        return "Hello here is just accessed by MANAGER";
    }

    @GetMapping("/test")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String testUserAccess() {
        return "Hello here is just accessed by USER";
    }

    private List<String> getRolesByLoggedUser(Principal principal) {
        String roles = getLoginUser(principal).getRoles();
       List<String> giveRoles = Arrays.stream(roles.split(",")).collect(Collectors.toList());
       if(giveRoles.contains("ROLE_ADMIN")) {
           return Arrays.stream(ADMIN_ACCESS).collect(Collectors.toList());
       }
       if(giveRoles.contains("ROLE_MANAGER")) {
           return Arrays.stream(MANAGER_ACCESS).collect(Collectors.toList());
       }
       return Collections.emptyList();
    }

    //Get the logged user
    private Users getLoginUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).get();
    }
}
