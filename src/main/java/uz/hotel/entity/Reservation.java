package uz.hotel.entity;

import lombok.Data;
import uz.hotel.entity.enums.ReservationStatus;

import java.sql.Timestamp;

@Data
public class Reservation {
    private int id;
    private int userId;
    private int roomId;
    private Timestamp checkIn;
    private Timestamp checkOut;
    private Double totalPrice;
    private ReservationStatus status;
}
