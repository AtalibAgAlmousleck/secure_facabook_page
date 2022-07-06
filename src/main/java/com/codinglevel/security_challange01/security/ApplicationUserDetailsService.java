package com.codinglevel.security_challange01.security;

import com.codinglevel.security_challange01.entities.Users;
import com.codinglevel.security_challange01.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Users> users = userRepository.findByEmail(email);
        return users.map(ApplicationUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Email  not found"));
    }
}
