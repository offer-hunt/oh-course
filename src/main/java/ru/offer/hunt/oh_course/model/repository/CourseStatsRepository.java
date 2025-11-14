package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.CourseStats;

import java.util.UUID;

public interface CourseStatsRepository extends JpaRepository<CourseStats, UUID> {}
