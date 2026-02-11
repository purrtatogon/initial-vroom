package com.initialvroom.config;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

/**
 * Custom OpenCSV converter for CSV columns with "TRUE"/"FALSE" strings.
 * Default Boolean parsing is case-sensitive; this handles both.
 */
public class BooleanConverter extends AbstractBeanField<Boolean, String> {

    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim().toLowerCase());
    }
}
