package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.User;
import uz.hotel.entity.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDAO {
    final JdbcTemplate jdbcTemplate;

    public Optional<User> getUserByEmailAndPassword(String email, String password) {
       return jdbcTemplate.query("select * from users where email = ? and password = ?", new Object[]{email, password},
                (rs, rowNum) ->{
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                    user.setBalance(rs.getDouble("balance"));
                    return user;
                }).stream().findFirst();
    }
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{id}, BeanPropertyRowMapper.newInstance(User.class));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> getUserByEmail(String email) {
        return jdbcTemplate.query("select * from users where email = ?", new Object[]{email},
                (rs, rowNum) ->{
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                    user.setBalance(rs.getDouble("balance"));
                    return user;
                }).stream().findFirst();
    }

    public void saveUser(User user) {
        jdbcTemplate.update("insert into users(name, email, password, role, balance) values (?,?,?,?,?)",
                            user.getName(), user.getEmail(), user.getPassword(), user.getRole().name(), user.getBalance());
    }

    public void updateUserBalance(int id, Double balance) {
         jdbcTemplate.update("update users set balance = balance + ? where id = ?", balance, id);
    }
}


