package org.esupportail.lecture.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.lecture.dao.DaoService;
import org.esupportail.lecture.domain.beans.CategoryBean;
import org.esupportail.lecture.domain.beans.ContextBean;
import org.esupportail.lecture.domain.beans.ItemBean;
import org.esupportail.lecture.domain.beans.SourceBean;
import org.esupportail.lecture.domain.beans.UserBean;
import org.esupportail.lecture.domain.model.Channel;
import org.esupportail.lecture.exceptions.domain.CategoryNotLoadedException;
import org.esupportail.lecture.exceptions.domain.CategoryNotVisibleException;
import org.esupportail.lecture.exceptions.domain.CategoryProfileNotFoundException;
import org.esupportail.lecture.exceptions.domain.ContextNotFoundException;
import org.esupportail.lecture.exceptions.domain.DomainServiceException;
import org.esupportail.lecture.exceptions.domain.InfoDomainException;
import org.esupportail.lecture.exceptions.domain.InfoExternalException;
import org.esupportail.lecture.exceptions.domain.InternalDomainException;
import org.esupportail.lecture.exceptions.domain.InternalExternalException;
import org.esupportail.lecture.exceptions.domain.SourceNotLoadedException;
import org.esupportail.lecture.exceptions.domain.TreeSizeErrorException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Class to test calls to facadeService instead of web interface or command-line
 * @author gbouteil
 *
 */
public class DomainTest {
	protected static final Log log = LogFactory.getLog(DomainTest.class); 
	private static FacadeService facadeService;

	
	/* Controller local variables */
	private static String userId = "bourges";
	private static String contextId;
	private static List<String> categoryIds;
	private static String itemId;
	private static String sourceId;
	/**
	 * @param args non argumet needed
	 * @throws InternalExternalException 
	 * @throws DomainServiceException 
	 */
	public static void main(String[] args)  {
		ClassPathResource res = new ClassPathResource("properties/applicationContext.xml");
		XmlBeanFactory factory = new XmlBeanFactory(res);
		facadeService = (FacadeService)factory.getBean("facadeService");
		
		try {
			/* Test alternative behavior */
//			testGetContextBis("ccc");
//			testGetVisibleSourceAlternativeWay(); 
			
			/* Test normal behavior */
			testGetConnectedUser();
			testGetContext();
			testGetVisibleCategories();
			testGetVisibleSources();
			testGetItems();
		
			/* small actions */
//			testMarkItemAsRead();
//			testSetTreeSize();
//			testFoldCategory();
		} catch (InternalExternalException e) {
			System.out.println("\n!!! EXCEPTION !!!");
			System.out.println("\n!!! Catching InternalExternalException");
			e.printStackTrace();
		}catch (InfoDomainException e) {
			System.out.println("\n!!! EXCEPTION !!!");
			System.out.println("\n!!! Catching InfoDomainException");
			e.printStackTrace();
		} catch (InternalDomainException e) {
			System.out.println("\n!!! EXCEPTION !!!");
			System.out.println("\n!!! Catching InternalDomainException");
			e.printStackTrace();
		} catch (DomainServiceException e) {
			System.out.println("\n!!! EXCEPTION !!!");
			System.out.println("\n!!! Catching DomainServiceException");
			e.printStackTrace();
		}
		

	
	}






/*
 * M�thodes de Test
 */

	/**
	 * Test of servide "getConnectedUser"
	 * @throws InternalExternalException 
	 */
	private static void testGetConnectedUser() throws InternalExternalException {
		printIntro("getConnectedUser");
		String userIdLocal = facadeService.getConnectedUserId();
		UserBean user = facadeService.getConnectedUser(userIdLocal);
		System.out.println(user.toString());
	}
	
	/**
	 * Test of service "getContext"
	 * @throws InternalExternalException 
	 * @throws ContextNotFoundException 
	 * @throws DomainServiceException 
	 */
	private static void testGetContext() throws InternalExternalException, ContextNotFoundException{
		printIntro("getContext");
		contextId = facadeService.getCurrentContextId();
		ContextBean context = facadeService.getContext(userId,contextId);
		System.out.println(context.toString());
	}
	private static void testGetContextBis(String cid) throws ContextNotFoundException  {
		printIntro("getContext");
		ContextBean context = facadeService.getContext(userId,cid);
		System.out.println(context.toString());
	}

	/**
	 * Test of service "getCategories"
	 * @throws ContextNotFoundException 
	 * @throws DomainServiceException 
	 */
	private static void testGetVisibleCategories() throws ContextNotFoundException,DomainServiceException{
		printIntro("getVisibleCategories");
		List<CategoryBean> categories = facadeService.getVisibleCategories(userId, contextId);
		categoryIds = new ArrayList<String>();
		for(CategoryBean cat : categories){
			categoryIds.add(cat.getId());
			System.out.println(" **** categorie ****");
			System.out.println(cat.toString());
		}
		
	}
	
