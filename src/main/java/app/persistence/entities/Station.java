package app.persistence.entities;

import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Station(String stationName, String description)
    {
        ValidationUtil.validateNotBlank(stationName, "Station name");
        ValidationUtil.validateNotBlank(description, "Description");
        this.stationName = stationName;
        this.description = description;
    }

    public void update(String stationName, String description)
    {
        ValidationUtil.validateNotBlank(stationName, "Station name");
        ValidationUtil.validateNotBlank(description, "Description");
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
