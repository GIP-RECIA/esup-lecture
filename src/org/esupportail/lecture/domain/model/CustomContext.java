package org.esupportail.lecture.domain.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.lecture.domain.DomainTools;
import org.esupportail.lecture.domain.ExternalService;
import org.esupportail.lecture.exceptions.ComposantNotLoadedException;
import org.esupportail.lecture.exceptions.ContextNotFoundException;
import org.esupportail.lecture.exceptions.ErrorException;


/**
 * Customizations on a context for a user profile 
 * @author gbouteil
 *
 */
public class CustomContext implements CustomElement {

	/*
	 ************************** PROPERTIES *********************************/	
	
	/**
	 * Log instance
	 */
	protected static final Log log = LogFactory.getLog(CustomContext.class);
	
	/**
	 * The context Id of this customization refered to
	 */
	private String contextId;

	/**
	 * The context of this customization referred to, corresponding to the contextId
	 */
	private Context context;
	
	/**
	 * The map of subscribed CustomManagedCategory
	 */
	private Map<String,CustomManagedCategory> subscriptions;
	
	/**
	 * The userprofile parent (used by hibernate)
	 */
	private UserProfile userProfile;
	
	/**
	 * The Id of this CustomContext : for Database
	 */
	private int Id;

	
	/*
	 ************************** INIT *********************************/	
	
	/**
	 * Constructor
	 * @param contextId id of the context refered by this
	 * @param user owner of this
	 */
	public CustomContext(String contextId, UserProfile user) {
		subscriptions = new Hashtable<String,CustomManagedCategory>();
		this.contextId = contextId;
		this.userProfile = user;
	}
	
	/*
	 *************************** METHODS ************************************/

	/**
	 * @param externalService access to externalService
	 * @return list of customCategories defined in this customContext
	 * @throws ContextNotFoundException 
	 * @throws ComposantNotLoadedException 
	 */
	public List<CustomCategory> getSortedCustomCategories(ExternalService externalService) throws ContextNotFoundException, ComposantNotLoadedException{
		// TODO (GB later) rewrite with custom personnal category (+ sorted display)
	
		/* update this customContext with context */
		getContext().updateCustom(this,externalService);
		
		List<CustomCategory> listCustomCategories = new Vector<CustomCategory>();
		for(CustomManagedCategory customCat : subscriptions.values()){
			// later : add other custom elements (imported et personal)
			listCustomCategories.add(customCat);
		}
		return listCustomCategories;
	}
	
	
//	/** 
//	 * Update data contained in this customContext by visibilty evaluation
//	 * on managedCategories, in order to update list of customManagedCategories
//	 * @param externalService
//	 */
//	private void update(ExternalService externalService) {
//		
//		getContext().updateCustomContext(this,externalService);
//		// later :  Personnal Categories
//		
//		Iterator<CustomManagedCategory> iterator = subscriptions.values().iterator();
//		while(iterator.hasNext()){
//			CustomManagedCategory customManagedCategory = iterator.next();
//			customManagedCategory.update(externalService);
//		}	
//	}
	
//	public List<ManagedCategoryProfile> getVisibleManagedCategoryProfile(ExternalService externalService) {
//		
//		return getContext().updateCustomContext(this,externalService);
//		
//	}
	
	/**
	 * Add a custom category to this custom context if no exists after creating it.
	 * @param profile the managed category profile associated to the customCategory
	 */
	public void addCustomManagedCategory(ManagedCategoryProfile profile) {
		String profileId = profile.getId();
		
		if (!subscriptions.containsKey(profileId)){
			CustomManagedCategory customManagedCategory = new CustomManagedCategory(profileId,userProfile);
			subscriptions.put(profileId,customManagedCategory);
			userProfile.addCustomManagedCategory(customManagedCategory);
		}
	}
	
	/**
	 * Remove the managedCategoryProfile from the customContext
	 * @param profile managedCategoryProfile to remove
	 */
	public void removeCustomManagedCategory(ManagedCategoryProfile profile) {
		// TODO (GB) tester avec la BDD
		subscriptions.remove(profile.getId());
		userProfile.removeCustomManagedCategory(profile.getId());
		
	}
	
	/**
	 * @return context refered by this
	 * @throws ContextNotFoundException 
	 */
	public Context getContext() throws ContextNotFoundException {
		if (context == null) {
			context = DomainTools.getChannel().getContext(contextId);
		}
		return context;
	}
	
	/**
	 * @throws ContextNotFoundException 
	 * @see org.esupportail.lecture.domain.model.CustomElement#getName()
	 */
	public String getName() throws ContextNotFoundException {
		return getContext().getName();
	}
	
//	public String getContent() {
//		return getContext().getDescription();
//	}

	
	/* 
	 ************************** ACCESSORS **********************************/

	/**
	 * @return context
	 * @see CustomContext#contextId
	 */
	public String getContextId() {
		return contextId;
	}

	/**
	 * @param contextId : the contextId to set
	 * @see CustomContext#contextId
	 */
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return Id;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		Id = id;
	}

	/**
	 * @return userProfile
	 */
	public UserProfile getUserProfile() {
		return userProfile;
	}

	/**
	 * @param userprofile
	 */
	public void setUserProfile(UserProfile userprofile) {
		this.userProfile = userprofile;
	}

//	public Collection getSubscriptions() {
//		return subscriptions;
//	}
//
//	public void setSubscriptions(Collection subscriptions) {
//		this.subscriptions = subscriptions;
//	}

//
//	
//	public Collection getCreations() {
//		return creations;
//	}
//
//	public void setCreations(Collection creations) {
//		this.creations = creations;
//	}

	

//
//	public Set getFoldedCategories() {
//		return foldedCategories;
//	}
//
//	
//	public void setFoldedCategories(Set foldedCategories) {
//		this.foldedCategories = foldedCategories;
//	}


//	
//	public SortedSet getOrderCategories() {
//		return orderCategories;
//	}
//
//	
//	public void setOrderCategories(SortedSet orderCategories) {
//		this.orderCategories = orderCategories;
//	}



//
//	public Collection getImportations() {
//		return importations;
//	}
//
//	
//	public void setImportations(Collection importations) {
//		this.importations = importations;
//	}



//
//	public int getTreeWinWidth() {
//		return treeWinWidth;
//	}
//
//	public void setTreeWinWidth(int treeWinWidth) {
//		this.treeWinWidth = treeWinWidth;
//	}






}