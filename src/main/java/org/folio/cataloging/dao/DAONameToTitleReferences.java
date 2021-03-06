/*
 * (c) LibriCore
 * 
 * Created on Jan 2, 2014
 * 
 * DAONameToTitleReferences.java
 */
package org.folio.cataloging.dao;

import java.util.List;

import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.persistence.NME_TO_TTL_REF;
import org.folio.cataloging.dao.persistence.REF;
import org.folio.cataloging.dao.persistence.ReferenceType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;

/**
 * @author paulm
 * @version $Revision: 1.1 $, $Date: 2006/01/05 13:25:59 $
 * @since 1.0
 */
public class DAONameToTitleReferences extends DAOCrossReferences {

	/* (non-Javadoc)
	 * @see DAOCrossReferences#loadReciprocal(REF, int)
	 */
	public REF loadReciprocal(REF ref, int cataloguingView)
		throws DataAccessException {

		short reciprocalType = ReferenceType.getReciprocal(ref.getType());

		REF result = null;
		String queryString;
		if (((NME_TO_TTL_REF) ref).isSourceName()) {
			queryString =
				"from NME_TO_TTL_REF as ref "
					+ " where ref.nameHeadingNumber = ? AND "
					+ " ref.titleHeadingNumber = ? AND "
					+ " substr(ref.userViewString, ?, 1) = '1' AND "
					+ " ref.type = ?";

		} else {
			queryString =
				"from NME_NME_TTL_REF as ref "
					+ " where ref.titleHeadingNumber = ? AND "
					+ " ref.nameHeadingNumber = ? AND "
					+ " substr(ref.userViewString, ?, 1) = '1' AND "
					+ " ref.type = ?";
		}
		List l =
			find(
				queryString,
				new Object[] {
					new Integer(ref.getSource()),
					new Integer(ref.getTarget()),
					new Integer(cataloguingView),
					new Short(reciprocalType)},
				new Type[] {
					Hibernate.INTEGER,
					Hibernate.INTEGER,
					Hibernate.INTEGER,
					Hibernate.SHORT });
		if (l.size() == 1) {
			result = (REF) l.get(0);
		}

		return result;
	}

}
