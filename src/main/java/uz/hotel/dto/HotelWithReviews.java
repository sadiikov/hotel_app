package uz.hotel.dto;

import lombok.Data;
import uz.hotel.entity.Hotel;
import uz.hotel.entity.Review;

import java.util.List;

@Data
public class HotelWithReviews {
    private Hotel hotel;
    private List<Review> reviews;
    public HotelWithReviews(Hotel hotel, List<Review> reviews) {
        this.hotel = hotel;
        this.reviews = reviews;
    }
}
