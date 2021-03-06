package org.folio.cataloging.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.folio.cataloging.action.LibrisuiteAction;
import org.folio.cataloging.business.*;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.persistence.CollectionMaster;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.folio.cataloging.dao.persistence.T_CLCTN_MST_TYP;
import org.folio.cataloging.dao.persistence.T_CLCTN_TYP;
import org.folio.cataloging.dao.persistence.T_STS_CLCTN_TYP;
import org.folio.cataloging.form.CollectionsMasterForm;
import org.folio.cataloging.dao.common.HibernateUtil;
import org.folio.cataloging.Global;

public class DAOCollectionMaster extends HibernateUtil 
{
	private static Log logger = LogFactory.getLog(DAOCollectionMaster.class);
	
	private static final String SORT_NAME_ITA_CODE = "nameIta";
	private static final String SORT_TYPOLOGY_CODE = "typologyCode";
	private static final String SORT_STATUS_CODE   = "statusCode";
	private static final String SORT_ASC_CODE      = "ASC";
	
	public DAOCollectionMaster() {
		super();
	}
	
	public void persistCollectionMaster( CollectionMaster collection) throws DataAccessException 
	{
		CollectionMaster collection2;
		if (collection.getIdCollection()==0 || loadCollectionMaster(collection.getIdCollection()).size() == 0) {
			persistByStatus(collection);
		} else {
			collection2 = (CollectionMaster) loadCollectionMaster(collection.getIdCollection()).get(0);
			collection2.markChanged();
			persistByStatus(collection2);
		}
	}

	public List loadCollectionMaster(int idCollection) throws DataAccessException 
	{
		List result = null;
	    result= find(
				" from CollectionMaster as ct where ct.idCollection ="
				+ idCollection 
				+ " order by ct.idCollection");
		return result;
	}
	
	public int getIdCollectionMST() throws DataAccessException 
	{
		List l = null;
		int progress = 0;
		Session s = currentSession();

		try {
			l = s.find("select max(a.idCollection) " 
					+ "from CollectionMaster a");
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		if(l.get(0)!=null){
			progress = new Integer(l.get(0).toString()).intValue()+1;	
		}
		else progress= progress+1;
				
		return progress;
	}
	
	public List loadCollectionMaster() throws DataAccessException 
	{
		Session s = currentSession();
		s.clear();
		try {
			s.flush();
		} catch (HibernateException e) {
			e.printStackTrace();
			throw new DataAccessException(); 
		}
		
		List result = null;
		result= find(" from CollectionMaster as ct order by ct.idCollection");
		return result;
	}
	
	public List loadByDescription(String nameIta) throws DataAccessException 
	{
		List result = null;
        nameIta = nameIta.toLowerCase();
		String description="'"+nameIta+"%'";
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct "
					+ " from CollectionMaster as ct,T_CLCTN_MST_TYP as mst " 
					+ " where ct.nameIta = mst.code and lower(mst.longText) like "
					+ description);
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public List loadByTypology(String typology) throws DataAccessException 
	{
		List result = null;
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct "
					+ " from CollectionMaster as ct,T_CLCTN_TYP as mst " 
					+ " where ct.typologyCode ='"
					+ typology+"'");
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public CollectionMaster loadMST(int idCollection)throws DataAccessException 
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		java.sql.ResultSet rs = null;
		Session s = currentSession();
		String query = "";
		List l = null;
		CollectionMaster co =null;
		try {
			connection = s.connection();
			query = "Select * from   " + System.getProperty(Global.SCHEMA_CUSTOMER_KEY) + ".T_CLCTN_MST where  " + System.getProperty(Global.SCHEMA_CUSTOMER_KEY) + ".T_CLCTN_MST.CLCTN_MST_CDE="	+ idCollection;
		    stmt = connection.prepareStatement(query);
			rs = stmt.executeQuery();
			l = new ArrayList();
			while (rs.next()) {
				co = new CollectionMaster();
				co.setIdCollection(rs.getInt("CLCTN_MST_CDE"));
				co.setNameIta(rs.getInt("NME_MST_CDE"));
				co.setStatusCode(rs.getInt("STUS_CDE"));
				co.setDateCreation(rs.getDate("CRTN_DTE"));
				co.setDateCancel(rs.getDate("CNCL_DTE"));
				co.setTypologyCode(rs.getString("TPLG_CDE"));
				co.markUnchanged();
				l.add(co);
			}
		} catch (HibernateException e) {
			logAndWrap(e);

		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				if (rs != null)	rs.close();
				if (stmt != null) stmt.close();
			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
		return co;
	}
	
//	public List orderMstCollection(String colonna, String tipoOrdinamento,Locale locale) throws DataAccessException
	public List orderMstCollection(CollectionsMasterForm form, Locale locale) throws DataAccessException
	{
		List result = new ArrayList();
		List result2 = new ArrayList();
		
		String query = workQuery(form.getTypologyCode(), form.getSearchNome(), form.getSearchId(), form.getColonna(), form.getStatus());
		
		try {
			Session s = currentSession();
			Query q = s.createQuery(query);
			result = q.list();
			
			Iterator iter = result.iterator();
			while (iter.hasNext()) {
				CollectionMaster rawMaster = (CollectionMaster) iter.next();
				MasterListElement rawMasterListElement = new MasterListElement(rawMaster);
				rawMasterListElement.setIdCollection(rawMaster.getIdCollection());
				rawMasterListElement.setNameIta(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getNameIta(),T_CLCTN_MST_TYP.class,locale));
				rawMasterListElement.setStatusCode(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getStatusCode(),T_STS_CLCTN_TYP.class,locale));
				rawMasterListElement.setTypologyCode(LibrisuiteAction.getDaoCodeTable().getLongText(rawMaster.getTypologyCode(),T_CLCTN_TYP.class,locale));
				if(loadHRCY(rawMaster.getIdCollection()).size()>0)
					rawMasterListElement.setHierarchy(true);
				else
					rawMasterListElement.setHierarchy(false);
				rawMasterListElement.setCountMst(countCollectionFromRecordUses(rawMaster.getIdCollection()));
				result2.add(rawMasterListElement);
			}

		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return result2;
		return sortMasterList(form.getStatus(), form.getColonna(), result2);
	}

