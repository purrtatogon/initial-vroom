package com.initialvroom.config;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

/** CSV exports spell booleans as TRUE/FALSE strings — OpenCSV needs a tiny converter hook for {@code has_speed_chime}. */
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
