/**
 * ESUP-Portail Lecture - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-lecture
 */
package org.esupportail.lecture.domain;

import java.util.List;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.lecture.domain.beans.CategoryBean;
import org.esupportail.lecture.domain.beans.ContextBean;
import org.esupportail.lecture.domain.beans.SourceBean;
import org.esupportail.lecture.domain.beans.UserBean;

/**
 * The facade service.
 * implement as an abstract class extended by FacadeServiceImpl
 */
abstract class FacadeService {
	
	/**
	 * external service used to access portlet or servlet information
	 */
	private ExternalService externalService;
	/**
	 * domain service used to access domain information
	 */
	private DomainService domainService;
	

	/**
	 * @return the current connected user
	 */
	public UserBean getConnectedUser() {
		return externalService.getConnectedUser();
	}
	
	/**
	 * @return the current context id (portlet preference with name "context")
	 */
	public String getCurrentContextId() {
		return externalService.getCurrentContextId();
	}
	
	/**
	 * @param contextId id of context
	 * @return ContextBean
	 */
	public ContextBean getContext(String contextId) {
		return domainService.getContext(contextId);
	}
	
	/**
	 * @param contextId id of context
	 * @param uid user ID
	 * @return List of CategoryBean obliged or subscribed by a user in a context
	 */
	public List<CategoryBean> getCategories(String contextId, String uid) {
		return domainService.getCategories(contextId, uid);
	}
	
	/**
	 * @param categoryId id of category
	 * @param uid user ID
	 * @return List of SourceBean obliged or subscribed by a user in a category
	 */
	public List<SourceBean> getSources(String categoryId, String uid) {
		return domainService.getSources(categoryId, uid);
	}
	
	/**
	 * @param uid user ID
	 * @param itemId item id
	 * @param sourceId source if
	 * marck a Item form a source for a user as read
	 */
	public void marckItemasRead(String uid, String itemId, String sourceId) {
		domainService.marckItemasRead(uid, itemId, sourceId);
	}

	/**
	 * @param uid user ID
	 * @param itemId item id
	 * @param sourceId source if
	 * marck a Item form a source for a user as unread
	 */
	public void marckItemasUnread(String uid, String itemId, String sourceId) {
		domainService.marckItemasUnread(uid, itemId, sourceId);
	}

	/**
	 * @param domainService
	 */
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	/**
	 * @param externalService
	 */
	public void setExternalService(ExternalService externalService) {
		this.externalService = externalService;
	}
}
