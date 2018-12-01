package com.example.cesit.sensores;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * TODO (1): Obtener datos de humedad
 * TODO (2): Obtener datos de temperatura
 * TODO (3): Obtener datos de un tercer sensor (¿referencia acelerómetro?)
 *
 * NOTAS: desactivar todos los sensores que no se vayan a utilizar, especialmente en onPause, para no drenar rápidamente la batería
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String TITULO_ALERTA_DE_TEMPERATURA = "Alerta de temperatura";
    public static final String TITULO_ALERTA_DE_HUMEDAD = "Alerta de humedad";
    public static final String MENSAJE_TEMPERATURA = "La temperatura no es la adecuada";
    public static final String MENSAJE_HUMEDAD = "La humedad no es la adecuada";
    private SensorManager sensorManager;
    private Sensor humedad;
    private Sensor temperatura;
    private Sensor giroscopio;
    private Sensor iluminacion;
    private TextView txtTemperatura;
    private TextView txtGiroscopio;
    private TextView txtHumedad;
    private TextView txtIluminacion;
    private AlertDialog.Builder alertBuilder;
    private boolean mostrandoAlerta = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTemperatura = findViewById(R.id.txtTemperatura);
        txtGiroscopio = findViewById(R.id.txtGiroscopio);
        txtHumedad = findViewById(R.id.txtHumedad);
        txtIluminacion = findViewById(R.id.txtIluminacion);

        alertBuilder = new AlertDialog.Builder(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        humedad = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        temperatura = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        iluminacion = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
//        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

    }
    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, temperatura, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humedad, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, iluminacion, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
        sensorManager.registerListener(this, temperatura, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == giroscopio){
            txtGiroscopio.setText("Giroscopio: ");
            txtGiroscopio.append(event.values[0] + "");
        }

        if (event.sensor == temperatura) {
            txtTemperatura.setText("Temperatura: ");
            for (float valor : event.values) {
                txtTemperatura.append(valor + "\n");
            }
            // mostrarAlerta("Temperatura", "La temperatura es ");
        }

        if (event.sensor == humedad){
            txtHumedad.setText("Humedad: ");
            txtHumedad.append(event.values[0] + "");
        }

        if (event.sensor == iluminacion){
            txtIluminacion.setText("Iluminación: ");
            txtIluminacion.append(event.values[0] + "");
            if (event.values[0] < 10){
                mostrarAlerta("luz", "La iluminación ha bajado de 10");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void mostrarAlerta(String titulo, String mensaje){

        // Esta variable se ha introducido para que no se muestre más de un diágolo, si se elimina la comprobación aparecerán varios diálogos al producirse cambios en los sensores (que es cuando mostrarAlerta es llamada
        if (!mostrandoAlerta){
            alertBuilder.setTitle(titulo);
            alertBuilder.setTitle(mensaje);
            alertBuilder.setCancelable(true);

            alertBuilder.setNegativeButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mostrandoAlerta = false;
                }
            });

            alertBuilder.show();
            mostrandoAlerta = true;
        }

    }
}