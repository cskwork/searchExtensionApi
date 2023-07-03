package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TestAdd {
	@Test
	public void testAdd() {
	    assertEquals(42, Integer.sum(19, 23));
	}
	@Test
	public void testDivide() {
	    assertThrows(ArithmeticException.class, () -> {
	        Integer.divideUnsigned(42, 0);
	    });
	}
	
    @Tag("slow")
    @Test
    public void testAddMaxInteger() {
        assertEquals(2147483646, Integer.sum(2147183646, 300000));
    }
 
    @Tag("fast")
    @Test
    public void testDivide2() {
        assertThrows(ArithmeticException.class, () -> {
            Integer.divideUnsigned(42, 0);
        });
    }
	
}
