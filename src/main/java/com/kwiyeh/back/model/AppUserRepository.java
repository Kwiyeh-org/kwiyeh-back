package com.kwiyeh.back.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface  AppUserRepository extends JpaRepository<AppUser, Long>{
    AppUser findByUid(String uid);
    AppUser findByEmail(String email);
}
