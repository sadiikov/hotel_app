package uz.hotel.dto;

import lombok.Data;
import uz.hotel.entity.Hotel;

import java.util.List;

@Data
public class HotelDTO {
    private int id;
    private String name;
    private String country;
    private String city;
    private String location;
    private Double rating;

    private int standartCount;
    private int luxeCount;
    private Double standartPrice;
    private Double luxePrice;
    private String standartDescription;
    private String luxeDescription;

    private List<RoomDTO> rooms;

    public Hotel toHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(this.id);
        hotel.setName(this.name);
        hotel.setCountry(this.country);
        hotel.setCity(this.city);
        hotel.setLocation(this.location);
        hotel.setRating(this.rating != null ? this.rating : 0.0);
        return hotel;
    }

    // ✅ Constructor for programmatic use (not for JSON deserialization)
    public HotelDTO(Hotel hotel, List<RoomDTO> rooms) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.country = hotel.getCountry();
        this.city = hotel.getCity();
        this.location = hotel.getLocation();
        this.rating = hotel.getRating();
        this.rooms = rooms;

        // You can optionally fill other fields based on room analysis:
        this.standartCount = (int) rooms.stream().filter(r -> r.getType().equals("STANDARD")).count();
        this.luxeCount = (int) rooms.stream().filter(r -> r.getType().equals("LUXE")).count();
    }

    // No-args constructor is still needed by Spring
    public HotelDTO() {}
}