package org.folio.cataloging.business.common.group;

import org.folio.cataloging.business.cataloguing.bibliographic.MarcCorrelationException;
import org.folio.cataloging.business.cataloguing.common.Tag;
import org.folio.cataloging.business.common.DataAccessException;

public interface GroupManager {
	TagGroup getGroup(Tag tag) throws MarcCorrelationException, DataAccessException;
	boolean isSameGroup(Tag tag1, Tag tag2) throws MarcCorrelationException, DataAccessException;
	void add(TagGroup group);
}
