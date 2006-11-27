package org.esupportail.lecture.domain.beans;

import org.esupportail.lecture.domain.model.Context;
import org.esupportail.lecture.domain.model.CustomContext;
import org.esupportail.lecture.exceptions.ContextNotFoundException;

/**
 * @author bourges
 * used to store context informations
 */
public class ContextBean {
	/**
	 * id of context
	 */
	private String id;
	/**
	 * name of context
	 */
	private String name;
	/**
	 * description of the context
	 */
	private String description;


	/**
	 * default contructor
	 */
	public ContextBean(){
		super();
	}
	
	/**
	 * @param customContext
	 * @throws ContextNotFoundException 
	 */
	public ContextBean(CustomContext customContext) throws ContextNotFoundException{
		Context context = customContext.getContext();

		setName(context.getName());
		setDescription(context.getDescription());
		setId(context.getId());
	}
	
	/**
	 * get the id of the context
	 * @return id of context
	 */
	public String getId() {
		return id;
	}
	/**
	 * set the id of the context
	 * @param id id of context
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * get the name of the context
	 * @return name of context
	 */
	public String getName() {
		return name;
	}
	/** 
	 * set the name of the context
	 * @param name name of the context
	 */
	public void setName(String name) {
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
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String string = "";
		string += " Id = " + id.toString() + "\n";
		string += " Name = " + name.toString() + "\n";
		string += " Desc = " + description.toString() + "\n";
		return string;
	}
}
