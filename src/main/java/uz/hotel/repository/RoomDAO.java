package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.Reservation;
import uz.hotel.entity.Room;
import uz.hotel.entity.enums.ReservationStatus;
import uz.hotel.entity.enums.RoomStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RoomDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<Room> getRoomsByHotelId(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM rooms WHERE hotel_id = ? and status = ?",
                new Object[]{id, RoomStatus.ACTIVE.name()},
                BeanPropertyRowMapper.newInstance(Room.class)
        );
    }

    public Optional<Room> getRoomById(Long id) {
        List<Room> query = jdbcTemplate.query("select * from rooms where id = ?", new Object[]{id}, BeanPropertyRowMapper.newInstance(Room.class));
        if (query.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(query.get(0));
    }

    public Optional<Room> getRoomByIdAndTime(Long id, LocalDateTime checkIn, LocalDateTime checkOut) {
        String sql = """
                    select * from rooms r
                    where r.id = ?
                    and not exists (
                        select 1 from reservations rv
                        where rv.room_id = r.id
                        and rv.status in ('CONFIRMED', 'PENDING') 
                        and rv.check_out > ? and rv.check_in < ?
                    )
                """;
        List<Room> query = jdbcTemplate.query(sql, new Object[]{id, checkIn, checkOut}, BeanPropertyRowMapper.newInstance(Room.class));
        return query.isEmpty() ? Optional.empty() : Optional.of(query.get(0));
    }

    public List<Reservation> getMyOrders(int id) {
        String sql = """
                select * from reservations where user_id = ? and status in (?, ?)
                """;
        return jdbcTemplate.query(sql, new Object[]{id, ReservationStatus.PENDING.name(), ReservationStatus.CLOSED.name()}, BeanPropertyRowMapper.newInstance(Reservation.class));

    }

    public Long getHotelIdByRoomId(Long roomId) {
        String sql = "SELECT hotel_id FROM rooms WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{roomId}, Long.class);
    }
}
