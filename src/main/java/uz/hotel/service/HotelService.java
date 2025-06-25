package uz.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.hotel.dto.HotelDTO;
import uz.hotel.dto.RoomDTO;
import uz.hotel.entity.Hotel;
import uz.hotel.entity.Room;
import uz.hotel.entity.enums.RoomType;
import uz.hotel.repository.HotelDAO;
import uz.hotel.repository.RoomDAO;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelDAO hotelDAO;
    private final RoomDAO roomDAO;

    public void saveHotel(HotelDTO dto) {
        Hotel hotel = new Hotel();
        hotel.setName(dto.getName());
        hotel.setCountry(dto.getCountry());
        hotel.setCity(dto.getCity());
        hotel.setLocation(dto.getLocation());
        hotel.setRating(dto.getRating());

        int hotelId = hotelDAO.saveHotelAndReturnId(hotel);

        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < dto.getStandartCount(); i++) {
            Room room = new Room();
            room.setHotelId(hotelId);
            room.setType(RoomType.STANDARD);
            room.setNumber("1" + String.format("%02d", i + 1));
            room.setPrice(dto.getStandartPrice());
            room.setRating(0.0);
            room.setDescription(dto.getStandartDescription());
            rooms.add(room);
        }
        for (int i = 0; i < dto.getLuxeCount(); i++) {
            Room room = new Room();
            room.setHotelId(hotelId);
            room.setType(RoomType.LUXE);
            room.setNumber("2" + String.format("%02d", i + 1));
            room.setPrice(dto.getLuxePrice());
            room.setRating(0.0);
            room.setDescription(dto.getLuxeDescription());
            rooms.add(room);
        }

        roomDAO.saveRooms(rooms, hotelId);
    }

    public List<HotelDTO> getAllHotelsWithRooms() {
        List<Hotel> hotels = hotelDAO.findAll();
        return hotels.stream().map(hotel -> {
            List<Room> rooms = roomDAO.findByHotelId(hotel.getId());
            List<RoomDTO> roomDTOs = rooms.stream().map(this::toRoomDTO).toList();
            return new HotelDTO(hotel, roomDTOs);
        }).toList();
    }

    public void updateHotelWithRooms(HotelDTO dto) {
        hotelDAO.update(dto.toHotel());

        for (RoomDTO roomDTO : dto.getRooms()) {
            Room room = roomDTO.toRoom();
            room.setHotelId(dto.getId()); // ensure hotelId set
            roomDAO.updateRoom(room);
        }
    }

    public void deleteHotelById(int id) {
        roomDAO.deleteByHotelId(id); // only if NOT ON DELETE CASCADE
        hotelDAO.deleteById(id);
    }

    private RoomDTO toRoomDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setNumber(room.getNumber());
        dto.setType(room.getType().name());
        dto.setPrice(room.getPrice());
        dto.setDescription(room.getDescription());
        dto.setStatus(room.getStatus());
        return dto;
    }
}