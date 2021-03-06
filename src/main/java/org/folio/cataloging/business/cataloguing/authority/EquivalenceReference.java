/*
 * (c) LibriCore
 * 
 * Created on Dec 5, 2005
 * 
 * EquivalenceReference.java
 */
package org.folio.cataloging.business.cataloguing.authority;

import org.folio.cataloging.business.cataloguing.bibliographic.MarcCorrelationException;
import org.folio.cataloging.business.common.CorrelationValues;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.persistence.CorrelationKey;
import org.folio.cataloging.dao.persistence.ReferenceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import org.folio.cataloging.util.StringText;
import org.folio.cataloging.model.Subfield;

/**
 * @author paulm
 * @version $Revision: 1.3 $, $Date: 2006/01/05 13:25:58 $
 * @since 1.0
 */
public class EquivalenceReference extends AuthorityReferenceTag {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Log logger = LogFactory.getLog(EquivalenceReference.class);
	
	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public EquivalenceReference() {
		super();
	}

	@Override
	public boolean isEquivalenceReference() {
		return true;
	}

	@Override
	public CorrelationKey getMarcEncoding() throws DataAccessException,
			MarcCorrelationException {
		CorrelationKey key = super.getMarcEncoding();		
		logger.debug("getMarcEncoding before source: " + key);
		key = key.changeAuthoritySourceIndicator(getDescriptor().getAuthoritySourceCode());
		logger.debug("getMarcEncoding after source: " + key);
		return key;
	}

	/* (non-Javadoc)
	 * @see VariableField#getStringText()
	 */
	@Override
	public StringText getStringText() {
		String subw =
			""
				+ getReference().getLinkDisplay()
				+ getReference().getReplacementComplexity();
		StringText result = super.getStringText();
		result.addSubfield(new Subfield("w", subw));
		return result;
	}

	/* (non-Javadoc)
	 * @see TagInterface#correlationChangeAffectsKey(librisuite.business.common.CorrelationValues)
	 */
	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		if (!super.correlationChangeAffectsKey(v)) {
			return !ReferenceType.isEquivalence(v.getValue(getRefTypeCorrelationPosition()));
		}
		else {
			return false;
		}
	}

	@Override
	public void parseModelXmlElementContent(Element xmlElement) {
		StringText s = StringText.parseModelXmlElementContent(xmlElement);
		String subw = s.getSubfieldsWithCodes("w").getSubfield(0).getContent();
		getReference().setLinkDisplay(subw.charAt(0));
		getReference().setReplacementComplexity(subw.charAt(1));
	}

}
