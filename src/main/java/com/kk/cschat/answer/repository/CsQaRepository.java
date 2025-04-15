package com.kk.cschat.answer.repository;

import com.kk.cschat.answer.entity.CsQa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsQaRepository extends JpaRepository<CsQa, Long> {
    Optional<CsQa> findByKeywordIgnoreCase(String keyword);
}
