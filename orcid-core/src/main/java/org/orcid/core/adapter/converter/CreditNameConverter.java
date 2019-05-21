package org.orcid.core.adapter.converter;

import org.orcid.jaxb.model.common_v2.CreditName;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class CreditNameConverter extends BidirectionalConverter<CreditName, String> {

    @Override
    public String convertTo(CreditName source, Type<String> destinationType, MappingContext context) {
        return source.getContent();
    }

    @Override
    public CreditName convertFrom(String source, Type<CreditName> destinationType, MappingContext context) {
        if (source != null && source.trim().isEmpty()) {
            return null;
        }
        CreditName creditName = new CreditName();
        creditName.setContent(source);
        return creditName;
    }
}
