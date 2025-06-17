package uz.hotel.entity;

import lombok.Data;
import uz.hotel.entity.enums.Type;

@Data
public class Room {
    private int id;
    private String number; // e.g. "101"
    private Type type;
    private Double price;
    private String description;
}
