package com.avbinvest.user.repository;

import com.avbinvest.user.module.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> getUserById(Long id);
    Page<User> findAllByIdIn(List<Long> ids, Pageable pageable);
    User findUserByPhoneNumber(String phoneNumber);
}
