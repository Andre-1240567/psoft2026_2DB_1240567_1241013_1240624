package pt.isep.psoft.alsafe.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.alsafe.security.domain.SystemUser;

import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    Optional<SystemUser> findByUsername(String username);
}