	/**
	 * Test of service "getSources"
	 * @throws InternalDomainException 
	 * @throws CategoryNotVisibleException 
	 * @throws CategoryProfileNotFoundException 
	 * @throws CategoryNotLoadedException 
	 * @throws DomainServiceException 
	 */
	private static void testGetVisibleSources() throws CategoryProfileNotFoundException, CategoryNotVisibleException, InternalDomainException, CategoryNotLoadedException  {
		printIntro("getVisibleSources");
		for(String catId : categoryIds){
			System.out.println(" **** cat "+catId+" **********");
			List<SourceBean> sources = facadeService.getVisibleSources(userId, catId);
			for(SourceBean so : sources){
				System.out.println("  **** source ****");
				System.out.println(so.toString());
				sourceId = so.getId();
			}
		}
		
	}


	/**
	 *  Test of service "getSources" in an alternative way :
	 *  - the parent category has not been got before
	 * @throws InternalExternalException 
	 * @throws ContextNotFoundException 
	 * @throws DomainServiceException 
	 * @throws InternalExternalException 
	 * @throws InternalDomainException 
	 * @throws CategoryNotVisibleException 
	 * @throws CategoryProfileNotFoundException 
	 * @throws CategoryNotLoadedException 
	 */
	private static void testGetVisibleSourceAlternativeWay() throws ContextNotFoundException, InternalExternalException, CategoryProfileNotFoundException, CategoryNotVisibleException, InternalDomainException, CategoryNotLoadedException  {	
		testGetContext();	
		printIntro("getVisibleSources - alternative way");
		categoryIds = new ArrayList<String>();
		categoryIds.add("cp1");
		categoryIds.add("cp2");
		for(String catId : categoryIds){
		System.out.println(" **** cat "+catId+" **********");
		List<SourceBean> sources = facadeService.getVisibleSources(userId, catId);
		for(SourceBean so : sources){
			System.out.println("  **** source ****");
			System.out.println(so.toString());
			sourceId = so.getId();
		}
	}
		// TODO Auto-generated method stub
		
	}


	/**
	 * Test of service "getItems"
	 * @throws InternalDomainException 
	 * @throws SourceNotLoadedException 
	 * @throws DomainServiceException 
	 */
	private static void testGetItems() throws SourceNotLoadedException, InternalDomainException  {
		printIntro("getItems");
		System.out.println(" **** source "+sourceId+" **********");
		List<ItemBean> items = facadeService.getItems(userId,sourceId);
		for(ItemBean it : items){
			System.out.println("  **** item ****");
			System.out.println(it.toString());
			itemId = it.getId();
		}
		
	}
	
	/**
	 * Test of service markItemAsRead and markItemAsUnread
	 * @throws InternalDomainException 
	 * @throws SourceNotLoadedException 
	 * @throws DomainServiceException 
	 */
	private static void testMarkItemAsRead() throws InternalDomainException, SourceNotLoadedException {
		printIntro("markItemAsRead");
		System.out.println("Marquage de l'item "+itemId+" comme lu");
		facadeService.marckItemAsRead(userId, "un", itemId);
		testGetItems();
		System.out.println("Marquage de l'item "+itemId+" comme non lu");
		facadeService.marckItemAsUnread(userId, "un", itemId);
		testGetItems();
		
		
	}


	/**
	 * Test of service setTreeSize
	 * @throws TreeSizeErrorException 
	 * @throws ContextNotFoundException 
	 * @throws InternalExternalException 
	 * @throws DomainServiceException 
	 * @throws InternalExternalException 
	 */
	private static void testSetTreeSize() throws ContextNotFoundException, TreeSizeErrorException, InternalExternalException {
		printIntro("setTreeSize");
		int newTreeSize = 10;
		System.out.println("Set tree size to "+newTreeSize);
		facadeService.setTreeSize(userId,contextId,newTreeSize);
		testGetContext();	
	}
	

	/**
	 * Test of service foldCategory and unfoldCategory
	 * @throws DomainServiceException 
	 * @throws DomainServiceException 
	 */
	private static void testFoldCategory() throws DomainServiceException {
		printIntro("foldCategory");
		System.out.println("Pliage de la categorie cp1 (deja pli�e) => WARN");
		facadeService.foldCategory(userId, contextId, "cp1");
		System.out.println("Depliage de la categorie cp1 \n");
		facadeService.unFoldCategory(userId, contextId, "cp1");
		testGetVisibleCategories();
		System.out.println("Pliage de la categorie cp1 \n");
		facadeService.foldCategory(userId, contextId, "cp1");
		testGetVisibleCategories();
		
		
	}






	
	
	/**
	 * Affichage du service � tester
	 * @param nomService nom du service � tester
	 */
	private static void printIntro(String nomService){
		System.out.println("******************************************************");
		System.out.println("Test du service -"+nomService+"- \n");
	}
	
	/**
	 * @return facadeService
	 */
	public FacadeService getFacadeService() {
		return facadeService;
	}

	/**
	 * @param service facadeService
	 */
	public void setFacadeService(FacadeService service) {
		DomainTest.facadeService = service;
	}
}
