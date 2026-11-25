package com.loanledger;

import com.loanledger.model.Borrower;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Borrower Model Unit Tests")
class BorrowerTest {

    @Test
    @DisplayName("Should create borrower with convenience constructor and default active status")
    void testConvenienceConstructor() {
        Borrower borrower = new Borrower("Rahul Sharma", "9876543210", "123 Main St, Delhi", "GOV12345");

        assertEquals("Rahul Sharma", borrower.getFullName());
        assertEquals("9876543210", borrower.getPhone());
        assertEquals("123 Main St, Delhi", borrower.getAddress());
        assertEquals("GOV12345", borrower.getIdNumber());
        assertEquals("active", borrower.getStatus());
        assertTrue(borrower.isActive());
        assertEquals(LocalDate.now(), borrower.getDateRegistered());
    }

    @Test
    @DisplayName("Should create borrower with full constructor")
    void testFullConstructor() {
        LocalDate regDate = LocalDate.of(2026, 1, 15);
        LocalDateTime now = LocalDateTime.now();

        Borrower borrower = new Borrower(10, "Priya Patel", "9123456780", "456 Market Rd, Mumbai",
                "AADHAAR9876", regDate, "inactive", now, now);

        assertEquals(10, borrower.getId());
        assertEquals("Priya Patel", borrower.getFullName());
        assertEquals("9123456780", borrower.getPhone());
        assertEquals("456 Market Rd, Mumbai", borrower.getAddress());
        assertEquals("AADHAAR9876", borrower.getIdNumber());
        assertEquals(regDate, borrower.getDateRegistered());
        assertEquals("inactive", borrower.getStatus());
        assertFalse(borrower.isActive());
        assertEquals(now, borrower.getCreatedAt());
        assertEquals(now, borrower.getUpdatedAt());
    }

    @Test
    @DisplayName("Should correctly update fields via setters")
    void testSetters() {
        Borrower borrower = new Borrower();
        borrower.setId(5);
        borrower.setFullName("Amit Kumar");
        borrower.setPhone("9811223344");
        borrower.setAddress("78 Ring Road, Bangalore");
        borrower.setIdNumber("VOTER5544");
        borrower.setStatus("active");

        assertEquals(5, borrower.getId());
        assertEquals("Amit Kumar", borrower.getFullName());
        assertEquals("9811223344", borrower.getPhone());
        assertEquals("78 Ring Road, Bangalore", borrower.getAddress());
        assertEquals("VOTER5544", borrower.getIdNumber());
        assertTrue(borrower.isActive());
    }

    @Test
    @DisplayName("Should equate borrowers with same id, phone, and idNumber")
    void testEqualsAndHashCode() {
        Borrower b1 = new Borrower(1, "John Doe", "9876543210", "Addr 1", "ID100", LocalDate.now(), "active", null, null);
        Borrower b2 = new Borrower(1, "John Doe Updated", "9876543210", "Addr 2", "ID100", LocalDate.now(), "active", null, null);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }
}
