package com.pethoalpar.homesensor2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.pethoalpar.homesensor2.dto.TimePoint;
import com.pethoalpar.homesensor2.dto.TimeSerieDevice;
import com.pethoalpar.homesensor2.services.InfluxDbService;
import com.pethoalpar.homesensor2.utils.CollectionUtil;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author alpar.petho
 */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

	public static final String TEMP = "temp";
	public static final String HUMIDITY = "humidity";
	public static final String AIR_QUALITY = "air_quality";
	private InfluxDbService influxDbService;

	private TextView tempTextView;
	private TextView humidityTextView;
	private TextView airQualityTextView;
	private LineChart chart;
	private CheckBox tempCheckBox;
	private CheckBox humidityCheckBox;
	private CheckBox airQualityCheckbox;
	private Spinner timeSpinner;

	private List<String> spinnerValues;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tempTextView = findViewById(R.id.textViewTemp);
		humidityTextView = findViewById(R.id.textViewHumidity);
		airQualityTextView = findViewById(R.id.textViewAirQuality);
		chart = findViewById(R.id.graph);
		tempCheckBox = findViewById(R.id.checkBoxTemp);
		humidityCheckBox = findViewById(R.id.checkBoxHumidity);
		airQualityCheckbox = findViewById(R.id.checkBoxAirQuality);
		timeSpinner = findViewById(R.id.spinnerTimeFrame);
		ArrayAdapter ad
				= new ArrayAdapter(
				this,
				R.layout.my_spinner_element,
				getSpinnerValues());
		timeSpinner.setAdapter(ad);
		setListeners();

		influxDbService.callLastMeasuredData();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		redrawChart();
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		redrawChart();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private List<String> getSpinnerValues() {
		List<String> retList = new ArrayList<>();
		retList.add(getString(R.string.day));
		retList.add(getString(R.string.two_days));
		retList.add(getString(R.string.week));
		retList.add(getString(R.string.month));
		return retList;
	}

	private void redrawChart() {
		Calendar cal = Calendar.getInstance();
		Long to = cal.getTimeInMillis();
		switch (timeSpinner.getSelectedItemPosition()) {
			default:
			case 0:
				cal.set(Calendar.HOUR_OF_DAY, -24);
				break;
			case 1:
				cal.set(Calendar.HOUR_OF_DAY, -48);
				break;
			case 2:
				cal.set(Calendar.DAY_OF_YEAR, 7);
				break;
			case 3:
				cal.set(Calendar.DAY_OF_YEAR, 30);
				break;
		}

		Long from = cal.getTimeInMillis();
		influxDbService.callMeasuredDataPeriod(from, to, getCheckedList());
	}

	private void setListeners() {
		tempCheckBox.setOnCheckedChangeListener(this::onCheckedChanged);
		humidityCheckBox.setOnCheckedChangeListener(this::onCheckedChanged);
		airQualityCheckbox.setOnCheckedChangeListener(this::onCheckedChanged);
		timeSpinner.setOnItemSelectedListener(this);
		chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
			@Override
			public void onValueSelected(Entry e, Highlight h) {
				Description description = new Description();
				SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
				Date date = new Date((long) e.getX() * 1000);
				description.setText("Time:" + sdf.format(date) + "  " + getString(R.string.value) + ":" + e.getY());
				description.setTextColor(Color.WHITE);
				chart.setDescription(description);
			}

			@Override
			public void onNothingSelected() {

			}
		});

		influxDbService = new InfluxDbService() {
			@Override
			public void onLastMeasurementResult(List<TimeSerieDevice> results) {
				super.onLastMeasurementResult(results);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setLastValues(results);
					}
				});
			}

			@Override
			public void onMeasurementResult(List<TimeSerieDevice> results) {
				plot(results);
			}
		};
	}

	private void setLastValues(List<TimeSerieDevice> results) {
		for (TimeSerieDevice tsd : CollectionUtil.emptyIfNull(results)) {
			Map<String, List<TimePoint>> series = tsd.getSeries();
			if (series != null) {
				for (Map.Entry<String, List<TimePoint>> serie : series.entrySet()) {
					if (TEMP.equals(serie.getKey())) {
						tempTextView.setText(getTranslatedName(serie.getKey()) + ": " + serie.getValue().get(0).getValue().toString() + " °C");
					}
					if (HUMIDITY.equals(serie.getKey())) {
						humidityTextView.setText(getTranslatedName(serie.getKey()) + ": " + serie.getValue().get(0).getValue().toString() + " %");
					}
					if (AIR_QUALITY.equals(serie.getKey())) {
						airQualityTextView.setText(getTranslatedName(serie.getKey()) + ": " + serie.getValue().get(0).getValue().toString() + " PPM");
					}
				}
			}
		}
	}

	private String getTranslatedName(String fieldName) {
		if (TEMP.equals(fieldName)) {
			return getString(R.string.temperature);
		} else if (HUMIDITY.equals(fieldName)) {
			return getString(R.string.humidity);
		} else {
			return getString(R.string.air_quality);
		}
	}

	private List<String> getCheckedList() {
		List<String> retList = CollectionUtil.emptyList();
		if (tempCheckBox.isChecked()) {
			retList.add(TEMP);
		}
		if (humidityCheckBox.isChecked()) {
			retList.add(HUMIDITY);
		}
		if (airQualityCheckbox.isChecked()) {
			retList.add(AIR_QUALITY);
		}
		return retList;
	}

	private void plot(List<TimeSerieDevice> results) {
		if (chart.getData() != null) {
			chart.clearValues();
		}
		List<LineDataSet> dataSets = CollectionUtil.emptyList();

		if (CollectionUtil.isNotEmpty(results)) {
			for (TimeSerieDevice tsd : results) {
				if (tsd.getSeries() != null && !tsd.getSeries().isEmpty()) {
					for (String field : tsd.getSeries().keySet()) {
						LineDataSet lds = buildLineDataSet(tsd, field);
						dataSets.add(lds);
					}
				}
			}
		}

		setXAxis();
		setYAxis();

		LineData lineData = new LineData(dataSets.toArray(new LineDataSet[dataSets.size()]));
		chart.setData(lineData);
		chart.setTouchEnabled(true);
		chart.setPinchZoom(true);
		chart.notifyDataSetChanged();
		chart.getLegend().setTextColor(Color.WHITE);
		chart.invalidate();
	}

	private void setYAxis() {
		YAxis yAxisRight = chart.getAxisRight();
		yAxisRight.setEnabled(false);

		YAxis yAxisLeft = chart.getAxisLeft();
		yAxisLeft.setGranularity(0.3f);
	}

	private void setXAxis() {
		XAxis xAxis = chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		ValueFormatter formatter = new ValueFormatter() {
			SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd HH:mm");

			@Override
			public String getFormattedValue(float value) {
				return sdf.format(new Date((long) (value)));
			}
		};
		xAxis.setValueFormatter(formatter);
		xAxis.setTextColor(Color.WHITE);
		xAxis.setLabelCount(4);
		chart.getAxisLeft().setTextColor(Color.WHITE);
	}

	@NotNull
	private LineDataSet buildLineDataSet(TimeSerieDevice tsd, String field) {
		List<TimePoint> series = tsd.getSeries().get(field);
		List<Entry> entries = new ArrayList<>();
		for (TimePoint tp : CollectionUtil.emptyIfNull(series)) {
			Date date = new Date(tp.getTimestamp());
			entries.add(new Entry(date.getTime(), new Double(tp.getValue().toString()).floatValue()));
		}
		LineDataSet lds = new LineDataSet(entries, field);
		lds.setDrawCircles(false);
		lds.setLineWidth(2f);
		if (TEMP.equals(field)) {
			lds.setColor(Color.RED);
		}
		if (HUMIDITY.equals(field)) {
			lds.setColor(Color.BLUE);
		}
		if (AIR_QUALITY.equals(field)) {
			lds.setColor(Color.GREEN);
		}
		return lds;
	}


}