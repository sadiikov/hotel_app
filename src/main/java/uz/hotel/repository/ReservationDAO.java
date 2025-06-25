package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
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

//    public void cancelOrderById(Long id) {
//        String sql = "select * from reservations where id = ?";
//        List<Reservation> orders = jdbcTemplate.query(sql, new Object[]{id}, BeanPropertyRowMapper.newInstance(Reservation.class));
//        if(orders.isEmpty()){
//            throw new RuntimeException("Reservation not found");
//        }
//        Reservation reservation = orders.get(0);
//
//        System.out.println("DB status = '" + reservation.getStatus() + "'");
//        System.out.println("Enum status = '" + ReservationStatus.PENDING.name() + "'");
//
//        if(reservation.getStatus() == ReservationStatus.PENDING){
//            User user = jdbcTemplate.queryForObject("select * from users where id = ?", new Object[]{reservation.getUserId()}, BeanPropertyRowMapper.newInstance(User.class));
//
//            Room room = jdbcTemplate.queryForObject("select * from rooms where id = ?", new Object[]{reservation.getRoomId()}, BeanPropertyRowMapper.newInstance(Room.class));
//
//            double refundAmount = room.getPrice() * 0.9; // refund 90%
//            double adminFee = room.getPrice() * 0.1;
//
//            Double newBalance = user.getBalance()+refundAmount;
//            jdbcTemplate.update("update users set balance = ? and where id = ?", newBalance, user.getId());
//            jdbcTemplate.update("update reservations set status = ? and where id = ?", ReservationStatus.CLOSED.name(), id);
//        } else if (reservation.getStatus().equals(ReservationStatus.ACCEPTED)) {
//            jdbcTemplate.update("update reservations set status = ? and id = ?", ReservationStatus.CLOSED.name(), id);
//        }else {
//            throw new RuntimeException("Reservation cannot be canceled in its current status");
//        }
//
//
//    }

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
