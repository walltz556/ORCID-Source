package org.orcid.core.adapter.converter;

import org.orcid.jaxb.model.record_v2.GivenNames;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class GivenNamesConverter extends BidirectionalConverter<GivenNames, String> {

    @Override
    public String convertTo(GivenNames source, Type<String> destinationType, MappingContext context) {
        return source.getContent();
    }

    @Override
    public GivenNames convertFrom(String source, Type<GivenNames> destinationType, MappingContext context) {
        if (source != null && source.trim().isEmpty()) {
            return null;
        }
        return new GivenNames(source);
    }
}
