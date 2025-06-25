package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.dto.ReservationDTO;
import uz.hotel.entity.Reservation;
import uz.hotel.entity.Room;
import uz.hotel.entity.User;
import uz.hotel.entity.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservationDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<ReservationDTO> findAll() {
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
            info.setStartDate(rs.getDate("check_in").toLocalDate());   // <-- fixed
            info.setEndDate(rs.getDate("check_out").toLocalDate());    // <-- fixed
            info.setStatus(rs.getString("status"));
            info.setTotalPrice(rs.getDouble("total_price"));
            info.setHotelName(rs.getString("hotel_name"));
            info.setRoomNumber(rs.getString("room_number"));
            return info;
        });
    }


    public List<ReservationDTO> findAllPendingsAndCancels() {
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
        WHERE r.status IN (?, ?)
    """;

        return jdbcTemplate.query(
                sql,
                new Object[]{ReservationStatus.PENDING.name(), ReservationStatus.ACCEPTED.name()},
                BeanPropertyRowMapper.newInstance(ReservationDTO.class)
        );
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

        return jdbcTemplate.queryForObject(sql, new Object[]{reservationId}, BeanPropertyRowMapper.newInstance(Reservation.class));
    }

    public void save(Reservation reservation) {
        jdbcTemplate.update("insert into reservations(user_id, room_id, check_in, check_out, status, created_at, total_price) " +
                        "values (?,?,?,?,?,?,?)",
                new Object[]{reservation.getUserId(),
                        reservation.getRoomId(),
                        reservation.getCheckIn(),
                        reservation.getCheckOut(),
                        reservation.getStatus().name(),
                        reservation.getCreatedAt(),
                        reservation.getTotalPrice()});
    }

    public void cancelOrderById(Long id) {
        String sql = "select * from reservations where id = ?";
        List<Reservation> orders = jdbcTemplate.query(sql, new Object[]{id}, BeanPropertyRowMapper.newInstance(Reservation.class));
        if(orders.isEmpty()){
            throw new RuntimeException("Reservation not found");
        }
        Reservation reservation = orders.get(0);
        if(reservation.getStatus() == ReservationStatus.PENDING){
            jdbcTemplate.update("update reservations set status = ? where id = ?", ReservationStatus.CANCELLED_BY_USER.name(), id);
        }else {
            throw new RuntimeException("Reservation cannot be canceled in its current status");
        }
    }

    public List<Reservation> getClosedOrders(int id) {
        String sql = "select * from reservations where status = ? and user_id = ?";
        return jdbcTemplate.query(
                sql, new Object[]{ReservationStatus.CLOSED.name(), id},
                BeanPropertyRowMapper.newInstance(Reservation.class));
    }

    public Optional<Reservation> findClosedByHotelIdAndUserId(Long hotelId, Long userId) {
        String sql = "SELECT rn.* FROM reservations rn " +
                "JOIN room r ON rn.room_id = r.id " +
                "WHERE rn.user_id = ? AND rn.status = 'CLOSED' AND r.hotel_id = ? " +
                "ORDER BY rn.created_at DESC LIMIT 1";
        List<Reservation> list = jdbcTemplate.query(sql,
                new Object[]{userId, hotelId},
                BeanPropertyRowMapper.newInstance(Reservation.class));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }



    public void updateStatusAfterComment(Long reservationId) {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, ReservationStatus.COMMENTED_BY_USER.name(), reservationId);
    }
}
