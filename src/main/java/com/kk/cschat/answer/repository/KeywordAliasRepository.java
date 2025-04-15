package com.kk.cschat.answer.repository;

import com.kk.cschat.answer.entity.KeywordAlias;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordAliasRepository extends JpaRepository<KeywordAlias, Long> {
    Optional<KeywordAlias> findByAliasIgnoreCase(String alias);
}
