package uz.hotel.entity;

import lombok.Data;
import uz.hotel.entity.enums.OrderStatus;

@Data
public class Order {
    private int id;
    private int userId;
    private int roomId;
    private int menuItemId;
    private int quantity;
    private OrderStatus status;
}

