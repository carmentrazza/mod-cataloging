package org.folio.cataloging.business.common.filter;

import org.folio.cataloging.business.cataloguing.bibliographic.MarcCorrelationException;
import org.folio.cataloging.business.cataloguing.common.Browsable;
import org.folio.cataloging.business.cataloguing.common.Tag;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.business.descriptor.Descriptor;

public class SameDescriptorTagFilter implements TagFilter {

	public boolean accept(Tag tag, Object optionalCondition) throws MarcCorrelationException, DataAccessException {
		if(!tag.isBrowsable()) {
			return false;
		}
		// ok, the tag is browsable
		if(optionalCondition==null || 
			!(optionalCondition instanceof Descriptor)){
			return false;
		}
		// ok, the optionalCondition is presents and it is a correct type
		Descriptor tagDescriptor = ((Browsable)tag).getDescriptor();
		if(tagDescriptor==null) return false;
		// ok, tag has a decriptor associated
		Descriptor toFind = (Descriptor)optionalCondition;
		
		// check if the descriptors are the same...
		return tagDescriptor.equals(toFind);
	}
}
