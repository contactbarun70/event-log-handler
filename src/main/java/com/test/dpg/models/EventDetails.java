package com.test.dpg.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "event_details")
public class EventDetails {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "duration")
	private Integer duration;

	@Column(name = "type")
	private String type;

	@Column(name = "host")
	private String host;

	@Column(name = "alert")
	private boolean alert;

	public String getId() {
		return id;
	}

	public int getDuration() {
		return duration;
	}

	public String getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setDuration(final int duration) {
		this.duration = duration;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setAlert(final boolean alert) {
		this.alert = alert;
	}

}
