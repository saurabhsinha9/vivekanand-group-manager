
package com.vivekanand.manager.members;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Page<Member> findByActiveTrue(Pageable pageable);
    Page<Member> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    List<Member> findByActiveTrue();
    Optional<Member> findByEmailIgnoreCase(String email);
    Optional<Member> findByPhone(String phone);
}
