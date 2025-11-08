package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository("userDbRepository")
@Primary
@RequiredArgsConstructor
public class UserDbRepository implements UserRepository {

    private final UserJpaRepository jpa;

    @Override
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public User update(User user) {
        // для JPA update == save
        return jpa.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<User> findAll() {
        // чтобы сохранить сортировку по id
        return jpa.findAll(jpa.sortByIdAsc());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email, Long excludeId) {
        return jpa.existsByEmailCaseInsensitive(email, excludeId);
    }
}
