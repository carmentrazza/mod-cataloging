/*
 * (c) LibriCore
 * 
 * $Author: wimc $
 * $Date: 2005/03/10 08:55:28 $
 * $Locker:  $
 * $Name:  $
 * $Revision: 1.1 $
 * $Source: /source/LibriSuite/src/librisuite/bean/cataloguing/bibliographic/codelist/TargetAudience.java,v $
 * $State: Exp $
 */
package librisuite.bean.cataloguing.bibliographic.codelist;

import librisuite.hibernate.T_TRGT_AUDNC;

/**
 * @author Wim Crols
 * @version $Revision: 1.1 $, $Date: 2005/03/10 08:55:28 $
 * @since 1.0
 */
public class TargetAudience extends CodeListBean {

	public TargetAudience() {
		super(T_TRGT_AUDNC.class);
	}

}