package com.pethoalpar.homesensor2.dto;

/**
 * @author alpar.petho
 *
 */
public class TimePoint {

	public TimePoint(Long timestamp, Object value) {
		this.timestamp = timestamp;
		this.value = value;
	}

	public TimePoint() {
	}

	private Long timestamp;
	private Object value;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
