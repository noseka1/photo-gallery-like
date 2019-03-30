package com.redhat.photogallery.like;

import com.redhat.photogallery.common.DataStoreItem;

public class LikesItem extends DataStoreItem {

	private int likes;

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	@Override
	public String toString() {
		return "LikesItem [likes=" + likes + ", id=" + id + "]";
	}

}
