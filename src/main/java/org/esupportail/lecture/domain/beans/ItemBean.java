/**
* ESUP-Portail Lecture - Copyright (c) 2006 ESUP-Portail consortium
* For any information please refer to http://esup-helpdesk.sourceforge.net
* You may obtain a copy of the licence at http://www.esup-portail.org/license/
*/
package org.esupportail.lecture.domain.beans;

import org.esupportail.lecture.domain.model.CustomSource;
import org.esupportail.lecture.domain.model.Item;

/**
 * used to store item informations.
 * @author bourges
 */
public class ItemBean {
	
	/* 
	 *************************** PROPERTIES ******************************** */	
	
	/**
	 * id of item.
	 */
	private String id;
	/**
	 * html content of item.
	 */
	private String htmlContent;
	/**
	 * store if item is read or not.
	 */
	private boolean read;
	
	/*
	 *************************** INIT ************************************** */	
	/**
	 * Default constructor.
	 */
	public ItemBean() {
		super();
	}

	/**
	 * Constructor initializing object.
	 * @param it
	 * @param customSource
	 */
	public ItemBean(final Item it, final CustomSource customSource) {
		id = it.getId();
		htmlContent = it.getHtmlContent();
		read = customSource.isItemRead(id);
	}
	
	/*
	 *************************** ACCESSORS ********************************* */	
	
	/**
	 * @return html content of item
	 */
	public String getHtmlContent() {
		return htmlContent;
	}
	/**
	 * @param htmlContent
	 */
	public void setHtmlContent(final String htmlContent) {
		this.htmlContent = htmlContent;
	}
	/**
	 * @return id of item
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(final String id) {
		this.id = id;
	}
	/**
	 * @return if item is read or not
	 */
	public boolean isRead() {
		return read;
	}
	/**
	 * @param read
	 */
	public void setRead(final boolean read) {
		this.read = read;
	}

	
	// TODO (RB/VR<-GB) C'est pas très objet ça ;-)
	/**
	 * @return false because it is not a dummy Item
	 */
	public boolean isDummy() {
		return false;
	}
	/*
	 *************************** METHODS *********************************** */	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String string = "";
		string += "     Id = " + id + "\n";
		string += "     Html = " + htmlContent + "\n";
		string += "     read = " + read + "\n";
		
		return string;
	}

}