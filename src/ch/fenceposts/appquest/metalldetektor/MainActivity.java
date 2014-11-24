package ch.fenceposts.appquest.metalldetektor;

import ch.fenceposts.appquest.metalldetektor.controller.ControllerSensor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

	private static final String DEBUG_TAG = "mydebug";
	private static final int SCAN_QR_CODE_REQUEST_CODE = 0;
	private static final int PROGRESSBAR_MAGNETIC_FIELD_MAX = 500;
	private ControllerSensor controllerSensor;
	private SensorManager sensorManager;
	private Sensor sensorMagneticField;
	private TextView textViewMagneticFieldValue;
	private ProgressBar progressBarMagneticFieldValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		controllerSensor = new ControllerSensor(getApplicationContext());
		sensorManager = controllerSensor.getSensorManager();

		Log.d(DEBUG_TAG, Integer.toString(controllerSensor.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size()));

		// check and try to add magnetic field sensor
		if ((sensorMagneticField = controllerSensor.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0)) == null) {
			try {
				throw new Exception("No magnetic field sensor found!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
		}

		// initialize layout components
		textViewMagneticFieldValue = (TextView) findViewById(R.id.textViewMagneticFieldValue);
		progressBarMagneticFieldValue = (ProgressBar) findViewById(R.id.progressBarMagneticFieldValue);
		progressBarMagneticFieldValue.setMax(PROGRESSBAR_MAGNETIC_FIELD_MAX);
	}

	@Override
	protected void onPause() {
		super.onPause();

		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (sensorMagneticField != null) {
			sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			float[] mag = event.values;
			int value = (int) Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]);

			textViewMagneticFieldValue.setText(Integer.toString(value));
			progressBarMagneticFieldValue.setProgress(value);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add("Log");
		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String logMsg = intent.getStringExtra("SCAN_RESULT");
				// Weiterverarbeitung..
				log(logMsg);
			}
		}
	}

	private void log(String qrCode) {
		Intent intent = new Intent("ch.appquest.intent.LOG");

		if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		}

		// intent.putExtra("ch.appquest.taskname", "Grössen Messer");
		intent.putExtra("ch.appquest.taskname", "Metall Detektor");
		// CharSequence calculatedObjectHeight = textView.getText();
		CharSequence calculatedMagneticFieldValue = textViewMagneticFieldValue.getText();
		// Achtung, je nach App wird etwas anderes eingetragen (siehe Tabelle
		// ganz unten):
		intent.putExtra("ch.appquest.logmessage", qrCode + ": " + calculatedMagneticFieldValue);

		startActivity(intent);
	}
}
