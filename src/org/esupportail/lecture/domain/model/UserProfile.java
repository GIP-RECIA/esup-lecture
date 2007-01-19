package org.esupportail.lecture.domain.model;


import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.lecture.domain.DomainTools;
import org.esupportail.lecture.domain.ExternalService;
import org.esupportail.lecture.domain.beans.User;
import org.esupportail.lecture.exceptions.domain.CategoryNotVisibleException;
import org.esupportail.lecture.exceptions.domain.ComputeFeaturesException;
import org.esupportail.lecture.exceptions.domain.ContextNotFoundException;
import org.esupportail.lecture.exceptions.domain.CustomCategoryNotFoundException;
import org.esupportail.lecture.exceptions.domain.CustomSourceNotFoundException;
import org.esupportail.lecture.exceptions.domain.ManagedCategoryProfileNotFoundException;



/**
 * Class where are defined user profile (and customizations ...)
 * @author gbouteil
 *
 */
public class UserProfile {
	
	/*
	 ************************** PROPERTIES *********************************/	
	
	/**
	 * Log instance
	 */
	protected static final Log log = LogFactory.getLog(UserProfile.class);
	
	/**
	 * Id of the user, get from externalService request by USER_ID, defined in the channel config
	 * @see org.esupportail.lecture.domain.DomainTools#USER_ID
	 * @see ChannelConfig#loadUserId()
	 */
	private String userId;
	
	/**
	 * Hashtable of CustomContexts defined for the user, indexed by contexID.
	 */
	private Map<String,CustomContext> customContexts = new Hashtable<String,CustomContext>();

	/**
	 * Hashtable of CustomManagedCategory defined for the user, indexed by ManagedCategoryProfilID.
	 */
	private Map<String,CustomCategory> customCategories = new Hashtable<String, CustomCategory>();

	/**
	 * Hashtable of CustomSource defined for the user, indexed by SourceProfilID.
	 */
	private Map<String,CustomSource> customSources = new Hashtable<String,CustomSource>();
	/**
	 * Database Primary Key
	 */
	private long userProfilePK;

	
	/*
	 ************************** Initialization ************************************/
	

	
	/**
	 * Constructor
	 * @param userId
	 */
	public UserProfile(String userId){
	   	if (log.isDebugEnabled()){
    		log.debug("UserProfile("+userId+")");
    	}
		this.setUserId(userId);
	}

	/**
	 * Default Constructor
	 */
	public UserProfile(){
		if (log.isDebugEnabled()){
    		log.debug("UserProfile("+userId+")");
    	}
	}

	/*
	 *************************** METHODS ************************************/

	
	/**
	 * Return the customContext identified by the contextId 
	 * if exists in userProfile, else create it.
	 * @param contextId identifier of the context refered by the customContext
	 * @return customContext (or null)
	 * @throws ContextNotFoundException 
	 */
	public CustomContext getCustomContext(String contextId) throws ContextNotFoundException{
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - getCustomContext("+contextId+")");
    	}
		CustomContext customContext = customContexts.get(contextId);
		if (customContext == null){
			if (!DomainTools.getChannel().isThereContext(contextId)) {
				String errorMsg = "Context "+contextId+ " is not found in Channel";
				log.error(errorMsg);
				throw new ContextNotFoundException(errorMsg);
			}
			customContext = new CustomContext(contextId,this);
			addCustomContext(customContext);
		}
		
