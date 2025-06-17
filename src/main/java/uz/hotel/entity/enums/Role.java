package uz.hotel.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN(List.of()),
    RECEPTIONIST(List.of()),
    USER(List.of());

    private List<String> permissions;
}
