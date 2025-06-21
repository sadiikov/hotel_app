package uz.hotel.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum UserRole {
    ADMIN(List.of()),
    USER(List.of());

    private List<String> permissions;
}
