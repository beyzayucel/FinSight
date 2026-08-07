package com.akademi.finsight.user.service.impl;

import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.service.AuditLogService;
import com.akademi.finsight.auth.refreshtoken.service.RefreshTokenService;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.user.dto.request.CreateUserRequest;
import com.akademi.finsight.user.dto.request.UpdateUserRequest;
import com.akademi.finsight.user.dto.response.UserResponse;
import com.akademi.finsight.user.dto.response.UserStatsResponse;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.entity.Role;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.mapper.UserMapper;
import com.akademi.finsight.user.repository.UserRepository;
import com.akademi.finsight.user.repository.UserSpecification;
import com.akademi.finsight.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.akademi.finsight.user.util.CredentialsGenerator;
import com.akademi.finsight.user.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService tokenService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String search, Boolean enabled, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecification.hasSearch(search))
                .and(UserSpecification.hasEnabled(enabled));

        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void createUser(CreateUserRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        checkDuplicateUser(normalizedEmail, request.phoneNumber());

        String username = generateUniqueUsername(request.firstName(), request.lastName());
        String temporaryPassword = CredentialsGenerator.generateTemporaryPassword();

        User user = userMapper.toEntity(request);
        user.setEmail(normalizedEmail);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(temporaryPassword));

        User savedUser = userRepository.save(user);
        tokenService.createAndSendVerificationToken(savedUser, temporaryPassword);
        auditLogService.createAuditLogForAdmin(AuditActionType.USER_CREATED, savedUser);
        log.info("User created: event=USER_CREATED, email={}", MaskType.EMAIL.mask(savedUser.getEmail()));
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
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findByIdOrThrow(id);

        checkPhoneNumberAvailability(request.phoneNumber(), user.getId());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());

        User savedUser = userRepository.save(user);
        auditLogService.createAuditLogForAdmin(AuditActionType.USER_UPDATED, savedUser);
        log.info("User updated by admin: event=USER_UPDATED, email={}", MaskType.EMAIL.mask(savedUser.getEmail()));
        return userMapper.toResponse(savedUser);
    }

    private void checkPhoneNumberAvailability(String phoneNumber, UUID userId) {
        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId)) {
            log.info("Profile update rejected: event=PHONE_ALREADY_EXISTS, phone={}", MaskType.PHONE.mask(phoneNumber));
            throw new UserException(UserErrorType.PHONE_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    public void changeUserStatus(UUID id, boolean enabled, String currentUserEmail) {
        User user = findByIdOrThrow(id);
        validateAdminAction(user, currentUserEmail);
        if (user.isEnabled() == enabled) {
            throw new UserException(UserErrorType.USER_STATUS_UNCHANGED);
        }
        user.setEnabled(enabled);
        userRepository.save(user);
        auditLogService.createAuditLogForAdmin(
                enabled ? AuditActionType.USER_ACTIVATED : AuditActionType.USER_DEACTIVATED, user);
        log.info("User status changed: event=USER_STATUS_CHANGED, email={}, enabled={}", MaskType.EMAIL.mask(user.getEmail()), enabled);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id, String currentUserEmail) {
        User user = findByIdOrThrow(id);
        validateAdminAction(user, currentUserEmail);
        auditLogService.createAuditLogForAdmin(AuditActionType.USER_DELETED, user);
        refreshTokenService.revokeAllByUser(user);
        userRepository.delete(user);
        log.info("User deleted by admin: event=USER_DELETED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    private void validateAdminAction(User targetUser, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(EmailNormalizer.normalize(currentUserEmail))
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
        if (targetUser.getId().equals(currentUser.getId())) {
            throw new UserException(UserErrorType.SELF_ACTION_NOT_ALLOWED);
        }
        if (targetUser.getRole() == Role.ADMIN) {
            throw new UserException(UserErrorType.ADMIN_PROTECTED);
        }
    }


    private User findByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByIdentifier(String identifier) {
        String normalized = identifier.contains("@")
                ? EmailNormalizer.normalize(identifier)
                : identifier;
        return userRepository.findByIdentifier(normalized)
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
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
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
       return new UserStatsResponse(
                userRepository.count(),
               userRepository.countByEnabled(true),
               userRepository.countByEnabled(false),
               userRepository.countByLastLoginAtAfter(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())

        );
    }

    @Override
    @Transactional
    public void resendVerification(UUID id) {
        User user = findByIdOrThrow(id);

        if (user.isEmailVerified()) {
            throw new UserException(UserErrorType.EMAIL_ALREADY_VERIFIED);
        }

        String temporaryPassword = CredentialsGenerator.generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        tokenService.resendVerificationToken(user, temporaryPassword);
        auditLogService.createAuditLogForAdmin(AuditActionType.VERIFICATION_RESENT, user);
        log.info("Verification resent by admin: event=VERIFICATION_RESENT, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    private void checkDuplicateUser(String email, String phoneNumber) {
        if (userRepository.existsByEmail(email)) {
            log.info("User creation rejected: event=EMAIL_ALREADY_EXISTS, email={}", MaskType.EMAIL.mask(email));
            throw new UserException(UserErrorType.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            log.info("User creation rejected: event=PHONE_ALREADY_EXISTS, phone={}", MaskType.PHONE.mask(phoneNumber));
            throw new UserException(UserErrorType.PHONE_ALREADY_EXISTS);
        }
    }


}
