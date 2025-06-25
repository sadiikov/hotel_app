package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.dto.ReservationHistoryDTO;
import uz.hotel.entity.Reservation;
import uz.hotel.entity.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class HistoryDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<ReservationHistoryDTO> getAllHistories(Long userId) {
        String sql = """
        SELECT r.id, r.user_id, r.room_id, r.check_in, r.check_out, r.status,
               h.name AS hotel_name
        FROM reservations r
        JOIN rooms rm ON r.room_id = rm.id
        JOIN hotels h ON rm.hotel_id = h.id
        WHERE r.user_id = ? AND r.status IN (?, ?, ?)
    """;
        return jdbcTemplate.query(
                sql,
                new Object[]{userId, ReservationStatus.CLOSED.name(), ReservationStatus.ACCEPTED.name(), ReservationStatus.CANCELLED_BY_USER.name()},
                new BeanPropertyRowMapper<>(ReservationHistoryDTO.class)
        );
    }
}
