package com.empathai.rewards.repository;

import com.empathai.rewards.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByTriggerTypeAndTriggerTitle(String triggerType, String triggerTitle);
}