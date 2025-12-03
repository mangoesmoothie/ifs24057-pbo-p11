package org.delcom.app.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class CashFlowFormTests {

    private static Validator validator;

    // Inisialisasi Validator sebelum semua test dijalankan
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Form Valid (Happy Path)")
    void testValidCashFlowForm() {
        CashFlowForm form = new CashFlowForm();
        form.setType("CASH_IN");
        form.setSource("Dompet");
        form.setLabel("Uang Saku");
        form.setAmount(50000);
        form.setDescription("Dikasih orang tua");

        // Validasi form
        Set<ConstraintViolation<CashFlowForm>> violations = validator.validate(form);

        // Harusnya tidak ada error (violations kosong)
        assertTrue(violations.isEmpty(), "Form harusnya valid dan tidak ada error");
    }

    @Test
    @DisplayName("Test Form Invalid (Field Kosong)")
    void testInvalidEmptyFields() {
        CashFlowForm form = new CashFlowForm();
        // Semua field sengaja dikosongkan/null
        form.setType("");
        form.setSource("   ");
        form.setLabel(null);
        form.setAmount(null);
        form.setDescription("");

        Set<ConstraintViolation<CashFlowForm>> violations = validator.validate(form);

        // Harusnya ada error
        assertFalse(violations.isEmpty(), "Form harusnya tidak valid");
        // Kita mengharapkan 5 error (Type, Source, Label, Amount, Description)
        assertEquals(5, violations.size(), "Harusnya ada 5 field yang error");
    }

    @Test
    @DisplayName("Test Form Invalid (Nominal <= 0)")
    void testInvalidAmount() {
        CashFlowForm form = new CashFlowForm();
        form.setType("CASH_OUT");
        form.setSource("ATM");
        form.setLabel("Jajan");
        form.setDescription("Beli Cilok");
        
        // Kasus 1: Nominal 0
        form.setAmount(0);
        Set<ConstraintViolation<CashFlowForm>> violationsZero = validator.validate(form);
        assertFalse(violationsZero.isEmpty());
        // Cek pesan error mengandung kata "Nominal" atau validasi Min
        boolean hasMinError = violationsZero.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        assertTrue(hasMinError, "Harusnya ada error di field amount");

        // Kasus 2: Nominal Negatif
        form.setAmount(-5000);
        Set<ConstraintViolation<CashFlowForm>> violationsNegative = validator.validate(form);
        assertFalse(violationsNegative.isEmpty());
    }

    @Test
    @DisplayName("Test Getters and Setters")
    void testGettersSetters() {
        UUID id = UUID.randomUUID();
        CashFlowForm form = new CashFlowForm();

        form.setId(id);
        form.setType("CASH_IN");
        form.setSource("Bank BCA");
        form.setLabel("Gaji");
        form.setAmount(1000000);
        form.setDescription("Gaji Bulan November");
        form.setConfirmLabel("Gaji");

        assertEquals(id, form.getId());
        assertEquals("CASH_IN", form.getType());
        assertEquals("Bank BCA", form.getSource());
        assertEquals("Gaji", form.getLabel());
        assertEquals(1000000, form.getAmount());
        assertEquals("Gaji Bulan November", form.getDescription());
        assertEquals("Gaji", form.getConfirmLabel());
    }
}