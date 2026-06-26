package com.roommatefinder.repository;

import com.roommatefinder.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT p FROM Profile p WHERE p.city = :city AND p.user.id != :userId AND p.user.enabled = true")
    List<Profile> findByCityAndUserIdNot(@Param("city") String city, @Param("userId") Long userId);

    @Query("SELECT p FROM Profile p WHERE p.user.enabled = true AND p.user.id != :userId")
    List<Profile> findAllExceptUser(@Param("userId") Long userId);

    @Query("SELECT p FROM Profile p WHERE p.user.enabled = true AND p.user.id != :userId " +
           "AND (:city IS NULL OR LOWER(p.city) = LOWER(:city)) " +
           "AND (:budgetMax IS NULL OR p.budgetMin <= :budgetMax) " +
           "AND (:budgetMin IS NULL OR p.budgetMax >= :budgetMin) " +
           "AND (:sleepSchedule IS NULL OR p.sleepSchedule = :sleepSchedule)")
    List<Profile> findWithFilters(
            @Param("userId") Long userId,
            @Param("city") String city,
            @Param("budgetMin") Integer budgetMin,
            @Param("budgetMax") Integer budgetMax,
            @Param("sleepSchedule") Profile.SleepSchedule sleepSchedule);
}
