package uz.hotel.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.hotel.entity.enums.Role;

import java.util.Collection;
import java.util.List;

@Data
public class User implements UserDetails {
    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private Double balance;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        List<GrantedAuthority> auth = new ArrayList<>();
//        auth.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
//        auth.add(new SimpleGrantedAuthority("CREATE_ARTICLE"));

        List<SimpleGrantedAuthority> authorities =
                role
                        .getPermissions()
                        .stream()
                        .map(s -> new SimpleGrantedAuthority(s))
                        .toList();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return authorities;
//        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getUsername() {
        return "";
    }
}
