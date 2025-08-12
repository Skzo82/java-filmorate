package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger();

    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(idGenerator.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    public void deleteAll() {
        users.clear();
        idGenerator.set(0);
    }

    // --- Управление друзьями (односторонняя дружба) ---

    @Override
    public void addFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = findById(userId);
        user.getFriends().remove(friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = findById(userId);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends()) {
            friends.add(findById(friendId));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherUserId) {
        User user = findById(userId);
        User other = findById(otherUserId);

        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());

        List<User> commonFriends = new ArrayList<>();
        for (Integer id : commonIds) {
            commonFriends.add(findById(id));
        }
        return commonFriends;
    }
}
