package uz.hotel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.hotel.entity.enums.UserRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements UserDetails {
    private int id;
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private Double balance;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(
            role.getPermissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList()
                );
        authorities.add(new SimpleGrantedAuthority("ROLE_"+role.name()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
