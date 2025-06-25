package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.Hotel;
import uz.hotel.entity.Reservation;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class HotelDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = jdbcTemplate.query("select * from hotels order by id", BeanPropertyRowMapper.newInstance(Hotel.class));
        return hotels;
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
}
