package app.persistence.entities;

import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "allergen")
public class Allergen implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_da", nullable = false, unique = true, length = 100)
    private String nameDA;

    @Column(name = "name_en", nullable = false, unique = true, length = 100)
    private String nameEN;

    @Column(name = "description_da", length = 150)
    private String descriptionDA;

    @Column(name = "description_en", length = 150)
    private String descriptionEN;

    @Column(name = "display_number")
    private Integer displayNumber;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Allergen(String nameDA, String nameEN, String descriptionDA, String descriptionEN, Integer displayNumber)
    {
        validateDisplayNumber(displayNumber);
        ValidationUtil.validateNotBlank(nameDA, "Name DA");
        ValidationUtil.validateNotBlank(nameEN, "Name EN");
        ValidationUtil.validateNotBlank(descriptionDA, "Description DA");
        ValidationUtil.validateNotBlank(descriptionEN, "Description EN");

        this.nameDA = nameDA.trim();
        this.nameEN = nameEN.trim();
        this.descriptionDA = descriptionDA.trim();
        this.descriptionEN = descriptionEN.trim();
        this.displayNumber = displayNumber;
    }

    public void update(String nameDA, String nameEN, String descriptionDA, String descriptionEN, Integer displayNumber)
    {
        ValidationUtil.validateNotBlank(nameDA, "Name DA");
        ValidationUtil.validateNotBlank(nameEN, "Name EN");
        ValidationUtil.validateNotBlank(descriptionDA, "Description DA");
        ValidationUtil.validateNotBlank(descriptionEN, "Description EN");
        validateDisplayNumber(displayNumber);

        this.nameDA = nameDA.trim();
        this.nameEN = nameEN.trim();
        this.descriptionDA = descriptionDA.trim();
        this.descriptionEN = descriptionEN.trim();
        this.displayNumber = displayNumber;
    }

    private void validateDisplayNumber(Integer displayNumber)
    {
        if(displayNumber < 0)
        {
            throw new IllegalArgumentException("Display number must be positive");
        }
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate(){this.updatedAt = LocalDateTime.now(); }

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
