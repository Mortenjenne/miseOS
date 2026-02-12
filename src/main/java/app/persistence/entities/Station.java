package app.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "station_name",nullable = false, unique = true)
    private String stationName;

    @Setter
    @Column(name = "description", nullable = true)
    private String description;

    public Station(String stationName, String description)
    {
        this.stationName = stationName;
        this.description = description;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(id, station.id) && Objects.equals(stationName, station.stationName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, stationName);
    }
}
