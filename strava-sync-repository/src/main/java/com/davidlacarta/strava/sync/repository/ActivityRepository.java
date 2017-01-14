package com.davidlacarta.strava.sync.repository;

import com.davidlacarta.strava.sync.model.domain.Activity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Activity Repository
 */
public interface ActivityRepository extends JpaRepository<Activity, String> {
}
