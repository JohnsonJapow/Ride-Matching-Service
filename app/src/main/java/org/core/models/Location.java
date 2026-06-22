package org.core.models;

import java.math.BigDecimal;

public record Location(BigDecimal x, BigDecimal y) {
    public Location(String xStr, String yStr) {
        this(parseBigDecimal(xStr), parseBigDecimal(yStr));
    }

    private static BigDecimal parseBigDecimal(String val) {
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect Location format: " + val, e);
        }
    }
}