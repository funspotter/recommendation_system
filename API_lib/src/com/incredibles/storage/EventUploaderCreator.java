package com.incredibles.storage;


public abstract class EventUploaderCreator {

	public static EventUploader create() {
		return new CloudEventUploader();
	}
}
