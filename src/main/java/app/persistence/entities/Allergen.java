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

    @Setter
    @Column(name = "icon_code", nullable = true)
    private String iconCode;

    public Allergen(String name, String iconCode)
    {
        this.name = name;
        this.iconCode = iconCode;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Allergen allergen = (Allergen) o;
        return Objects.equals(id, allergen.id) && Objects.equals(name, allergen.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name);
    }
}
