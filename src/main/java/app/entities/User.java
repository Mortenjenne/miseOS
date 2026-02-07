package app.entities;

import app.enums.Role;
import app.enums.Station;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name")
    private String firstName;

    
    private String lastName;

    private String email;

    private String hashedPassword;

    private Role role;

    private Station station;
}
