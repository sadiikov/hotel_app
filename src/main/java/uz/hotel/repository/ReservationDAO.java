package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.dto.ReservationDTO;
import uz.hotel.entity.Reservation;
import uz.hotel.entity.User;
import uz.hotel.entity.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservationDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<ReservationDTO> findAll(){
        String sql = """
                SELECT r.id, r.user_id, r.room_id, r.check_in, r.check_out, r.status, r.total_price,
                                         rm.number AS room_number, h.name AS hotel_name
                                  FROM reservations r
                                  JOIN rooms rm ON r.room_id = rm.id
                                  JOIN hotels h ON rm.hotel_id = h.id
                                  ORDER BY r.id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReservationDTO info = new ReservationDTO();
            info.setId(rs.getInt("id"));
            info.setUserId(rs.getInt("user_id"));
            info.setRoomId(rs.getInt("room_id"));
            info.setStartDate(rs.getDate("start_date").toLocalDate());
            info.setEndDate(rs.getDate("end_date").toLocalDate());
            info.setStatus(rs.getString("status"));
            info.setTotalPrice(rs.getDouble("total_price"));
            info.setHotelName(rs.getString("hotel_name"));
            info.setRoomNumber(rs.getString("room_number"));
            return info;
        });
    }

    public List<ReservationDTO> findAllPendingsAndCancels(){
        String sql = """
        SELECT r.id, r.user_id, r.room_id, r.check_in, r.check_out, r.status, r.total_price,
                                         rm.number AS room_number, h.name AS hotel_name
                                  FROM reservations r
                                  JOIN rooms rm ON r.room_id = rm.id
                                  JOIN hotels h ON rm.hotel_id = h.id
                                  WHERE r.status = 'PENDING' AND r.status = 'CANCELED_BY_USER'
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReservationDTO info = new ReservationDTO();
            info.setId(rs.getInt("id"));
            info.setUserId(rs.getInt("user_id"));
            info.setRoomId(rs.getInt("room_id"));
            info.setStartDate(rs.getDate("start_date").toLocalDate());
            info.setEndDate(rs.getDate("end_date").toLocalDate());
            info.setStatus(rs.getString("status"));
            info.setTotalPrice(rs.getDouble("total_price"));
            info.setHotelName(rs.getString("hotel_name"));
            info.setRoomNumber(rs.getString("room_number"));
            return info;
        });
    }

    public List<ReservationDTO> findAllPendingReservationsWithDetails() {
        String sql = """
        SELECT r.id, r.user_id, r.room_id, r.check_in, r.check_out, r.status, r.total_price,
                                         rm.number AS room_number, h.name AS hotel_name
                                  FROM reservations r
                                  JOIN rooms rm ON r.room_id = rm.id
                                  JOIN hotels h ON rm.hotel_id = h.id
                                  WHERE r.status = 'PENDING' AND r.status = 'ACCEPTED'
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReservationDTO info = new ReservationDTO();
            info.setId(rs.getInt("id"));
            info.setUserId(rs.getInt("user_id"));
            info.setRoomId(rs.getInt("room_id"));
            info.setStartDate(rs.getDate("start_date").toLocalDate());
            info.setEndDate(rs.getDate("end_date").toLocalDate());
            info.setStatus(rs.getString("status"));
            info.setTotalPrice(rs.getDouble("total_price"));
            info.setHotelName(rs.getString("hotel_name"));
            info.setRoomNumber(rs.getString("room_number"));
            return info;
        });
    }

    public void updateStatus(int reservationId, String status) {
        jdbcTemplate.update("UPDATE reservations SET status = ? WHERE id = ?", status, reservationId);
    }

    public Optional<User> findUserByReservationId(int reservationId) {
        String sql = """
        SELECT u.id, u.name, u.email, u.balance
        FROM users u
        JOIN reservations r ON u.id = r.user_id
        WHERE r.id = ?
    """;

        List<User> users = jdbcTemplate.query(sql, new Object[]{reservationId}, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setBalance(rs.getDouble("balance")); // если поле есть
            return user;
        });

        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Reservation findReservationById(int reservationId) {
        String sql = "SELECT * FROM reservations WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{reservationId}, (rs, rowNum) -> {
            Reservation reservation = new Reservation();
            reservation.setId(rs.getInt("id"));
            reservation.setUserId(rs.getInt("user_id"));
            reservation.setRoomId(rs.getInt("room_id"));
            reservation.setCheckIn(rs.getTimestamp("check_in"));
            reservation.setCheckOut(rs.getTimestamp("check_out"));
            reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
            reservation.setTotalPrice(rs.getDouble("total_price"));
            return reservation;
        });
    }
}
