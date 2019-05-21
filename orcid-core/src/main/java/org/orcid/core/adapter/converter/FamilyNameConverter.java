package org.orcid.core.adapter.converter;

import org.orcid.jaxb.model.record_v2.FamilyName;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class FamilyNameConverter extends BidirectionalConverter<FamilyName, String> {

    @Override
    public String convertTo(FamilyName source, Type<String> destinationType, MappingContext context) {
        return source.getContent();
    }

    @Override
    public FamilyName convertFrom(String source, Type<FamilyName> destinationType, MappingContext context) {
        if (source != null && source.trim().isEmpty()) {
            return null;
        }
        return new FamilyName(source);
    }
}
