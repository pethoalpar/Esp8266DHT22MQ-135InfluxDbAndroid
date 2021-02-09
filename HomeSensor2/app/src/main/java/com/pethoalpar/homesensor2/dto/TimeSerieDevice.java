package com.pethoalpar.homesensor2.dto;

import java.util.List;
import java.util.Map;

/**
 * @author alpar.petho
 *
 */
public class TimeSerieDevice {

	private String deviceId;
	private Map<String, List<TimePoint>> series;

	public String getDeviceId() {
		return deviceId;
	}

	public Map<String, List<TimePoint>> getSeries() {
		return series;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setSeries(Map<String, List<TimePoint>> series) {
		this.series = series;
	}
}
