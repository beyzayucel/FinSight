package com.akademi.finsight.user.service.impl;


import com.akademi.finsight.auth.refreshtoken.service.RefreshTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.user.dto.CreateUserRequest;
import com.akademi.finsight.user.dto.CreateUserResponse;
import com.akademi.finsight.user.dto.UpdateProfileRequest;
import com.akademi.finsight.user.dto.UserResponse;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.EmailAlreadyExistsException;
import com.akademi.finsight.user.exception.PhoneAlreadyExistsException;
import com.akademi.finsight.user.exception.UserNotFoundException;
import com.akademi.finsight.user.mapper.UserMapper;
import com.akademi.finsight.user.repository.UserRepository;
import com.akademi.finsight.user.service.UserService;
import com.akademi.finsight.user.util.CredentialsGenerator;
import com.akademi.finsight.user.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        checkDuplicateUser(normalizedEmail, request.phoneNumber());

        String username = generateUniqueUsername(request.firstName(), request.lastName());
        String temporaryPassword = CredentialsGenerator.generateTemporaryPassword();

        User user = userMapper.toEntity(request);
        user.setEmail(normalizedEmail);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        User savedUser = userRepository.save(user);

        log.info("User created: event=USER_CREATED, email={}", MaskType.EMAIL.mask(savedUser.getEmail()));

        return new CreateUserResponse(savedUser.getEmail(), username, temporaryPassword);
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        String base = CredentialsGenerator.generateBaseUsername(firstName, lastName);
        String username = base;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = base + suffix;
            suffix++;
        }

        return username;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        return userRepository.findByEmail(EmailNormalizer.normalize(email))
                .map(userMapper::toResponse)
                .orElseThrow(UserNotFoundException::new);
    }


    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(UserNotFoundException::new);
    }


    @Override
    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(User user, String encodedPassword, boolean clearFirstLogin) {
        user.setPassword(encodedPassword);

        if (clearFirstLogin) {
            user.setFirstLogin(false);
        }

        userRepository.save(user);
        log.info("Password updated: event=PASSWORD_UPDATED, email={}, firstLoginCleared={}", MaskType.EMAIL.mask(user.getEmail()), clearFirstLogin);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        checkPhoneNumberAvailability(request.phoneNumber(), user.getId());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());

        User savedUser = userRepository.save(user);
        log.info("Profile updated: event=PROFILE_UPDATED, email={}", MaskType.EMAIL.mask(user.getEmail()));
        return userMapper.toResponse(savedUser);
    }


    @Override
    @Transactional
    public void deleteCurrentUser(String email) {
        User user = findByEmail(email);
        refreshTokenService.revokeAllByUser(user);
        userRepository.delete(user);
        log.info("User self-deleted: event=USER_SELF_DELETED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    private void checkDuplicateUser(String email, String phoneNumber) {
        if (userRepository.existsByEmail(email)) {
            log.info("User creation rejected: event=EMAIL_ALREADY_EXISTS, email={}", MaskType.EMAIL.mask(email));
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            log.info("User creation rejected: event=PHONE_ALREADY_EXISTS, phone={}", MaskType.PHONE.mask(phoneNumber));
            throw new PhoneAlreadyExistsException();
        }
    }

    private void checkPhoneNumberAvailability(String phoneNumber, UUID userId) {
        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId)) {
            log.info("Profile update rejected: event=PHONE_ALREADY_EXISTS, phone={}", MaskType.PHONE.mask(phoneNumber));
            throw new PhoneAlreadyExistsException();
        }
    }
}
