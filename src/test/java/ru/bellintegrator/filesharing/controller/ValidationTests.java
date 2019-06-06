package ru.bellintegrator.filesharing.controller;

import org.junit.Assert;
import org.junit.Test;
import ru.bellintegrator.filesharing.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Тест валидации пользователя
 */
public class ValidationTests {

    private static Validator validator;
    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    @Test
    public void testValidators() {
        final User user = new User();

        Set<ConstraintViolation<User>> validates = validator.validate(user);
        Assert.assertTrue(validates.size() == 4);
        Assert.assertTrue(validates.stream().anyMatch(v -> v.getMessage().equals("Enter the name!")));
        Assert.assertTrue(validates.stream().anyMatch(v -> v.getMessage().equals("Enter the password!")));
        Assert.assertTrue(validates.stream().anyMatch(v -> v.getMessage().equals("Password confirmation cannot be empty!")));
        Assert.assertTrue(validates.stream().anyMatch(v -> v.getMessage().equals("Enter the e-mail!")));
    }
}
