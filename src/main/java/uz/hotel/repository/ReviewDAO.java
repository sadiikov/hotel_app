package uz.hotel.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hotel.entity.Review;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ReviewDAO {
    private final JdbcTemplate jdbcTemplate;

    public void save(Review review) {
        String sql = "INSERT INTO reviews (user_id, reservation_id, hotel_id, text, rating, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                review.getUserId(),
                review.getReservationId(),
                review.getHotelId(),
                review.getText(),
                review.getRating(),
                review.getCreatedAt()
        );
    }
    public List<Review> getReviewsByHotelId(Long hotelId) {
        String sql = "SELECT * FROM reviews WHERE hotel_id = ?";
        return jdbcTemplate.query(sql, new Object[]{hotelId}, new BeanPropertyRowMapper<>(Review.class));
    }

}
