package org.esupportail.lecture.domain.model;

import org.esupportail.lecture.exceptions.domain.ManagedCategoryNotLoadedException;

public class ManagedCategoryProfileActus extends ManagedCategoryProfile {
	private String actualitesURL;
	private String fileId ; 
	public ManagedCategoryProfileActus(ManagedCategoryProfile mcp) {
		this.setId(mcp.getId());
		this.setFileId(mcp.getFileId());
		this.setName(mcp.getName());
		this.setTrustCategory(mcp.getTrustCategory());
		this.setUserCanMarkRead(mcp.isUserCanMarkRead());
		this.setSpecificUserContent(mcp.isSpecificUserContent());
		try {
			this.setTtl(mcp.getTtl());
		} catch (ManagedCategoryNotLoadedException e) {
			LOG.error("error  : "+e);
		}
		this.setTimeOut(mcp.getTimeOut());
		this.setName(mcp.getName());
		this.setAccess(mcp.getAccess());

	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileID(){
		return this.fileId;
	}
	public void setActualitesURL(String valueOf) {
		this.actualitesURL =valueOf ;
	}

	public String getActualitesURL(){
		return this.actualitesURL;
	}

	public String getItemXPath() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getMobileXsltUrl() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getXsltURL() {
		// TODO Auto-generated method stub
		return "";
	}
}
