package uz.hotel.entity;

import lombok.Data;
import uz.hotel.entity.enums.ReservationStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class Reservation {
    private int id;
    private int userId;
    private Long roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime createdAt;
    private Double totalPrice;
    private ReservationStatus status;
}
