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

    public Station(String stationName, String description)
    {
        this.stationName = stationName;
        this.description = description;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }
}
