package uz.hotel.entity;

import lombok.Data;
import uz.hotel.entity.enums.RoomType;

@Data
public class Room {
    private int id;
    private int hotelId; // new field to connect to Hotel
    private String number;
    private RoomType type;
    private Double price;
    private String description;
    private Double rating; // average rating from reviews
}
