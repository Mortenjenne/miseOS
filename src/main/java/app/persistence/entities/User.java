package app.persistence.entities;

import app.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Setter
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Setter
    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(String firstName, String lastName, String email, String hashedPassword, UserRole userRole, Station station)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.userRole = userRole;
        this.station = station;
    }

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, email);
    }
}