	private String workQuery(String typologyCode, String searchNome, String searchId, String colonna, String status) 
	{
		StringBuffer buffer = new StringBuffer();
		String query = "select distinct ct from CollectionMaster as ct ";
		String sort = " order by ct." + colonna + " " + status;
		
		if (typologyCode!=null && !"0  ".equals(typologyCode)){
			buffer.append("ct.typologyCode ='" + typologyCode+"'");
		}else if (searchNome!=null && searchNome.trim().length()>0) {
			query = query + ", T_CLCTN_MST_TYP as mst";
			buffer.append(" ct.nameIta = mst.code and lower(mst.longText) like '" + searchNome.toLowerCase() + "%'");
		}else if (searchId!=null && searchId.trim().length()>0) {
//			buffer.append("ct.T_CLCTN_MST.CLCTN_MST_CDE =" + searchId);
			buffer.append("ct.idCollection =" + searchId);
		}
		
		if (buffer.toString().trim().length()>0){
			buffer.insert(0, query + " where ");
		}else {
			buffer.insert(0, query);
		}
		
		buffer.append(sort);
		
		logger.debug("Collections master --> Query eseguita per l'ordinamento : " + buffer.toString());
		
		return buffer.toString();
	}
	
//	20110203 inizio: ordinamento lista
	/**
	 * Il metodo ordina la lista delle collection master tenendo conto 
	 * non dei codici ma delle DESCRIZIONI di Status, Typology e Nome collection
	 */
	public List sortMasterList(String orderType, String orderField, List result2) 
	{
		logger.debug("Ordinamento richiesto : " + orderType);
		logger.debug("Campo di ordinamento  : " + orderField);
		
		if (SORT_ASC_CODE.equalsIgnoreCase(orderType)){
			if (SORT_NAME_ITA_CODE.equalsIgnoreCase(orderField)){
				Collections.sort(result2, new ComparatorMasterNameAsc());
			}else if (SORT_TYPOLOGY_CODE.equalsIgnoreCase(orderField)){
				Collections.sort(result2, new ComparatorMasterTypologyAsc());
			}else if (SORT_STATUS_CODE.equalsIgnoreCase(orderField)){
				Collections.sort(result2, new ComparatorMasterStatusAsc());
			}
		}else {
			if (SORT_NAME_ITA_CODE.equalsIgnoreCase(orderField)){
				Collections.sort(result2, new ComparatorMasterNameDesc());
			}else if (SORT_TYPOLOGY_CODE.equalsIgnoreCase(orderField)){
				Collections.sort(result2, new ComparatorMasterTypologyDesc());
			}else if (SORT_STATUS_CODE.equalsIgnoreCase(orderField)){
				Collections.sort(result2, new ComparatorMasterStatusDesc());
			}
		}
		return result2;
	}
//	20110203 fine
	
	
	public List getListMastersElement(Locale locale) throws DataAccessException 
	{
		List result = new ArrayList();
		List listAllCollection = loadCollectionMaster();
		Iterator iter = listAllCollection.iterator();
		while (iter.hasNext()) {
			CollectionMaster rawMaster = (CollectionMaster) iter.next();
			MasterListElement rawMasterListElement = new MasterListElement(rawMaster);
			rawMasterListElement.setIdCollection(rawMaster.getIdCollection());
			rawMasterListElement.setNameIta(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getNameIta(),T_CLCTN_MST_TYP.class,locale));
			rawMasterListElement.setTypologyCode(LibrisuiteAction.getDaoCodeTable().getLongText(rawMaster.getTypologyCode(),T_CLCTN_TYP.class,locale));
			rawMasterListElement.setStatusCode(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getStatusCode(),T_STS_CLCTN_TYP.class,locale));
			if(loadHRCY(rawMaster.getIdCollection()).size()>0)
				rawMasterListElement.setHierarchy(true);
			else
				rawMasterListElement.setHierarchy(false);
			rawMasterListElement.setCountMst(countCollectionFromRecordUses(rawMaster.getIdCollection()));
			result.add(rawMasterListElement);
		}
		return result;
	}
	
	public List getListMastersElementById(int idCollection,Locale locale) throws DataAccessException 
	{
		List result = new ArrayList();
		List result2 = new ArrayList();
		result = loadCollectionMaster(idCollection);
		Iterator iter = result.iterator();
		while (iter.hasNext()) {
			CollectionMaster rawMaster = (CollectionMaster) iter.next();
			MasterListElement rawMasterListElement = new MasterListElement(rawMaster);
			rawMasterListElement.setIdCollection(rawMaster.getIdCollection());
			rawMasterListElement.setNameIta(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getNameIta(),T_CLCTN_MST_TYP.class,locale));
			rawMasterListElement.setTypologyCode(LibrisuiteAction.getDaoCodeTable().getLongText(rawMaster.getTypologyCode(),T_CLCTN_TYP.class,locale));
			rawMasterListElement.setStatusCode(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getStatusCode(),T_STS_CLCTN_TYP.class,locale));
			if(loadHRCY(rawMaster.getIdCollection()).size()>0)
				rawMasterListElement.setHierarchy(true);
			else
				rawMasterListElement.setHierarchy(false);
			rawMasterListElement.setCountMst(countCollectionFromRecordUses(rawMaster.getIdCollection()));
			result2.add(rawMasterListElement);
		}
		return result2;
	}
	
	public List getListMastersElementByDescription(String nameIta,Locale locale) throws DataAccessException 
	{
		List result = new ArrayList();
		List result2 = new ArrayList();
		result = loadByDescription(nameIta);
		Iterator iter = result.iterator();
		while (iter.hasNext()) {
			CollectionMaster rawMaster = (CollectionMaster) iter.next();
			MasterListElement rawMasterListElement = new MasterListElement(rawMaster);
			rawMasterListElement.setIdCollection(rawMaster.getIdCollection());
			rawMasterListElement.setNameIta(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getNameIta(),T_CLCTN_MST_TYP.class,locale));
			rawMasterListElement.setTypologyCode(LibrisuiteAction.getDaoCodeTable().getLongText(rawMaster.getTypologyCode(),T_CLCTN_TYP.class,locale));
			rawMasterListElement.setStatusCode(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getStatusCode(),T_STS_CLCTN_TYP.class,locale));
			if(loadHRCY(rawMaster.getIdCollection()).size()>0)
				rawMasterListElement.setHierarchy(true);
			else
				rawMasterListElement.setHierarchy(false);
			rawMasterListElement.setCountMst(countCollectionFromRecordUses(rawMaster.getIdCollection()));
			result2.add(rawMasterListElement);
		}
	
