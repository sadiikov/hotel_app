package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import uz.hotel.entity.Hotel;
import uz.hotel.entity.User;
import uz.hotel.entity.Reservation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HotelDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = jdbcTemplate.query("select * from hotels order by id", BeanPropertyRowMapper.newInstance(Hotel.class));
        return hotels;
    }
    public int saveHotelAndReturnId(Hotel hotel) {
        String sql = "INSERT INTO hotels(name, country, city, location, rating) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, hotel.getName());
            ps.setString(2, hotel.getCountry());
            ps.setString(3, hotel.getCity());
            ps.setString(4, hotel.getLocation());
            ps.setObject(5, hotel.getRating());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        return (Integer) keys.get("id"); // <-- safer than getKey()
    }

    public List<Hotel> findAll() {
        String sql = "SELECT * FROM hotels order by id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Hotel hotel = new Hotel();
            hotel.setId(rs.getInt("id"));
            hotel.setName(rs.getString("name"));
            hotel.setCountry(rs.getString("country"));
            hotel.setCity(rs.getString("city"));
            hotel.setLocation(rs.getString("location"));
            hotel.setRating(rs.getDouble("rating"));
            return hotel;
        });
    }

    public Optional<Hotel> getHotelByReservation(Reservation closedOrder) {
        String sql = "SELECT h.* " +
                "FROM hotels h " +
                "JOIN rooms r ON h.id = r.hotel_id " +
                "JOIN reservations rn ON rn.room_id = r.id " +
                "WHERE rn.id = ?";
        List<Hotel> hotels = jdbcTemplate.query(sql, new Object[]{closedOrder.getId()}, BeanPropertyRowMapper.newInstance(Hotel.class));
        if(hotels.isEmpty()){
            throw new RuntimeException("hotels not found");
        }
        return hotels.isEmpty() ? Optional.empty() : Optional.ofNullable(hotels.get(0));
    }

    public void update(Hotel hotel) {
        String sql = "UPDATE hotels SET name = ?, country = ?, city = ?, location = ?, rating = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                hotel.getName(),
                hotel.getCountry(),
                hotel.getCity(),
                hotel.getLocation(),
                hotel.getRating(),
                hotel.getId());
    }

    public void deleteById(int id) {
        jdbcTemplate.update("DELETE FROM hotels WHERE id = ?", id);
    }


}
