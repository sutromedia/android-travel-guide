package com.sutromedia.android.lib.db;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class CurrencyAmountTest extends AndroidTestCase {

    public void testShouldSucceedWithNullValue() {
        CurrencyAmount amount = new CurrencyAmount(null);
        assertFalse(amount.hasAmount());
        assertFalse(amount.isFree());
        assertNull(amount.getFormatted("$"));
    }

    public void testShouldSucceedWithInvalidValue() {
        CurrencyAmount amount = new CurrencyAmount("oops");
        assertFalse(amount.hasAmount());
        assertFalse(amount.isFree());
        assertNull(amount.getFormatted("$"));
    }
    
    public void testShouldSucceedWithNoAmount() {
        CurrencyAmount amount = new CurrencyAmount("-1");
        assertFalse(amount.hasAmount());
        assertFalse(amount.isFree());
        assertNull(amount.getFormatted("$"));
    }    
    
    public void testShouldSucceedWithFreeAmount() {
        CurrencyAmount amount = new CurrencyAmount("0");
        assertTrue(amount.hasAmount());
        assertTrue(amount.isFree());
        assertEquals("Free!", amount.getFormatted("$"));
        assertEquals("Free!", amount.getFormatted(""));
        assertEquals("Free!", amount.getFormatted(null));
    }
    
    public void testShouldSucceedWithValidAmount() {
        CurrencyAmount amount = new CurrencyAmount("10");
        assertTrue(amount.hasAmount());
        assertFalse(amount.isFree());
        assertEquals("$10", amount.getFormatted("$"));
        assertEquals("10", amount.getFormatted(""));
        assertEquals("10", amount.getFormatted(null));
    }
    
    public void testShouldSucceedWithBigAmount() {
        CurrencyAmount amount = new CurrencyAmount("100000.12");
        assertEquals("$100,000.12", amount.getFormatted("$"));
    }    
}