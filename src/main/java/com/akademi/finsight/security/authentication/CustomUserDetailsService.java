package com.akademi.finsight.security.authentication;


import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.repository.UserRepository;
import com.akademi.finsight.user.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) {
        String normalizedIdentifier = identifier.contains("@")
                ? EmailNormalizer.normalize(identifier)
                : identifier;

        User user = userRepository.findByIdentifier(normalizedIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication failed"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
