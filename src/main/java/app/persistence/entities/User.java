package app.persistence.entities;

import app.enums.UserRole;
import app.exceptions.UnauthorizedActionException;
import app.utils.PasswordUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Getter
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Getter
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Getter
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
        ValidationUtil.validateNotBlank(firstName, "First name");
        ValidationUtil.validateNotBlank(lastName, "Last name");
        ValidationUtil.validateNotNull(userRole, "User role");

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = ValidationUtil.validateEmail(email);
        this.hashedPassword = hashedPassword;
        this.userRole = userRole;
    }

    public void update(String firstName, String lastName, String email, String password)
    {
        ValidationUtil.validateNotBlank(firstName, "First name");
        ValidationUtil.validateNotBlank(lastName, "Last name");
        this.updatedAt = LocalDateTime.now();

    }

    public boolean isHeadChef()
    {
        return this.userRole == UserRole.HEAD_CHEF;
    }

    public boolean isLineCook()
    {
        return this.userRole == UserRole.LINE_COOK;
    }

    public boolean isSousChef(){return this.userRole == UserRole.SOUS_CHEF; }

    public boolean hasRole(UserRole role)
    {
        return this.userRole == role;
    }

    public boolean verifyPassword(String plainTextPassword) {
        return PasswordUtil.verifyPassword(plainTextPassword, this.hashedPassword);
    }

    public boolean isKitchenStaff()
    {
        return userRole == UserRole.HEAD_CHEF || userRole == UserRole.SOUS_CHEF || userRole == UserRole.LINE_COOK;
    }

    public void ensureIsKitchenStaff()
    {
        if(!isKitchenStaff())
        {
            throw new UnauthorizedActionException(
                "Only kitchen staff can create dish suggestions"
            );
        }
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
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
