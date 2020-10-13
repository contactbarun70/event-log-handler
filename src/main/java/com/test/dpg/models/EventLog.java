package com.test.dpg.models;

import java.util.UUID;

public class EventLog {

	private String uuid = UUID.randomUUID().toString();

	private String id;

	private String state;

	private String type;

	private String host;

	private long timestamp;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public String getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setState(final String state) {
		this.state = state;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "EventLog [uuid=" + uuid + ", id=" + id + ", state=" + state + ", type=" + type + ", host=" + host
				+ ", timestamp=" + timestamp + "]";
	}

}
