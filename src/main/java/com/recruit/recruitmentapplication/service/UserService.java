package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
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
    public Optional<User> authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByUsernameWithRole(username.trim())
                .filter(User::isEnabled)
                .filter(user -> passwordUtil.matches(rawPassword, user.getPassword()));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllWithRole();
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
    public void delete(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }
}
