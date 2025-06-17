package uz.hotel.entity;

import lombok.Data;

@Data
public class MenuItem {
    private int id;
    private String name;
    private String description;
    private String photoPath; // e.g. "/uploads/burger.jpg"
    private Double price;
}

