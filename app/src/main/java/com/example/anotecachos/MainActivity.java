package com.example.anotecachos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNombreJugador;
    private Button botonAgregarJugador, botonIniciarJuego, botonReiniciarJuego;
    private GridView gridViewJugadores;
    private ArrayList<String> listaJugadores;
    private JugadorAdapter adaptadorJugadores;
    private CBaseDatos baseDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#757575"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        editTextNombreJugador = findViewById(R.id.editTextNombreJugador);
        botonAgregarJugador = findViewById(R.id.botonAgregarJugador);
        botonIniciarJuego = findViewById(R.id.botonIniciarJuego);
        botonReiniciarJuego = findViewById(R.id.botonReiniciarJuego);
        gridViewJugadores = findViewById(R.id.gridViewJugadores);

        baseDatos = new CBaseDatos(this);
        listaJugadores = baseDatos.obtenerJugadores();

        adaptadorJugadores = new JugadorAdapter(this, listaJugadores);
        gridViewJugadores.setAdapter(adaptadorJugadores);

        botonAgregarJugador.setOnClickListener(v -> {
            String nombreJugador = editTextNombreJugador.getText().toString().trim();
            if (!nombreJugador.isEmpty()) {
                if (baseDatos.agregarJugador(nombreJugador)) {
                    listaJugadores.add(nombreJugador);
                    adaptadorJugadores.notifyDataSetChanged();
                    editTextNombreJugador.setText("");
                    Toast.makeText(MainActivity.this, "Jugador agregado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error al agregar jugador", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Ingresa un nombre", Toast.LENGTH_SHORT).show();
            }
        });

        botonIniciarJuego.setOnClickListener(v -> {
            if (listaJugadores.size() < 2) {
                Toast.makeText(MainActivity.this, "Debes registrar al menos 2 jugadores", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, JuegoActivity.class);
                intent.putStringArrayListExtra("jugadores", new ArrayList<>(listaJugadores));
                startActivity(intent);
            }
        });

        botonReiniciarJuego.setOnClickListener(v -> mostrarDialogoConfirmacion());
    }

    private void mostrarDialogoConfirmacion() {
        if (listaJugadores.isEmpty()) {
            Toast.makeText(this, "No hay ningún jugador registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_personalizado, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(false);

        TextView tituloDialogo = dialogView.findViewById(R.id.tituloDialogo);
        TextView subtituloDialogo = dialogView.findViewById(R.id.subtituloDialogo);

        tituloDialogo.setText("Confirmación");
        subtituloDialogo.setText("¿Estás seguro de que quieres reiniciar el juego?");

        Button botonCancelar = dialogView.findViewById(R.id.botonCancelar);
        Button botonConfirmar = dialogView.findViewById(R.id.botonConfirmar);

        AlertDialog alertDialog = builder.create();

        botonCancelar.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        botonConfirmar.setOnClickListener(v -> {
            reiniciarJuego();
            alertDialog.dismiss();
        });

        alertDialog.show();
    }
    private void reiniciarJuego() {
        if (listaJugadores.isEmpty()) {
            Toast.makeText(this, "No hay jugadores para reiniciar el juego", Toast.LENGTH_SHORT).show();
            return;
        }
        baseDatos.borrarTodasLasPuntuaciones();
        Intent intent = new Intent(MainActivity.this, JuegoActivity.class);
        intent.putStringArrayListExtra("jugadores", new ArrayList<>(listaJugadores));
        startActivity(intent);
    }

}
