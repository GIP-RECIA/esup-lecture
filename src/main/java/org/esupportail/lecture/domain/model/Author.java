package org.esupportail.lecture.domain.model;

import java.io.Serializable;

public class Author implements Serializable {

	private String uid;
	private String name;
	private boolean isUserArticleAuthor;
	public Author(String uid, String name) {
		this.uid = uid;
		this.name = name;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isUserArticleAuthor() {
		return isUserArticleAuthor;
	}
	public void setUserArticleAuthor(boolean isUserArticleAuthor) {
		this.isUserArticleAuthor = isUserArticleAuthor;
	}

	@Override
	public String toString() {
		return "Author{" +
				"uid='" + uid + '\'' +
				", name='" + name + '\'' +
				", isUserArticleAuthor=" + isUserArticleAuthor +
				'}';
	}
}
