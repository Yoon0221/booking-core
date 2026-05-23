package io.jiyoon.booking_core.domain.point.repository;

import io.jiyoon.booking_core.domain.point.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
}