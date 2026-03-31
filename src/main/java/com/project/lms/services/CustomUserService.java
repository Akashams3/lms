package com.project.lms.services;



import com.project.lms.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.project.lms.entity.User;
import org.springframework.stereotype.Component;


@Component
public class CustomUserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override

    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user =userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()

                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();
    }
}
