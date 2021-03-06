package org.folio.cataloging.business.common.group;

import org.folio.cataloging.business.cataloguing.bibliographic.MarcCorrelationException;
import org.folio.cataloging.business.cataloguing.common.Tag;
import org.folio.cataloging.business.common.DataAccessException;

public interface TagGroup {
	boolean contains(Tag tag) throws MarcCorrelationException, DataAccessException;
	boolean isCanSort();
	boolean isSingleSort();
}
