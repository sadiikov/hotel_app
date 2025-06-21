package uz.hotel.entity;

import lombok.Data;
import uz.hotel.entity.enums.PaymentStatus;

import java.sql.Timestamp;

@Data
public class Payment {
    private int id;
    private int reservationId;
    private double amount;
    private PaymentStatus status;
    private String method; // optional: CARD, CLICK, etc.
    private Timestamp paidAt;
}
