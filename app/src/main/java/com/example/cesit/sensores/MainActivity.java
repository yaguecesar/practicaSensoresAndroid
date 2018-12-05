package com.example.cesit.sensores;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Formatter;

/**
 * TODO (1): Obtener datos de humedad
 * TODO (2): Obtener datos de temperatura
 * TODO (3): Obtener datos de un tercer sensor (¿referencia acelerómetro?)
 * TODO (4): Añadir botones para activar y desactivar los distintos sensores utilizados
 *
 * NOTAS: desactivar todos los sensores que no se vayan a utilizar, especialmente en onPause, para no drenar rápidamente la batería
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{


    // Valores límite para los distintos sensores
    private static final float MAX_TEMP = 23;
    private static final float MIN_TEMP = 20;
    private static final float MIN_HUMEDAD = 40;
    private static final float MAX_HUMEDAD = 60;
    private static final int MIN_ILUMINACION = 10;
    private static double LIMITE_ACELERACION = 30;

    // SensorManager y sensores necesarios
    private SensorManager sensorManager;
    private Sensor humedad;
    private Sensor temperatura;
    private Sensor acelerometro;
    private Sensor iluminacion;

    // TextViews para mostrar algunos valores
    private TextView txtAcelerometro;
    private TextView txtIluminacion;

    // AlertBuilder para los diálogos de alerta
    private AlertDialog.Builder alertBuilder;

    // Garantiza, mediante comprobaciones, que sólo se muestre una alerta
    private boolean mostrandoAlerta;

    // Botones para activar o desactivar el seguimiento de los sensores
    private Button btnIluminacion;
    private Button btnTemperatura;
    private Button btnHumedad;
    private Button btnAcelerometro;

    // Estado del seguimiento de los sensores
    private boolean iluminacionOn;
    private boolean acelerometroOn;
    private boolean temperaturaOn;
    private boolean humedadOn;

    // Variables para las pruebas con el acelerómetro
    private Button btnLimite;
    private EditText editLimite;
    double maxVelocidad = 0;
    private boolean modoPrueba = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mostrandoAlerta = false;
        iluminacionOn = true;
        humedadOn = true;
        acelerometroOn = true;
        temperaturaOn = true;

        editLimite = findViewById(R.id.inputLimite);
        btnLimite = findViewById(R.id.btnLimite);
        btnLimite.setOnClickListener(this);

        txtAcelerometro = findViewById(R.id.txtAcelerometro);
        txtIluminacion = findViewById(R.id.txtIluminacion);

        btnAcelerometro = findViewById(R.id.btnAcelerometro);
        btnHumedad = findViewById(R.id.btnHumedad);
        btnTemperatura = findViewById(R.id.btnTemperatura);
        btnIluminacion = findViewById(R.id.btnIluminacion);

        btnAcelerometro.setOnClickListener(this);
        btnIluminacion.setOnClickListener(this);
        btnHumedad.setOnClickListener(this);
        btnTemperatura.setOnClickListener(this);

        alertBuilder = new AlertDialog.Builder(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        humedad = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        temperatura = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        iluminacion = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
//        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

    }
    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, temperatura, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humedad, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, iluminacion, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



        if (event.sensor == acelerometro){

            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate
            double x = 0, y = 0, z = 0;

            long tiempo = 1;


            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            // Módulo del vector aceleración
            double speed = Math.sqrt(x*x+y*y+z*z);

            if (speed > maxVelocidad){
                maxVelocidad = speed;
            }

            if (speed > LIMITE_ACELERACION) {
                mostrarAlerta("", getString(R.string.caida_msg));
            }

            Formatter f = new Formatter();
            f.format("maxVel:%.4f\nLimite: %.4f", maxVelocidad, LIMITE_ACELERACION);

            String textoGiroscopio = f.toString();
            txtAcelerometro.setText(textoGiroscopio);
        }

        if (event.sensor == temperatura) {

            if (event.values[0] > MAX_TEMP){
                mostrarAlerta("", getString(R.string.temp_alta_msg));
            }else{
                if (event.values[0] < MIN_TEMP){
                    mostrarAlerta("", getString(R.string.temp_baja_msg));
                }
            }
        }

        if (event.sensor == humedad){
            if (event.values[0] > MAX_HUMEDAD){
                mostrarAlerta("", getString(R.string.humedad_alta_msg));
            }else{
                if (event.values[0] < MIN_HUMEDAD){
                    mostrarAlerta("", getString(R.string.humedad_baja_msg));
                }
            }
        }

        if (event.sensor == iluminacion){
            txtIluminacion.setText("Iluminación: ");
            txtIluminacion.append(event.values[0] + "");
            if (event.values[0] < MIN_ILUMINACION){ // AlertaLuz se ha introducido para que no se muestre más de un diágolo, si se elimina la comprobación aparecerán varios diálogos al producirse cambios en los sensores (que es cuando mostrarAlerta es llamada
                mostrarAlerta("luz", getString(R.string.iluminacion_baja_msg));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void mostrarAlerta(String titulo, String mensaje){
        if (!mostrandoAlerta) {
            alertBuilder.setNegativeButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mostrandoAlerta = false;
                }
            });
            alertBuilder.setTitle(titulo);
            alertBuilder.setTitle(mensaje);
            alertBuilder.setCancelable(true);

            mostrandoAlerta = true;
            alertBuilder.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnHumedad:
                if (humedadOn) {
                    sensorManager.unregisterListener(this, humedad);
                    Toast.makeText(this, R.string.toast_humedadOFF, Toast.LENGTH_SHORT).show();
                }else{
                    sensorManager.registerListener(this, humedad, SensorManager.SENSOR_DELAY_NORMAL);
                    Toast.makeText(this, R.string.toast_humedadON, Toast.LENGTH_SHORT).show();;
                }
                humedadOn = !humedadOn;
                break;
            case R.id.btnTemperatura:
                if (temperaturaOn) {
                    sensorManager.unregisterListener(this, temperatura);
                    Toast.makeText(this, R.string.toast_temperaturaOFF, Toast.LENGTH_SHORT).show();;
                }else{
                    sensorManager.registerListener(this, temperatura, SensorManager.SENSOR_DELAY_NORMAL);
                    Toast.makeText(this, R.string.toast_temperaturaON, Toast.LENGTH_SHORT).show();;
                }
                temperaturaOn = !temperaturaOn;
                break;
            case R.id.btnAcelerometro:
                if (acelerometroOn) {
                    sensorManager.unregisterListener(this, acelerometro);
                    Toast.makeText(this, R.string.toast_aceleracionOFF, Toast.LENGTH_SHORT).show();;
                }else{
                    sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
                    Toast.makeText(this, R.string.toast_aceleracionON, Toast.LENGTH_SHORT).show();;
                }
                acelerometroOn = !acelerometroOn;
                break;
            case R.id.btnIluminacion:
                if (iluminacionOn) {
                    sensorManager.unregisterListener(this, iluminacion);
                    Toast.makeText(this, R.string.toas_ilumuncacionOFF, Toast.LENGTH_SHORT).show();;
                }else{
                    sensorManager.registerListener(this, iluminacion, SensorManager.SENSOR_DELAY_NORMAL);
                    Toast.makeText(this, R.string.toas_ilumuncacionON, Toast.LENGTH_SHORT).show();;
                }
                iluminacionOn = !iluminacionOn;
                break;
            case R.id.btnLimite:
                String s = editLimite.getText().toString();
                LIMITE_ACELERACION = Double.parseDouble(s);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.modoPrueba){
            cambiarModoPrueba();
        }
        return super.onOptionsItemSelected(item);
    }

    private void cambiarModoPrueba(){
        modoPrueba = !modoPrueba;

        if (!modoPrueba){
            btnLimite.setVisibility(View.INVISIBLE);
            editLimite.setVisibility(View.INVISIBLE);
        }else{
            btnLimite.setVisibility(View.VISIBLE);
            editLimite.setVisibility(View.VISIBLE);
        }
    }
}