/*
 * (c) LibriCore
 * 
 * Created on Jul 20, 2004
 * 
 * DAOIndexList.java
 */
package org.folio.cataloging.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.folio.cataloging.business.codetable.ValueLabelElement;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.persistence.IndexMainList;
import org.folio.cataloging.dao.persistence.IndexSubList;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static java.util.stream.Collectors.toList;
import org.folio.cataloging.dao.common.HibernateUtil;
import org.folio.cataloging.Global;
import org.folio.cataloging.business.searching.IndexBean;
import org.folio.cataloging.business.searching.SearchIndexElement;
import org.folio.cataloging.log.MessageCatalog;

/** 
 * @author hansv
 * @version %I%, %G%
 * @since 1.0
 */
public class DAOSearchIndex extends HibernateUtil 
{
	private static final Log logger = LogFactory.getLog(DAOSearchIndex.class);

	/**
	 * Returns a list of all categories belonging to the requested type.
	 *
	 * @param session the session of hibernate
	 * @param indexType the index type, used here as a filter criterion.
	 * @param locale the Locale, used here as a filter criterion.
	 * @return a list of categories and descriptions for index type associated with the requested language.
	 * @throws DataAccessException in case of data access failure.
	 */
	public List<ValueLabelElement> getMainIndex(final Session session,final String indexType, final Locale locale) throws DataAccessException
	{

		try {
			final List<IndexMainList> indices =
					session.find(
						"from IndexMainList as a " +
						"where a.language = ? and a.indexType = '" + indexType +"' order by a.indexKey",
						new Object[] {locale.getISO3Language()}, new Type[] { Hibernate.STRING});
			return indices
					.stream()
					.filter(index -> index.getLanguage().equals(locale.getISO3Language()))
					.map(index -> new ValueLabelElement(String.valueOf(index.getIndexValueCode()),index.getIndexMainName()))
					.collect(toList());

		} catch (final HibernateException exception) {
			logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
			return Collections.emptyList();
		}

	}

	public List getSubIndex(Locale locale, char indexType) throws DataAccessException 
	{
		List l = null;
		List result = new ArrayList();
		Session s = currentSession();

		try {
			l =
			s.find( 
				"from IndexSubList as a " +
				"where a.language = ? and a.indexType = '" + indexType +"'" +
				/*modifica barbara ordinamento indici*/
				" order by a.indexSubName",
				new Object[] {
							locale.getISO3Language()},
						new Type[] { Hibernate.STRING});
		} catch (HibernateException e) {
			logAndWrap(e);
		}

		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			IndexSubList aRow = (IndexSubList) iter.next();
				result.add(
					new SearchIndexElement(
						aRow.getIndexValueCode(),
						aRow.getIndexSubValueCode(),
						aRow.getIndexSearchCode(),
						aRow.getIndexSubName()
						));
		}
		return result;
	}
	
	public List getSubIndex(Locale locale, char indexType, int ind) throws DataAccessException 
	{
		List l = null;
		List result = new ArrayList();
		Session s = currentSession();
		try {
			l =
			s.find( 
				"from IndexSubList as a " +
				"where a.language = ? and a.indexType = '" + indexType +"'"+
				"and a.indexValueCode = '" + ind +"'" +
				/*modifica barbara ordinamento indici*/
				" order by a.indexSubName",
				new Object[] {
						locale.getISO3Language()},
						new Type[] { Hibernate.STRING});
		} catch (HibernateException e) {
			logAndWrap(e);
		}

		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			IndexSubList aRow = (IndexSubList) iter.next();
				result.add(
					new SearchIndexElement(
						aRow.getIndexValueCode(),
						aRow.getIndexSubValueCode(),
						aRow.getIndexSearchCode(),
						aRow.getIndexSubName()
						));
		}
		return result;
	}
	
	/**
	 * Il metodo carica tutti gli indici main (le categorie) data la lingua(1=eng 5=ita) e il tipo P=Principali S=Secondari      
	 * @param lang
	 * @param indexType
	 * @return
	 * @throws DataAccessException
	 */
	public List getMainIndexByType(String lang, String indexType) throws DataAccessException
	{
		PreparedStatement stmt = null;
		java.sql.ResultSet js = null;
		IndexBean indexBean = null;
		List indexMainList = new ArrayList(); 
		
		try{
			Session s = currentSession();
			Connection connection = s.connection();
			stmt =	connection.prepareStatement(
					"Select b.INDEX_SEARCH_CODE, b.INDEX_NAME, c.INDEX_TRLTN_NBR "
					+ "from  " + System.getProperty(Global.SCHEMA_SUITE_KEY) + ".INDEX_MAIN_LIST_TBL c,  " + System.getProperty(Global.SCHEMA_SUITE_KEY) + ".INDEX_TRLTN_TBL b "
					+ "where c.INDEX_TYPE = '" + indexType + "'"
					+ " and b.IDX_LIST_LC_MAD_CDE = 'LC'"
					+ " and c.INDEX_TRLTN_NBR = b.TRLTN_KEY_NBR "
					+ " and b.language = " + lang
					+ " order by 2"
				);

			js = stmt.executeQuery();
			while (js.next()) {
				indexBean = new IndexBean(js.getString("INDEX_SEARCH_CODE"),js.getString("INDEX_NAME"),js.getInt("INDEX_TRLTN_NBR"), indexType, lang);
				indexMainList.add(indexBean);				
			}
		} catch (HibernateException e) {
			logAndWrap(e);

		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				if (js != null)
					js.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
		return indexMainList;
	}
	
	/**
	 * Il metodo carica tutti gli indici child dato l'indice main, la lingua(1=eng 5=ita) e il tipo P=Principali S=Secondari      
	 * @param main
	 * @param indexType
	 * @param lang
	 * @return
	 * @throws DataAccessException
	 */
	public List getChildIndexByMain(int main, String indexType, String lang) throws DataAccessException
	{
		PreparedStatement stmt = null;
		java.sql.ResultSet js = null;
		IndexBean indexBean = null;
		List indexChildList = new ArrayList(); 
		
		try{
			Session s = currentSession();
			Connection connection = s.connection();
			stmt =	connection.prepareStatement(		
				    "select b.INDEX_SEARCH_CODE, b.INDEX_NAME"
				    + " from  " + System.getProperty(Global.SCHEMA_SUITE_KEY) + ".INDEX_SUB_LIST_TBL a,  " + System.getProperty(Global.SCHEMA_SUITE_KEY) + ".INDEX_MAIN_LIST_TBL c,  " + System.getProperty(Global.SCHEMA_SUITE_KEY) + ".INDEX_TRLTN_TBL b"
				    + " where a.INDEX_VLU_CDE = c.INDEX_VLU_CDE"
				    + " and a.INDEX_TRLTN_NBR = b.TRLTN_KEY_NBR"
				    + " and c.INDEX_TRLTN_NBR = " + main  
				    + " and c.INDEX_TYPE = '" + indexType + "'"
				    + " and c.INDEX_TYPE = a.INDEX_TYPE"
				    + " and b.IDX_LIST_LC_MAD_CDE = 'LC'"
				    + " and b.language = " + lang
				    + " order by 1"
				);

			js = stmt.executeQuery();
			while (js.next()) {
				indexBean = new IndexBean(js.getString("INDEX_SEARCH_CODE"),js.getString("INDEX_NAME"),0, indexType, lang);
				indexChildList.add(indexBean);				
			}
		} catch (HibernateException e) {
			logAndWrap(e);

		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				if (js != null)
					js.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
		return indexChildList;
	}	
}