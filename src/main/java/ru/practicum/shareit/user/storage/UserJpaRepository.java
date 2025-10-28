package ru.practicum.shareit.user.storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.user.model.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
           select (count(u) > 0) from User u
           where lower(u.email) = lower(:email)
             and (:excludeId is null or u.id <> :excludeId)
           """)
    boolean existsByEmailCaseInsensitive(@Param("email") String email,
                                         @Param("excludeId") Long excludeId);

    default Sort sortByIdAsc() {
        return Sort.by(Sort.Direction.ASC, "id");
    }
}
