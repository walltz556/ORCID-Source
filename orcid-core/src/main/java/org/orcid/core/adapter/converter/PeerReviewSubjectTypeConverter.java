package org.orcid.core.adapter.converter;

import org.orcid.jaxb.model.record_v2.WorkType;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class PeerReviewSubjectTypeConverter  extends BidirectionalConverter<WorkType, String> {

    @Override
    public String convertTo(WorkType source, Type<String> destinationType, MappingContext context) {
        return source.name();
    }

    @Override
    public WorkType convertFrom(String source, Type<WorkType> destinationType, MappingContext context) {
        try {
            return WorkType.valueOf(source);
        } catch (IllegalArgumentException e) {
            return WorkType.OTHER;
        }
    }

}
