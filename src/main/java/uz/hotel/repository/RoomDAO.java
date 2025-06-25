package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.Reservation;
import uz.hotel.entity.Room;
import uz.hotel.entity.enums.ReservationStatus;
import uz.hotel.entity.enums.RoomStatus;
import uz.hotel.entity.enums.RoomType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoomDAO {
    private final JdbcTemplate jdbcTemplate;

    public void saveRooms(List<Room> rooms, int hotelId) {
        String sql = "INSERT INTO rooms (hotel_id, number, type, price, description, rating) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Room room = rooms.get(i);
                ps.setInt(1, hotelId);
                ps.setString(2, room.getNumber());
                ps.setString(3, room.getType().name());
                ps.setDouble(4, room.getPrice());
                ps.setString(5, room.getDescription());
                ps.setObject(6, room.getRating());
            }

            @Override
            public int getBatchSize() {
                return rooms.size();
            }
        });
    }

    public List<Room> findByHotelId(int hotelId) {
        String sql = "SELECT * FROM rooms WHERE hotel_id = ? order by number";
        return jdbcTemplate.query(sql, new Object[]{hotelId}, (rs, rowNum) -> {
            Room room = new Room();
            room.setId(rs.getInt("id"));
            room.setHotelId(rs.getInt("hotel_id"));
            room.setNumber(rs.getString("number"));
            room.setType(RoomType.valueOf(rs.getString("type")));
            room.setPrice(rs.getDouble("price"));
            room.setDescription(rs.getString("description"));
            room.setRating(rs.getDouble("rating"));
            room.setStatus(RoomStatus.valueOf(rs.getString("status")));
            return room;
        });
    }

    public void updateRoom(Room room) {
        String sql = """
                UPDATE rooms
                SET number = ?, type = ?, price = ?, description = ?, rating = ?, status = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                room.getNumber(),
                room.getType().name(),
                room.getPrice(),
                room.getDescription(),
                room.getRating(),
                room.getStatus().name(),
                room.getId());
    }

    public void deleteByHotelId(int hotelId) {
        jdbcTemplate.update("DELETE FROM rooms WHERE hotel_id = ?", hotelId);
    }

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
