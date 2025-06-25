package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.Room;
import uz.hotel.entity.enums.RoomStatus;
import uz.hotel.entity.enums.RoomType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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
}

