package com.faforever.server.security;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UidExemptRepository extends JpaRepository<UniqueIdExempt, Integer> {
}