//----> 20110203 inizio: visualizza le collection ordinate per descrizione nome e non per il codice numerico corrispondente al nome 
		return sortMasterList(SORT_ASC_CODE, SORT_NAME_ITA_CODE, result2);
//		return result2;		
//----> 20110203 fine
	}
	
	public List getListMastersElementByTypology(String typology,Locale locale) throws DataAccessException 
	{
		List result = new ArrayList();
		List result2 = new ArrayList();
		result = loadByTypology(typology);
		Iterator iter = result.iterator();
		while (iter.hasNext()) {
			CollectionMaster rawMaster = (CollectionMaster) iter.next();
			MasterListElement rawMasterListElement = new MasterListElement(rawMaster);
			rawMasterListElement.setIdCollection(rawMaster.getIdCollection());
			rawMasterListElement.setNameIta(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getNameIta(),T_CLCTN_MST_TYP.class,locale));
			rawMasterListElement.setTypologyCode(LibrisuiteAction.getDaoCodeTable().getLongText(rawMaster.getTypologyCode(),T_CLCTN_TYP.class,locale));
			rawMasterListElement.setStatusCode(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getStatusCode(),T_STS_CLCTN_TYP.class,locale));
			if(loadHRCY(rawMaster.getIdCollection()).size()>0)
				rawMasterListElement.setHierarchy(true);
			else
				rawMasterListElement.setHierarchy(false);
			rawMasterListElement.setCountMst(countCollectionFromRecordUses(rawMaster.getIdCollection()));
			result2.add(rawMasterListElement);
		}
