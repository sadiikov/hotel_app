package uz.hotel.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationDTO {
    private int id;
    private int userId;
    private int roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double totalPrice;

    private String hotelName;
    private String roomNumber;
}

