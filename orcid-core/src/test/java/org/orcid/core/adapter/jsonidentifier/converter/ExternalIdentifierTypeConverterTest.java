package org.orcid.core.adapter.jsonidentifier.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ExternalIdentifierTypeConverterTest {
   
    ExternalIdentifierTypeConverter converter = new ExternalIdentifierTypeConverter();

    @Test
    public void testConvertTo() {
        assertEquals("SOMETHING", converter.convertTo("something", null, null));
        assertEquals("GRANT_NUMBER", converter.convertTo("grant_number", null, null));
        assertEquals("ERM_WHAT_ELSE", converter.convertTo("erm-what-else", null, null));
    }

    @Test
    public void testConvertFrom() {
        assertEquals("something", converter.convertFrom("SOMETHING", null, null));
        assertEquals("grant_number", converter.convertFrom("GRANT_NUMBER", null, null));
        assertEquals("erm-what-else", converter.convertFrom("ERM_WHAT_ELSE", null, null));
    }

}