//----> 20110203 inizio: visualizza le collection ordinate per descrizione nome e non per il codice numerico corrispondente al nome 
		return sortMasterList(SORT_ASC_CODE, SORT_TYPOLOGY_CODE, result2);
//		return result2;
//----> 20110203 fine
	}
	
	public List loadClientMaster(int collectionCode, String customerCode) throws DataAccessException 
	{
		List result = null;
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct "
					+ " from CLCTN_MST_CSTMR as ct " 
					+ " where ct.collectionCode =" + collectionCode 
					+ " and ct.customerCode= '" + customerCode + "'");
			q.setMaxResults(1);
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public List loadClientMasterById(int collectionCode) throws DataAccessException 
	{
		List result = null;
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct "
					+ " from CLCTN_MST_CSTMR as ct " 
					+ " where ct.collectionCode ="
					+ collectionCode);
			q.setMaxResults(1);
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}

	public List loadMasterByName(int nameIta) throws DataAccessException 
	{
		List result = null;
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct "
					+ " from CollectionMaster as ct " 
					+ " where ct.nameIta ="
					+ nameIta);
			q.setMaxResults(1);
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}

//	inizio
	public void updateObj (CollectionMaster item) throws DataAccessException 
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		Session session = currentSession();
		String query = "";
		
		try {			
			connection = session.connection();
			
			query = "update T_CLCTN_MST i set i.NME_MST_CDE = ?" + 
				    ",i.STUS_CDE = ? " +
				    ",i.CRTN_DTE = ? " +
				    ",i.CNCL_DTE = ? " +
				    ",i.TPLG_CDE = ? " +
				    ",i.MDFY_DTE = ? " +
				    ",i.USR_MDFY = ? " +
				    ",i.YEAR = ? " +
				    ",i.DT_INI_VAL = ? " +
				    ",i.DT_FIN_VAL = ? " +
					" where i.CLCTN_MST_CDE = ? ";
					
			Date dataSqlCreation = null;
			Date dataSqlCancel = null;
			Date dataSqlModify = null;
			if (item.getDateCreation()!=null)
				dataSqlCreation = new Date(item.getDateCreation().getTime());
			if (item.getDateCancel()!=null)
				dataSqlCancel = new Date(item.getDateCancel().getTime());
			if (item.getDateModify()!=null)
				dataSqlModify = new Date(item.getDateModify().getTime());	
			if (item.getDateCreation()!=null)
				stmt = connection.prepareStatement(query);
			
//-------->	20110131 inizio:
			Date dataSqlIniVal = null;
			Date dataSqlFinVal = null;
			if (item.getDateIniVal()!=null){
				dataSqlIniVal = new Date(item.getDateIniVal().getTime());
			}
			if (item.getDateFinVal()!=null){
				dataSqlFinVal = new Date(item.getDateFinVal().getTime());
			}
//-------->	20110131 fine
			
		    stmt.setInt(1, item.getNameIta());
		    stmt.setInt(2, item.getStatusCode());
		    stmt.setDate(3, dataSqlCreation);
		    stmt.setDate(4, dataSqlCancel);
		    stmt.setString(5, item.getTypologyCode());
		    stmt.setDate(6, dataSqlModify);
		    stmt.setString(7, item.getUserModify());
		    stmt.setInt(8, item.getYear());
//--------> 20110131 inizio:
		    stmt.setDate(9, dataSqlIniVal);
		    stmt.setDate(10, dataSqlFinVal);
//--------> 20110131 fine
		    stmt.setInt(11, item.getIdCollection());
		   
			int row = stmt.executeUpdate();
	
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				connection.commit();
				if (stmt != null)
					stmt.close();

			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
	}
	
	public void deleteObj(CollectionMaster collectionMaster, int nameColl) throws DataAccessException 
	{
		Session s = currentSession();
		Transaction tx = null;
		try {
			tx = s.beginTransaction();
//-------->	Cancella collection master
			s.delete("from CollectionMaster as i where i.idCollection =" + collectionMaster.getIdCollection());
			
//-------->	Cancella nome collection master eliminata
//			deleteCollName(collectionMaster.getNameIta());			
//			deleteCollName(nameColl);
			s.delete("from T_CLCTN_MST_TYP as i where i.code =" + nameColl);
//-------->	Se tutto ok COMMIT
			tx.commit();
		} catch (HibernateException e) {
			logAndWrap(e);
			try {
                tx.rollback();
            } catch (HibernateException e1) {
           	 logAndWrap(e1);
            }
		}
	}
	
	public void deleteCollName (int nameIta) throws DataAccessException 
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		Session session = currentSession();
		String query = "";
		try {			
			connection = session.connection();
            query = "delete from T_CLCTN_MST_TYP i where i.TBL_VLU_CDE = ? ";		
			stmt = connection.prepareStatement(query);
		    stmt.setInt(1, nameIta);
			stmt.execute();         
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
	}
	
	public void deleteObjOLD (int idCollection, int nameIta) throws DataAccessException 
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		Session session = currentSession();
		String query = "";
		
		try {			
			connection = session.connection();
			query = "delete from T_CLCTN_MST i where i.CLCTN_MST_CDE = ? ";		
			stmt = connection.prepareStatement(query);
		    stmt.setInt(1, idCollection);
			stmt.execute();
			
            query = "delete from T_CLCTN_MST_TYP i where i.TBL_VLU_CDE = ? ";		
			stmt = connection.prepareStatement(query);
		    stmt.setInt(1, nameIta);
			stmt.execute();         
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				connection.commit();
				if (stmt != null)
					stmt.close();

			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
	}
	
	public List loadHRCY(int collectionCode) throws DataAccessException 
	{
		List result = null;
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct "
					+ " from CLCTN_MST_HRCY as ct " 
					+ " where ct.collectionCode ="
					+ collectionCode);
			q.setMaxResults(1);
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public int countCollectionFromRecordUses(int idCollection) throws DataAccessException 
	{
		List l = find("select count(*) from CLCTN_MST_ACS_PNT apf, CollectionMaster ct " 
				+ " where ct.idCollection = apf.collectionNumber" 
				+ " and apf.collectionNumber = ?", new Object[] { new Integer(
				idCollection) }, new Type[] { Hibernate.INTEGER });
		if (l.size() > 0) {
			return ((Integer) l.get(0)).intValue();
		} else {
			return 0;
		}
	}
	
	public List getListMastersElementByItem(int itemNumber, Locale locale) throws DataAccessException 
	{
		List result = new ArrayList();
		List listAllCollection = getCollectionsByBibItem(itemNumber);
		Iterator iter = listAllCollection.iterator();
		while (iter.hasNext()) {
			CollectionMaster rawMaster = (CollectionMaster) iter.next();
			MasterListElement rawMasterListElement = new MasterListElement(rawMaster);
			rawMasterListElement.setIdCollection(rawMaster.getIdCollection());
			rawMasterListElement.setNameIta(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getNameIta(),T_CLCTN_MST_TYP.class,locale));
			rawMasterListElement.setTypologyCode(LibrisuiteAction.getDaoCodeTable().getLongText(rawMaster.getTypologyCode(),T_CLCTN_TYP.class,locale));
			rawMasterListElement.setStatusCode(LibrisuiteAction.getDaoCodeTable().getLongText((short)rawMaster.getStatusCode(),T_STS_CLCTN_TYP.class,locale));
			if(loadHRCY(rawMaster.getIdCollection()).size()>0)
				rawMasterListElement.setHierarchy(true);
			else
				rawMasterListElement.setHierarchy(false);
			rawMasterListElement.setCountMst(countCollectionFromRecordUses(rawMaster.getIdCollection()));
//-------->	20101014 inizio: devo memorizzare la data di associazione del record
			rawMasterListElement.setDateAssociatedRecord(getRecordAssociatedDate(itemNumber, rawMaster.getIdCollection()));
//-------->	20101014 fine 
			result.add(rawMasterListElement);
		}
		return result;
	}
	
