package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.AdminUserCreateForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.entity.User.AccountStatus;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordUtil passwordUtil;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordUtil = passwordUtil;
    }

    @Transactional
    public User register(RegisterForm form) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role candidateRole = roleRepository.findByName(Role.CANDIDATE)
                .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo vai trò CANDIDATE"));
        User user = new User(
                username,
                passwordUtil.hash(form.getPassword()),
                email,
                form.getFullName().trim(),
                candidateRole
        );
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) {
            return Optional.empty();
        }
        String login = usernameOrEmail.trim();
        if (login.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> user = userRepository.findByUsernameWithRole(login);
        if (user.isEmpty()) {
            user = userRepository.findByEmailWithRole(login);
        }

        return user
                .filter(User::isEnabled)
                .filter(account -> AccountStatus.ACTIVE.equals(account.getAccountStatus()))
                .filter(account -> passwordUtil.matches(rawPassword, account.getPassword()));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllWithRole();
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String search, String roleName, AccountStatus accountStatus) {
        return userRepository.searchUsers(normalizeBlank(search), normalizeBlank(roleName), accountStatus);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> findStaffCreationRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> Role.RECRUITER.equals(role.getName()) || Role.INTERVIEWER.equals(role.getName()))
                .toList();
    }

    @Transactional
    public User createStaffAccount(AdminUserCreateForm form) {
        String roleName = normalizeBlank(form.getRoleName());
        if (!Role.RECRUITER.equals(roleName) && !Role.INTERVIEWER.equals(roleName)) {
            throw new IllegalArgumentException("Role must be HR Manager or Interviewer.");
        }

        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role must be HR Manager or Interviewer."));
        User user = new User(
                username,
                passwordUtil.hash(form.getInitialPassword()),
                email,
                form.getFullName().trim(),
                role
        );
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public User updateRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleEnabled(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public User deactivateUser(Long userId) {
        User user = findById(userId);
        if (isLastActiveAdmin(user)) {
            throw new IllegalArgumentException("The last active admin account cannot be deactivated.");
        }
        user.setAccountStatus(AccountStatus.INACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public User unlockUser(Long userId) {
        User user = findById(userId);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean isLastActiveAdmin(User user) {
        return user != null
                && user.getRole() != null
                && Role.ADMIN.equals(user.getRole().getName())
                && AccountStatus.ACTIVE.equals(user.getAccountStatus())
                && userRepository.countByRole_NameAndAccountStatus(Role.ADMIN, AccountStatus.ACTIVE) <= 1;
    }

    @Transactional
    public void delete(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
