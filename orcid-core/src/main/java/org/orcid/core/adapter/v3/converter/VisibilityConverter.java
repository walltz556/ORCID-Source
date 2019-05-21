package org.orcid.core.adapter.v3.converter;

import org.orcid.jaxb.model.v3.release.common.Visibility;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class VisibilityConverter extends BidirectionalConverter<Visibility, String> {

    @Override
    public String convertTo(Visibility source, Type<String> destinationType, MappingContext context) {
        return source.name();
    }

    @Override
    public Visibility convertFrom(String source, Type<Visibility> destinationType, MappingContext context) {
        return Visibility.valueOf(source);
    }
}
