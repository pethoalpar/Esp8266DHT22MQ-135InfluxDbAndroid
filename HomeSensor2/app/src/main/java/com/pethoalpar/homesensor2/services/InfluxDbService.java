package com.pethoalpar.homesensor2.services;

import com.pethoalpar.homesensor2.dto.TimePoint;
import com.pethoalpar.homesensor2.dto.TimeSerieDevice;
import com.pethoalpar.homesensor2.utils.CollectionUtil;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author alpar.petho
 */
public class InfluxDbService {

	private InfluxDB influxDB;

	public InfluxDbService() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (influxDB == null) {
						influxDB = InfluxDBFactory.connect("http://your_ip:8086", "admin", "admin");
						influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
						influxDB.setDatabase("bedroom");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public void callLastMeasuredData() {

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int count = 0;
					while (influxDB == null && count < 10) {
						Thread.sleep(750);
					}
					if (influxDB != null) {
						String sql = "SELECT temp,humidity,air_quality FROM \"bedroom\".\"autogen\".\"bedroom\" ORDER BY time desc LIMIT 1";

						QueryResult queryResult = influxDB.query(new Query(sql), TimeUnit.MILLISECONDS);
						List<TimeSerieDevice> res = buildResultList(queryResult.getResults());

						onLastMeasurementResult(res);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public void callMeasuredDataPeriod(Long from, Long to, List<String> fields) {

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int count = 0;
					while (influxDB == null && count < 10) {
						Thread.sleep(750);
					}
					if (influxDB != null) {
						StringBuilder fieldsStr = new StringBuilder();
						for (int i = 0; i < fields.size(); ++i) {
							fieldsStr.append(fields.get(i));
							if (i < fields.size() - 1) {
								fieldsStr.append(",");
							}
						}
						String sql = "SELECT " + fieldsStr.toString() + " FROM \"bedroom\".\"autogen\".\"bedroom\" WHERE time > " + from * 1000 * 1000 + " AND time < " + to * 1000 * 1000 + " order by time";

						QueryResult queryResult = influxDB.query(new Query(sql), TimeUnit.MILLISECONDS);
						List<TimeSerieDevice> res = buildResultList(queryResult.getResults());

						onMeasurementResult(res);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public void onLastMeasurementResult(List<TimeSerieDevice> results) {

	}

	public void onMeasurementResult(List<TimeSerieDevice> results) {

	}


	private List<TimeSerieDevice> buildResultList(List<Result> results) {
		List<TimeSerieDevice> retList = new ArrayList<>();
		for (Result result : CollectionUtil.emptyIfNull(results)) {
			List<Series> series = result.getSeries();
			TimeSerieDevice tds = new TimeSerieDevice();
			if (CollectionUtil.isNotEmpty(series)) {
				buildSeriesList(retList, series, tds);
			}
		}
		return retList;
	}

	private void buildSeriesList(List<TimeSerieDevice> retList, List<Series> series, TimeSerieDevice tds) {
		for (Series serie : series) {
			tds.setDeviceId(serie.getName());
			Map<String, List<TimePoint>> pointMap = new HashMap<>();
			Map<Integer, String> orderMap = new HashMap<>();
			for (int i = 0; i < serie.getColumns().size(); ++i) {
				String name = serie.getColumns().get(i);
				if (!"time".equals(name)) {
					pointMap.put(name, new ArrayList<>());
					orderMap.put(i, name);
				}
			}
			tds.setSeries(pointMap);
			buildValueList(serie, pointMap, orderMap);
		}
		retList.add(tds);
	}

	private void buildValueList(Series serie, Map<String, List<TimePoint>> pointMap, Map<Integer, String> orderMap) {
		for (List<Object> objects : CollectionUtil.emptyIfNull(serie.getValues())) {
			if (CollectionUtil.isNotEmpty(objects)) {
				Long timestamp = ((Double) objects.get(0)).longValue();
				if (timestamp > 0) {
					for (int i = 1; i < objects.size(); ++i) {
						Object object = objects.get(i);
						if (orderMap.containsKey(i) && object != null) {
							pointMap.get(orderMap.get(i)).add(new TimePoint(timestamp, object));
						}
					}
				}
			}
		}
	}

}
