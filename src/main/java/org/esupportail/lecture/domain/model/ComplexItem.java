package org.esupportail.lecture.domain.model;

public class ComplexItem extends Item {

	private VisibilitySets visibility; 
	public ComplexItem(Source source) {
		super(source);
	}
	public void setVisibility(VisibilitySets visibilitySets) {
		this.visibility = visibilitySets;
	}
	public VisibilitySets getVisibility() {
		return visibility;
	}
}
