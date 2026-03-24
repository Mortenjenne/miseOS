package app.utils;

import app.persistence.entities.Station;
import app.persistence.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class DBValidatorTest
{
    @DisplayName("Validate exists - Throws when entity is null")
    @Test
    void validateExistsThrowsWhenEntityIsNull()
    {
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> DBValidator.validateExists(null, 42L, User.class));

        assertThat(ex.getMessage(), containsString("User"));
        assertThat(ex.getMessage(), containsString("42"));
    }

    @DisplayName("Validate exists - Returns Entity when object is not null")
    @Test
    void validateExistsReturnsEntityWhenPresent()
    {
        Station station = new Station(
            "Hot station",
            "Hot food"
        );

        Station result = DBValidator.validateExists(station, 1L, Station.class);
        assertThat(result, is(station));
    }
}
