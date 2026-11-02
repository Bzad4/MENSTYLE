package com.menstyle.service;

import com.menstyle.dto.request.ChangePasswordRequest;
import com.menstyle.dto.request.UpdateAdminProfileRequest;
import com.menstyle.entity.Role;
import com.menstyle.entity.User;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private AdminAccountService service;
    private User admin;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        service = new AdminAccountService(userRepository, passwordEncoder);
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        admin = new User();
        admin.setEmail("admin@menstyle.test");
        admin.setFullName("Admin");
        admin.setPasswordHash("old-hash");
        admin.setRole(role);
        authentication = new UsernamePasswordAuthenticationToken(admin.getEmail(), null);
        lenient().when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
    }

    @Test
    void updatesOnlyEditableProfileFields() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateProfile(authentication,
                new UpdateAdminProfileRequest("  Updated Admin  ", " 0900000000 "));

        assertEquals("Updated Admin", result.fullName());
        assertEquals("0900000000", result.phone());
        assertEquals("admin@menstyle.test", result.email());
        verify(userRepository).save(admin);
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> service.changePassword(authentication,
                new ChangePasswordRequest("wrong", "new-password", "new-password")));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changesPasswordOnlyAfterCurrentPasswordAndConfirmationPass() {
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword(authentication,
                new ChangePasswordRequest("old-password", "new-password", "new-password"));

        assertEquals("new-hash", admin.getPasswordHash());
        verify(userRepository).save(admin);
    }

    @Test
    void rejectsPasswordConfirmationMismatch() {
        assertThrows(BadRequestException.class, () -> service.changePassword(authentication,
                new ChangePasswordRequest("old-password", "new-password", "different-password")));
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void restoresLockedAdminAccountWithCorrectRoleNameAndNormalizedEmail() {
        AdminOperationsService operations = new AdminOperationsService(null, null, null, null, null, null,
                userRepository, mock(com.menstyle.repository.RoleRepository.class), passwordEncoder);
        User staff = new User();
        staff.setId(2L);
        staff.setFullName("Staff");
        staff.setEmail("staff@example.com");
        Role staffRole = new Role();
        staffRole.setName("ROLE_STAFF");
        staff.setRole(staffRole);
        staff.setStatus(com.menstyle.entity.enums.UserStatus.LOCKED);
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(staff));
        when(userRepository.save(staff)).thenReturn(staff);

        var result = operations.staffStatus(2L, com.menstyle.entity.enums.UserStatus.ACTIVE, admin.getEmail());

        assertEquals(com.menstyle.entity.enums.UserStatus.ACTIVE, result.status());
        verify(userRepository).findByIdForUpdate(2L);
    }
}
