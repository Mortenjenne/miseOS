package app.persistence.entities;

import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Setter
    @Column(name = "description", length = 255)
    private String description;

    @Setter
    @Column(name = "display_number")
    private Integer displayNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Allergen(String name, String description, Integer displayNumber)
    {
        validateDisplayNumber(displayNumber);
        ValidationUtil.validateNotBlank(name, "Name");
        ValidationUtil.validateNotBlank(description, "Description");
        this.name = name.trim();
        this.description = description.trim();
        this.displayNumber = displayNumber;
    }

    public void update(String newName, String newDescription, Integer displayNumber)
    {
        validateDisplayNumber(displayNumber);
        ValidationUtil.validateNotBlank(newName, "New name");
        ValidationUtil.validateNotBlank(newDescription, "New Description");
        this.name = newName.trim();
        this.description = newDescription.trim();
        this.displayNumber = displayNumber;
    }

    private void validateDisplayNumber(Integer displayNumber)
    {
        if(displayNumber < 0) throw new IllegalArgumentException("Display number must be positive");
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
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
