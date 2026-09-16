package com.examplatform.modules.user.repository;

import com.examplatform.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, User.AuthProvider authProvider);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByCreatedAtAfter(LocalDateTime dateTime);
    long countByIsActive(boolean isActive);

    List<User> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "u.phone LIKE CONCAT('%', :kw, '%')")
    List<User> searchUsers(@Param("kw") String kw);



    @Query("SELECT u.id FROM User u")
    java.util.List<String> findAllUserIds();

    @Query("SELECT u.id FROM User u WHERE u.educationLevel = ?1")
    java.util.List<String> findUserIdsByEducationLevel(User.EducationLevel educationLevel);

    @Query("SELECT u.id FROM User u WHERE u.targetExam = ?1")
    java.util.List<String> findUserIdsByTargetExam(User.TargetExam targetExam);
}
