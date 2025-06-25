package uz.hotel.dto;

import lombok.Data;
import uz.hotel.entity.enums.ReservationStatus;

import java.sql.Timestamp;
@Data
public class ReservationHistoryDTO {
    private Long id;
    private Long roomId;
    private Long userId;
    private Timestamp checkIn;
    private Timestamp checkOut;
    private String hotelName;
    private ReservationStatus status;
}
