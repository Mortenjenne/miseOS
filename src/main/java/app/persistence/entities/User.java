package app.persistence.entities;

import app.enums.UserRole;
import app.utils.PasswordUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements IEntity
{
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Getter
    @Setter
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Getter
    @Setter
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @Getter
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(String firstName, String lastName, String email, String hashedPassword, UserRole userRole)
    {
        ValidationUtil.validateEmail(email);

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email.toLowerCase().trim();
        this.hashedPassword = hashedPassword;
        this.userRole = userRole;
    }

    public boolean isHeadChef()
    {
        return this.userRole == UserRole.HEAD_CHEF;
    }

    public boolean isLineCook()
    {
        return this.userRole == UserRole.LINE_COOK;
    }

    public boolean hasRole(UserRole role)
    {
        return this.userRole == role;
    }

    public boolean verifyPassword(String plainTextPassword) {
        return PasswordUtil.verifyPassword(plainTextPassword, this.hashedPassword);
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
