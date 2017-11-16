package librisuite.business.common.filter;

import librisuite.business.cataloguing.bibliographic.MarcCorrelationException;
import librisuite.business.cataloguing.common.Tag;
import librisuite.business.common.DataAccessException;

public interface TagFilter {

	public boolean accept(Tag tag, Object optionalCondition) 
		throws MarcCorrelationException, DataAccessException;
}