//	20101014 inizio
	public Date getRecordAssociatedDate(int itemNumber, int idCollection) throws DataAccessException 
	{
		Date dateCreation = null; 
		try {
			Session s = currentSession();
			Query q = s.createQuery("Select ct.creationDate from CLCTN_MST_ACS_PNT ct where ct.collectionNumber = " 
					+ idCollection 
					+ " and ct.bibItemNumber = " 
					+ itemNumber);
			q.setMaxResults(1);
			dateCreation = new Date(((Timestamp) q.list().get(0)).getTime());

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return dateCreation;
	}
	
	public List getCollectionsByBibItem(int itemNumber) throws DataAccessException 
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		java.sql.ResultSet rs = null;
		Session s = currentSession();
		String query = "";
		List l = null;

		try {
			connection = s.connection();
			query = "Select distinct ct.* from   " + System.getProperty(Global.SCHEMA_CUSTOMER_KEY) + ".CLCTN_MST_ACS_PNT apf,  " + System.getProperty(Global.SCHEMA_CUSTOMER_KEY) + ".T_CLCTN_MST ct where apf.BIB_ITM_NBR ="
					+itemNumber
					+"  and ct.CLCTN_MST_CDE = apf.CLCTN_MST_CDE";
		    stmt = connection.prepareStatement(query);
			rs = stmt.executeQuery();
			l = new ArrayList();
			while (rs.next()) {
				CollectionMaster co = new CollectionMaster();
				co.setIdCollection(rs.getInt("CLCTN_MST_CDE"));
				co.setNameIta(rs.getInt("NME_MST_CDE"));
				//co.setNameEng(rs.getString("NME_CLCTN_ENG"));
				co.setStatusCode(rs.getInt("STUS_CDE"));
				co.setDateCreation(rs.getDate("CRTN_DTE"));
				co.setDateCancel(rs.getDate("CNCL_DTE"));
				co.setTypologyCode(rs.getString("TPLG_CDE"));
				co.markUnchanged();
				l.add(co);
			}
		} catch (HibernateException e) {
			logAndWrap(e);

		} catch (SQLException e1) {
			logAndWrap(e1);
		} finally {
			try {
				if (rs != null)	rs.close();
				if (stmt != null) stmt.close();
			} catch (SQLException e) {
				logAndWrap(e);
			}
		}
		return l;
	}
}