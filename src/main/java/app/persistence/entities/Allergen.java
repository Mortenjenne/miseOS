package app.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "allergen")
public class Allergen implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name", nullable = false, unique = true)
    private String name;


    public Allergen(String name)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Allergen name cannot be blank");
        }
        this.name = name.trim();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Allergen)) return false;
        Allergen other = (Allergen) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
