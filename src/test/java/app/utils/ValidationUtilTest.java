package app.utils;

import app.dtos.user.CreateUserRequestDTO;
import app.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest
{

    @DisplayName("Validate not null - Dosen't throw when object is not null")
    @Test
    void validateNotNullDosentThrow()
    {
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO("Test", "Test", "Test@mail.com", "Test");
        assertDoesNotThrow(() -> ValidationUtil.validateNotNull(requestDTO, "Field"));
    }

    @DisplayName(("Validate not null - Throws when object is null"))
    @Test
    void validateNotNullPasses()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateNotNull(null, "Field"));
    }

    @DisplayName("Validate not blank - Throws when field is null")
    @Test
    void validateNotBlankThrowsWhenNull()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateNotBlank(null, "Field"));
    }

    @DisplayName("Validate not blank - Throws when field is empty")
    @Test
    void validateNotBlankThrowsWhenBlank()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateNotBlank("   ", "Field"));
    }

    @DisplayName("Validate not blank - Dosen't throw when field is present")
    @Test
    void validateNotBlankPasses()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateNotBlank("value", "Field"));
    }

    @DisplayName("Validate text - Throws throw when when text is too short")
    @Test
    void validateTextThrowsWhenTooShort()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateText("a", "Field", 2, 100));
    }

    @DisplayName("Validate text - Throws throw when when text is too long")
    @Test
    void validateTextThrowsWhenTooLong()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateText("a".repeat(101), "Field", 2, 100));
    }

    @DisplayName("Validate text - Throws throw when when text, includes non accepted special characters")
    @Test
    void validateTextThrowsOnSpecialCharacters()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateText("Invalid*", "Field", 2, 100));
    }

    @DisplayName("Validate text - Passes when text is equal to minimum requirements")
    @Test
    void validateTextPasses()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateText("Valid text", "Field", 2, 100));
    }

    @DisplayName("Validate text - Passes when text is equal to maximum requirement")
    @Test
    void validateTextAcceptsBoundaryLengths()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateText("ab", "Field", 2, 100));
        assertDoesNotThrow(() -> ValidationUtil.validateText("a".repeat(100), "Field", 2, 100));
    }

    @DisplayName("Validate name - Throws throw when when name is too short")
    @Test
    void validateNameThrowsWhenTooShort()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateName("a", "Name"));
    }

    @DisplayName("Validate name - Passes with correct name requirement")
    @Test
    void validateNameAcceptsValidName()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateName("Gordon", "Name"));
    }

    @DisplayName("Validate description - Throws throw when when description is too short")
    @Test
    void validateDescriptionThrowsWhenTooShort()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateDescription("ab", "Description"));
    }

    @DisplayName("Validate description - Passes with correct description requirement")
    @Test
    void validateDescriptionAcceptsValidDescription()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateDescription("Valid description", "Description"));
    }

    @DisplayName("Validate positive - Throws when value is zero")
    @Test
    void validatePositiveThrowsWithZero()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validatePositive(0.0, "Quantity"));
    }

    @DisplayName("Validate positive - Throws when value is below zero")
    @Test
    void validatePositiveThrowsWithNegative()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validatePositive(-1.0, "Quantity"));
    }

    @DisplayName("Validate positive - Passes with value over zero")
    @Test
    void validatePositiveAcceptsPositiveValue()
    {
        assertDoesNotThrow(() -> ValidationUtil.validatePositive(0.1, "Quantity"));
    }

    @DisplayName("Validate range - Throws with number below minimum")
    @Test
    void validateRangeThrowsBelowMin()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRange(0, 1, 53, "Week"));
    }

    @DisplayName("Validate range - Throws with number above max")
    @Test
    void validateRangeThrowsAboveMax()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRange(54, 1, 53, "Week"));
    }

    @DisplayName("Validate range - Passes with correct boundary values")
    @Test
    void validateRangeAcceptsBoundaryValues()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateRange(7, 1, 53, "Week"));
        assertDoesNotThrow(() -> ValidationUtil.validateRange(2026, 2020, 2100, "Week"));
    }

    @DisplayName("Validate not past date - Throws with date in past")
    @Test
    void validateNotPastDateThrowsWithPastDate()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateNotPastDate(LocalDate.now().minusDays(1), "Date"));
    }

    @DisplayName("Validate not past date - Passes with future date")
    @Test
    void validateFutureDateAcceptsNotPastDate()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateNotPastDate(LocalDate.now().plusDays(1), "Date"));
    }

    @DisplayName("Validate id - Throws with null input")
    @Test
    void validateIdThrowsWithNull()
    {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateId(null));
    }

    @DisplayName("Validate id - Throws with zero")
    @Test
    void validateIdThrowsWithZero()
    {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateId(0L));
    }

    @DisplayName("Validate id - Throws with negative value")
    @Test
    void validateIdThrowsWithNegative()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateId(-1L));
    }

    @DisplayName("Validate id - Passes with correct value")
    @Test
    void validateIdAcceptsPositive()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateId(1L));
    }

    @DisplayName("Validate email - Throws when email missing @")
    @Test
    void validateEmailThrowsWithoutAtSign()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("gordonkitchen.com"));
    }

    @DisplayName("Validate email - Throws when email missing domain")
    @Test
    void validateEmailThrowsWithoutDomain()
    {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("gordon@"));
    }

    @DisplayName("Validate email - Returns email lowercased and trimmed")
    @Test
    void validateEmailReturnsLowercaseTrimmed()
    {
        String result = ValidationUtil.validateEmail(" GORDON@KITCHEN.COM  ");
        assertEquals("gordon@kitchen.com", result);
    }

    @DisplayName("Validate email - Passes with correct email format")
    @Test
    void validateEmailAcceptsValidEmail()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("gordon@kitchen.com"));
    }

    @DisplayName("Validate not empty - Throws with null collection")
    @Test
    void validateNotEmptyThrowsWithNullCollection()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateNotEmpty(null, "Items"));
    }

    @DisplayName("Validate not empty - Passes with correct collection")
    @Test
    void validateNotEmptyAcceptsNonEmptyCollection()
    {
        assertDoesNotThrow(() -> ValidationUtil.validateNotEmpty(List.of("item"), "Items"));
    }
}
