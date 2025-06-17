package uz.hotel.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Review {
    private int id;
    private int userId;
    private int roomId;
    private int rating; // e.g. 1 to 5
    private String text;
    private Timestamp createdAt;
}

