package com.davidlacarta.strava.sync.repository;

import com.davidlacarta.strava.sync.model.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User Repository
 */
public interface UserRepository extends JpaRepository<User, String> {

    User findByIdStrava(String idStrava);

    User findByCodeStrava(String codeStrava);
}
