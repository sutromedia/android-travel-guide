package com.sutromedia.android.lib.model;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyAmount {
    double mAmount;
    
    public CurrencyAmount(String value) {
        mAmount = -1.0;
        try {
            if (value != null) {
                mAmount = Double.valueOf(value);
            }
        } catch (NumberFormatException error) {
            mAmount = -1.0;
        }
    }
    
    public boolean isFree() {
        return mAmount == 0.0;
    }
    
    public boolean hasAmount() {
        return mAmount >= 0;
    }
    
    public double getAmount() {
        return mAmount;
    }
    
    public String getFormatted(
        String formatStringFree,
        String formatStringPricy,
        String currency) {
        if (hasAmount()) {
            if (isFree()) {
                return formatStringFree;
            } else {
                return String.format(formatStringPricy, mAmount, currency);
            }
        }
        return null;
    }

    public String getFormattedAbstract(
        final String formatStringFree,
	final String currency) {

        if (hasAmount()) {
            if (isFree()) {
                return formatStringFree;
            } else {
		int amount = (int)getAmount();
		StringBuilder builder = new StringBuilder(amount);
		for (int i=0; i<amount; i++) {
		    builder.append(currency);
		}
		return builder.toString();
            }
        }
        return null;	
    }
}