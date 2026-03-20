package app.persistence.entities;

import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "station")
public class Station implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_name",nullable = false, unique = true, length = 100)
    private String stationName;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Station(String stationName, String description)
    {
        ValidationUtil.validateNotBlank(stationName, "Station name");
        ValidationUtil.validateNotBlank(description, "Description");

        this.stationName = stationName.trim();
        this.description = description.trim();
    }

    public void update(String stationName, String description)
    {
        ValidationUtil.validateNotBlank(stationName, "Station name");
        ValidationUtil.validateNotBlank(description, "Description");

        this.stationName = stationName.trim();
        this.description = description.trim();
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station other = (Station) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
