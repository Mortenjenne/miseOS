package app.persistence.entities;

import app.enums.UserRole;
import app.exceptions.UnauthorizedActionException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "station")
public class Station implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "station_name",nullable = false, unique = true, length = 50)
    private String stationName;

    @Setter
    @Column(name = "description", nullable = true, length = 255)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    public static Station create(String stationName, String description, User creator)
    {
        if (creator.getUserRole() != UserRole.HEAD_CHEF) {
            throw new UnauthorizedActionException("Only head chefs can create stations");
        }

        if (stationName == null || stationName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Station name cannot be empty");
        }

        String normalized = stationName.trim().toUpperCase();

        Station station = new Station();
        station.stationName = normalized;
        station.description = description;
        station.createdBy = creator;
        return station;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
