package uz.hotel.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.User;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDAO {
    final JdbcTemplate jdbcTemplate;
    public void saveUser(User user){
        jdbcTemplate.update("insert into users(name,email,password,role) values(?,?,?,?)",user.getName(),user.getEmail(),user.getPassword(),user.getRole());
    }

    public Optional<User> getUserByEmail(String email) {
        List<User> users = jdbcTemplate.query("select * from users where email = ?", BeanPropertyRowMapper.newInstance(User.class), email);
        if (users.size()!=1){
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(0));
    }

    public Optional<User> findById(Integer id) {
        List<User> users = jdbcTemplate.query("select * from users where id = ?", BeanPropertyRowMapper.newInstance(User.class), id);
        if (users.size()!=1){
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(0));
    }

    public void editUser(User currentUser) {
        jdbcTemplate.update("update users set email = ?, fullname = ?, role = ? where id = ?",currentUser.getEmail(),currentUser.getName(),currentUser.getRole(),currentUser.getId());
    }

    public List<User> getAllUsers() {
        return jdbcTemplate.query("select * from users order by id", BeanPropertyRowMapper.newInstance(User.class));
    }
}