		return customContext;
	}
	
	/**
	 * @param contextId
	 * @return true if this userProfile contains the customContext identified by contextId
	 */
	public boolean containsCustomContext(String contextId) {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - containsCustomContext("+contextId+")");
    	}
		return customContexts.containsKey(contextId);
	}

	/**
	 * Return the customCategory identifed by the category id
	 * if exist,else,create it.
	 * @param categoryId identifier of the category refered by the customCategory
	 * @param ex access to externalService
	 * @return customCategory (or null)
	 * @throws CategoryNotVisibleException 
	 * @throws ManagedCategoryProfileNotFoundException 
	 * @throws CustomCategoryNotFoundException 
	 */
	public CustomCategory getCustomCategory(String categoryId,ExternalService ex) 
		throws ManagedCategoryProfileNotFoundException, CategoryNotVisibleException, CustomCategoryNotFoundException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - getCustomCategory("+categoryId+")");
    	}
		// TODO (GB later) revoir avec customManagedCategory et customPersonalCategory
		CustomCategory customCategory = customCategories.get(categoryId);
		if(customCategory == null){
			updateCustomContextsForOneManagedCategory(categoryId,ex);
			customCategory = customCategories.get(categoryId);
			if (customCategory == null){
				String errorMsg = "CustomCategory associated to category "+categoryId
					+" is not found in user profile "+userId+"\nwhereas an updateCustomContextForOneManagedCategory " +
							"has done and category seems visible to user profile "+userId
							+".\nPerhaps this categoryProfile is not defined in current context.";
				log.error(errorMsg);
				throw new CustomCategoryNotFoundException(errorMsg);
			}
		}
		return customCategory;
	}
	
	/**
	 * Update every customContext of this userProfile for (only one)categoryProfile identified by categoryProfileId
	 * @param categoryProfileId
	 * @param ex access to externalService
	 * @throws ManagedCategoryProfileNotFoundException
	 * @throws CategoryNotVisibleException
	 */
	protected void updateCustomContextsForOneManagedCategory(String categoryProfileId,ExternalService ex) 
		throws ManagedCategoryProfileNotFoundException, CategoryNotVisibleException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - updateCustomContextsForOneManagedCategory("+categoryProfileId+",ex)");
    	}
		
		ManagedCategoryProfile mcp = DomainTools.getChannel().getManagedCategoryProfile(categoryProfileId);
		Set<Context> contexts = mcp.getContextsSet();
		boolean categoryIsVisible = true;
		// For all contexts refered by the managedCategoryProfile
		for(Context context : contexts){
			String contextId = context.getId();
			// Update on customContexts existing in userProfile
			if (containsCustomContext(contextId)) {
				CustomContext customContext;
				try {
					customContext = getCustomContext(contextId);
				
					if (!mcp.updateCustomContext(customContext, ex)){
						categoryIsVisible = false;
					} else {
						DomainTools.getDaoService().updateCustomContext(customContext);
					}
				} catch (ContextNotFoundException e) {
					log.error("Impossible to get CustomContext associated to context "+ contextId
							+" for managedCategoryProfile "+mcp.getId()+" because context not found",e);
				} catch (ComputeFeaturesException e) {
					log.error("Impossible to update CustomContext associated to context "+ contextId
							+" for managedCategoryProfile "+mcp.getId()+"because an error occured when computing features",e);
				}
			}
		}
		if (!categoryIsVisible){
			String errorMsg = "Category "+categoryProfileId+" is not visible for user profile "+userId;
			log.error(errorMsg);
			throw new CategoryNotVisibleException(errorMsg);
		}
		
	}
	
	/**
	 * Return the customSource identified by the source Id
	 * @param sourceId identifier of the source refered by the customSource
	 * @return customSource
	 * @throws CustomSourceNotFoundException 
	 */
	public CustomSource getCustomSource(String sourceId) throws CustomSourceNotFoundException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - getCustomSource("+sourceId+")");
    	}
	   	// TODO (GB later) revoir avec customManagedSource et customPersonalSource
		CustomSource customSource = 
			customSources.get(sourceId);
		if(customSource == null){
			String errorMsg = "CustomSource "+sourceId+" is not found in userProfile "+this.userId;
			log.error(errorMsg);
			throw new CustomSourceNotFoundException(errorMsg);
		}
		
		return customSource;
	}
	
	/**
	 * Add a customContext to this userProfile
	 * @param customContext
	 */
	protected void addCustomContext(CustomContext customContext){
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - addCustomContext("+customContext.getElementId()+")");
    	}
		customContexts.put(customContext.getElementId(),customContext);
		DomainTools.getDaoService().updateUserProfile(this);
	}
	
	/**
	 * Remove a customContext from this userProfile
	 * @param contextId id of the customContext to add
	 */
	protected void removeCustomContext(String contextId){
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - removeCustomContext("+contextId+")");
    	}
	   	CustomContext cctx = customContexts.remove(contextId);
		if( cctx!= null) {
			DomainTools.getDaoService().deleteCustomContext(cctx);
		}
	}
	
	/**
	 * Add a customCategory to this userProfile
	 * @param customCategory customCategory to add
	 */
	protected void addCustomCategory(CustomCategory customCategory){
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - addCustomCategory("+customCategory.getElementId()+")");
    	}
		String id = customCategory.getElementId();
		customCategories.put(id,customCategory);
	}
	

	/**
	 * Remove a customCategory from this userProfile
	 * @param categoryId
	 */
	protected void removeCustomCategory(String categoryId){
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - removeCustomCategory("+categoryId+")");
    	}
	   	CustomCategory ccat = customCategories.remove(categoryId);
		if( ccat!= null) {
			DomainTools.getDaoService().deleteCustomCategory(ccat);
		}
	}
	
	/**
	 * Add a customSource to this userProfile
	 * @param customSource customSource to add
	 */
	protected void addCustomSource(CustomSource customSource){
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - addCustomSource("+customSource.getElementId()+")");
    	}
		customSources.put(customSource.getElementId(),customSource);
	}
	
	/**	 * 
	 * Remove a customCategory from this userProfile
	 * @param sourceId
	 */
	protected void removeCustomSource(String sourceId){
	   	if (log.isDebugEnabled()){
    		log.debug("id="+userId+" - removeCustomSource("+sourceId+")");
    	}
		CustomSource cs = customSources.remove(sourceId);
		if (cs != null) {
			DomainTools.getDaoService().deleteCustomSource(cs);
		}
	}
		
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof UserProfile)) return false;
		final UserProfile userprofile = (UserProfile) o;
		if (!userprofile.getUserId().equals(this.getUserId())) return false;
		return true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getUserId().hashCode();
	}

	/* ************************** ACCESSORS ********************************* */



	/**
	 * @return Returns the userId.
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId The userId to set.
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return customContexts
	 */
	private Map<String, CustomContext> getCustomContexts() {
		return customContexts;
	}

	/**
	 * @param customContexts
	 */
	private void setCustomContexts(Map<String, CustomContext> customContexts) {
		this.customContexts = customContexts;
	}

	/**
	 * @return customManagedCategories
	 */
	private Map<String, CustomCategory> getCustomCategories() {
		return customCategories;
	}

	/**
	 * @param customCategories 
	 */
	private void setCustomCategories(
			Map<String, CustomCategory> customCategories) {
		this.customCategories = customCategories;
	}

	/**
	 * @return database primary Key
	 */
	public long getUserProfilePK() {
		return userProfilePK;
	}

	/**
	 * @param userProfilePK - database Primary Key
	 */
	public void setUserProfilePK(long userProfilePK) {
		this.userProfilePK = userProfilePK;
	}

	/**
	 * @return custom sources from this userProfile
	 */
	private Map<String, CustomSource> getCustomSources() {
		return customSources;
	}

	/**
	 * @param customSources
	 */
	private void setCustomSources(Map<String, CustomSource> customSources) {
		this.customSources = customSources;
	}

	
}
