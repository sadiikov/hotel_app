package uz.hotel.entity;

import lombok.Data;

@Data
public class Hotel {
    private int id;
    private String name;
    private String country;
    private String city;
    private String location;
    private String imageUrl;
    private Double rating; // average rating of the hotel
}
