/**
* ESUP-Portail Lecture - Copyright (c) 2006 ESUP-Portail consortium
* For any information please refer to http://esup-helpdesk.sourceforge.net
* You may obtain a copy of the licence at http://www.esup-portail.org/license/
*/
package org.esupportail.lecture.domain.beans;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.esupportail.lecture.domain.model.Context;
import org.esupportail.lecture.domain.model.CustomContext;
import org.esupportail.lecture.domain.model.ItemDisplayMode;
import org.esupportail.lecture.domain.model.TreeDisplayMode;
import org.esupportail.lecture.exceptions.domain.ContextNotFoundException;

/**
 * used to store context informations.
 * @author bourges
 */
public class ContextBean {

	/* 
	 *************************** PROPERTIES ******************************** */	
	/**
	 * id of context.
	 */
	private String id;
	/**
	 * name of context.
	 */
	private String name;
	/**
	 * description of the context.
	 */
	private String description;
	
	private boolean modePublisher;
	/**
	 * size of tree window.
	 */
	private boolean userCanMarkRead;
	private int treeSize;

	/**
	* visibility of the tree panel.
	*/
	private TreeDisplayMode treeVisible = TreeDisplayMode.VISIBLE;
	private ItemDisplayMode itemDisplayMode;
	public ItemDisplayMode getItemDisplayMode() {
		return itemDisplayMode;
	}

	public void setItemDisplayMode(ItemDisplayMode itemDisplayMode) {
		this.itemDisplayMode = itemDisplayMode;
	}

	/**
	 * orderedSourceIDs store SourceID and ordering order in the CategoryProfile definition.
	 */
	private Map<String, Integer> orderedCategoryIDs = Collections.synchronizedMap(new HashMap<String, Integer>());	

	/*
	 *************************** INIT ************************************** */	

	/**
	 * Constructor initializing object.
	 * @param customContext
	 * @throws ContextNotFoundException 
	 */
	public ContextBean(final CustomContext customContext) throws ContextNotFoundException {
		Context context = customContext.getContext();
		setTreeSize(customContext.getTreeSize());
		setName(context.getName());
		setDescription(context.getDescription());
		setId(context.getId());
		setOrderedCategoryIDs(context.getOrderedCategoryIDs());
		setTreeVisible(context.getTreeVisible());
		setModePublisher(context.getModePublisher());
		setUserCanMarkRead(context.getUserCanMarkRead());
		setItemDisplayMode(customContext.getItemDisplayMode());
	}
	
	/*
	 *************************** ACCESSORS ********************************* */	
	
	public boolean isUserCanMarkRead() {
		return userCanMarkRead;
	}

	public void setUserCanMarkRead(boolean userCanMarkRead) {
		this.userCanMarkRead = userCanMarkRead;
	}

	/**
	 * get the id of the context.
	 * @return id of context
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * set the id of the context.
	 * @param id id of context
	 */
	public void setId(final String id) {
		this.id = id;
	}
	
	/**
	 * get the name of the context.
	 * @return name of context
	 */
	
	public String getName() {
		return name;
	}
	
	/** 
	 * set the name of the context.
	 * @param name name of the context
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return description of context
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description description of the context
	 */
	public void setDescription(final String description) {
		this.description = description;
	}
	
	/**
	 * @return tree size of context
	 */
	public int getTreeSize() {
		return treeSize;
	}

	/**
	 * @param treeSize tree size to set
	 */
	public void setTreeSize(final int treeSize) {
		this.treeSize = treeSize;
	}
	
	/**
	 * @param orderedCategoryIDs the orderedCategoryIDs to set
	 */
	public void setOrderedCategoryIDs(final Map<String, Integer> orderedCategoryIDs) {
		this.orderedCategoryIDs = orderedCategoryIDs;
	}

	/**
	 * @return the orderedCategoryIDs
	 */
	public Map<String, Integer> getOrderedCategoryIDs() {
		return orderedCategoryIDs;
	}
	
	/**
	 * @return the treeVisible
	 */
	public TreeDisplayMode getTreeVisible() {
		return treeVisible;
	}

	/**
	 * @param treeVisible the treeVisible to set
	 */
	public void setTreeVisible(final TreeDisplayMode treeVisible) {
		this.treeVisible = treeVisible;
	}

	public boolean isModePublisher() {
		return modePublisher;
	}

	public void setModePublisher(boolean modePublisher) {
		this.modePublisher = modePublisher;
	}

	/*
	 *************************** METHODS *********************************** */
	/**
	 * @param categoryID - the ID of source to find
	 * @return the XML order in of the source the CategoryProfile definition
	 */
	public int getXMLOrder(final String categoryID) {
		Integer ret = orderedCategoryIDs.get(categoryID);
		if (ret == null) {
			ret = Integer.MAX_VALUE;
		}
		return ret;
	}

	@Override
	public String toString() {
		return "ContextBean{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", modePublisher=" + modePublisher +
				", userCanMarkRead=" + userCanMarkRead +
				", treeSize=" + treeSize +
				", treeVisible=" + treeVisible +
				", itemDisplayMode=" + itemDisplayMode +
				'}';
	}
}
