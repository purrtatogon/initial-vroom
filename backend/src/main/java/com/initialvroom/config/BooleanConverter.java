package com.initialvroom.config;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

/**
 * Needed this because OpenCSV doesn't handle "TRUE"/"FALSE" strings as booleans by default.
 * The CSV has has_speed_chime as "TRUE" or "FALSE" text, and the default parser chokes on it.
 * Extending AbstractBeanField lets us hook into OpenCSV's type conversion pipeline.
 */
public class BooleanConverter extends AbstractBeanField<Boolean, String> {

    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value == null || value.isBlank()) {
            return null;
        }
        // trim + lowercase so "TRUE", " true ", "True" all work
        return Boolean.parseBoolean(value.trim().toLowerCase());
    }
}
