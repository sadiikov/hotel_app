package uz.hotel.dto;

import lombok.Data;
import uz.hotel.entity.Room;
import uz.hotel.entity.enums.RoomStatus;
import uz.hotel.entity.enums.RoomType;

@Data
public class RoomDTO {
    private int id;
    private String number;
    private String type; // "STANDARD" or "LUXE"
    private Double price;
    private String description;
    private Double rating;
    private RoomStatus status;

    public Room toRoom() {
        Room room = new Room();
        room.setId(this.id);
        room.setNumber(this.number);
        room.setType(RoomType.valueOf(this.type));
        room.setPrice(this.price);
        room.setDescription(this.description);
        room.setRating(this.rating != null ? this.rating : 0.0);
        room.setStatus(this.status != null ? this.status : RoomStatus.AVAILABLE);
        return room;
    }
}


