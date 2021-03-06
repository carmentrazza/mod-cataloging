/*
 * Created on Sep 15, 2004
 *
 */
package org.folio.cataloging.dao;

import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;
import org.folio.cataloging.dao.persistence.SBJCT_HDG;
import org.folio.cataloging.business.descriptor.Descriptor;

/**
 * Class comment
 * @author janick
 */
public class DAOSubjectDescriptor extends DAODescriptor {

	/* (non-Javadoc)
	 * @see DAODescriptor#getPersistentClass()
	 */
	public Class getPersistentClass() {
		return SBJCT_HDG.class;
	}


	/* (non-Javadoc)
	 * @see DAODescriptor#supportsAuthorities()
	 */
	public boolean supportsAuthorities() {
		return true;
	}
	public boolean isMatchingAnotherHeading(Descriptor desc) {
		SBJCT_HDG d = (SBJCT_HDG) desc;
		try {
			List l = currentSession().find(
					"select count(*) from "
							+ getPersistentClass().getName()
							+ " as c "
							+ " where c.stringText= ? "
							+ " and c.accessPointLanguage = ?" 
							+ " and c.typeCode =? "
							+ " and c.sourceCode =? "
							+ " and c.key.userViewString = ?"
							+ " and c.key.headingNumber <> ?",
					new Object[] { 
							d.getStringText(),
							new Integer(d.getAccessPointLanguage()),
							new Integer(d.getTypeCode()),
							new Integer(d.getSourceCode()),
							d.getUserViewString(),
							new Integer(d.getKey().getHeadingNumber()) },
					new Type[] { Hibernate.STRING,
							Hibernate.INTEGER,
						    Hibernate.INTEGER, 
							Hibernate.INTEGER,
							Hibernate.STRING,
							Hibernate.INTEGER});
			return ((Integer) l.get(0)).intValue() > 0;
		} catch (Exception e) {
			return false;
		}
	}



}
