package com.example.anotecachos;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JuegoActivity extends AppCompatActivity {

    private TextView textViewJugadorTurno;
    private TextView textViewPuntuacion;
    private Button botonCambiarTurno, botonVerPuntuaciones, btnAbrirDados;
    private TableroView tableroView;
    private ArrayList<String> listaJugadores;
    private int turnoActual = 0;
    private boolean turnoCambiado = false;
    private Map<String, Integer> casillasCompletadasPorJugador = new HashMap<>();
    private static final int TOTAL_CASILLAS = 11;
    private PopupMenu popupMenu;
    public interface JuegoListener {
        void onCasillaCompletada();
    }
    private JuegoListener juegoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#757575"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        textViewJugadorTurno = findViewById(R.id.textViewJugadorTurno);
        textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        botonCambiarTurno = findViewById(R.id.botonCambiarTurno);
        botonVerPuntuaciones = findViewById(R.id.botonVerPuntuaciones);
        tableroView = findViewById(R.id.tableroView);
        tableroView.setTextViewPuntuacion(textViewPuntuacion);
        listaJugadores = getIntent().getStringArrayListExtra("jugadores");

        for (String jugador : listaJugadores) {
            casillasCompletadasPorJugador.put(jugador, 0);
        }

        tableroView.setListaJugadores(listaJugadores);

        if (listaJugadores == null || listaJugadores.isEmpty()) {
            Toast.makeText(this, "No se recibieron jugadores", Toast.LENGTH_LONG).show();
            return;
        }


        actualizarTurno();
        botonCambiarTurno.setOnClickListener(v -> cambiarTurno());
        botonVerPuntuaciones.setOnClickListener(v -> mostrarPuntuacionesActuales());
        this.juegoListener = new JuegoListener() {
            @Override
            public void onCasillaCompletada() {
                verificarFinDelJuegoConRetardo();
            }
        };
        // Configurar el botón
        btnAbrirDados = findViewById(R.id.btnAbrirDados);
        btnAbrirDados.setOnClickListener(v -> {
            new DadosVirtuales().mostrarDialogoDados(JuegoActivity.this);
        });
    }
    private void mostrarMenuOpciones(View view) {
        popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_opciones, popupMenu.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.item_tuti) {
                mostrarBotonReiniciar();
                return true;
            } else if (id == R.id.item_eliminar_jugador) {
                eliminarJugador();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    private void verificarFinDelJuegoConRetardo() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            verificarFinDelJuego();
        }, 300);
    }
    private void cambiarTurno() {
        turnoActual = (turnoActual + 1) % listaJugadores.size();
        turnoCambiado = false;
        actualizarTurno();
    }
    private void actualizarTurno() {
        if (listaJugadores != null && !listaJugadores.isEmpty()) {
            String jugadorEnTurno = listaJugadores.get(turnoActual);
            textViewJugadorTurno.setText(jugadorEnTurno);
            textViewPuntuacion.setText(String.valueOf(calcularPuntuacion(jugadorEnTurno)));
            tableroView.setJugadorActual(jugadorEnTurno);
            tableroView.setTurnoCambiado(turnoCambiado);
            tableroView.invalidate();
        } else {
            textViewJugadorTurno.setText("Error: No hay jugadores.");
        }
    }
    public void actualizarContadorCasillas(String jugador, String nombreCelda, String valor) {
        if (!valor.equals("0")) {
            int completadas = casillasCompletadasPorJugador.get(jugador);
            CBaseDatos baseDatos = new CBaseDatos(this);
            ArrayList<String> puntuaciones = baseDatos.obtenerPuntuaciones(jugador);
            boolean yaEstabaCompletada = false;
            for (String puntuacion : puntuaciones) {
                String[] partes = puntuacion.split(": ");
                if (partes.length == 2 && partes[0].equals(nombreCelda) &&
                        !partes[1].equals("0")) { // Solo excluir "0"
                    yaEstabaCompletada = true;
                    break;
                }
            }
            if (!yaEstabaCompletada) {
                casillasCompletadasPorJugador.put(jugador, completadas + 1);
                Log.d("CONTADOR", jugador + " ahora tiene " + (completadas + 1) + " casillas completadas");
                if (completadas + 1 >= TOTAL_CASILLAS) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        verificarFinDelJuego();
                    }, 500);
                }
            }
        }
    }

    public void verificarFinDelJuego() {
        runOnUiThread(() -> {
            CBaseDatos baseDatos = new CBaseDatos(this);
            String sql = "SELECT COUNT(*) FROM (" +
                    "SELECT " + CBaseDatos.COLUMNA_NOMBRE + " " +
                    "FROM " + CBaseDatos.TABLA_JUGADORES + " " +
                    "WHERE " + CBaseDatos.COLUMNA_NOMBRE + " NOT IN (" +
                    "SELECT " + CBaseDatos.COLUMNA_NOMBRE_JUGADOR + " " +
                    "FROM " + CBaseDatos.TABLA_PUNTUACIONES + " " +
                    "WHERE " + CBaseDatos.COLUMNA_CELDA + " IN ('Bala','Duke','Trenes','Cuadra','Quinas','Senas','Escalera','Full','Poker','Grande 1','Grande 2') " +
                    "AND " + CBaseDatos.COLUMNA_VALOR + " != '0' " +
                    "GROUP BY " + CBaseDatos.COLUMNA_NOMBRE_JUGADOR + " " +
                    "HAVING COUNT(DISTINCT " + CBaseDatos.COLUMNA_CELDA + ") = 11" +
                    ")" +
                    ")";

            SQLiteDatabase db = baseDatos.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int jugadoresIncompletos = cursor.getInt(0);
                    Log.d("FIN_JUEGO", "Jugadores incompletos: " + jugadoresIncompletos);
                    if (jugadoresIncompletos == 0) {
                        Log.d("FIN_JUEGO", "Todos los jugadores han completado sus casillas");
                        mostrarResultadosFinales(true);
                    }
                }
            } catch (Exception e) {
                Log.e("FIN_JUEGO", "Error al verificar fin del juego", e);
                verificarFinDelJuegoAlternativo();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        });
    }
    private void verificarFinDelJuegoAlternativo() {
        CBaseDatos baseDatos = new CBaseDatos(this);
        boolean todosCompletaron = true;
        for (String jugador : listaJugadores) {
            if (!baseDatos.jugadorCompletoTodasCasillas(jugador)) {
                todosCompletaron = false;
                break;
            }
        }
        if (todosCompletaron) {
            mostrarResultadosFinales(true);
        }
    }
    public int calcularPuntuacion(String jugador) {
        CBaseDatos baseDatos = new CBaseDatos(this);
        ArrayList<String> puntuaciones = baseDatos.obtenerPuntuaciones(jugador);

        int puntuacionTotal = 0;

        for (String puntuacion : puntuaciones) {
            String[] partes = puntuacion.split(": ");
            if (partes.length == 2) {
                String valor = partes[1];
                try {
                    if (!valor.equals("0")) {
                        puntuacionTotal += valor.equals("00") ? 0 : Integer.parseInt(valor);
                    }
                } catch (NumberFormatException e) {
                    Log.e("PUNTUACION", "Valor no numérico: " + valor);
                }
            }
        }
        return puntuacionTotal;
    }
    private void mostrarResultadosFinales(boolean esFinDelJuego) {
        ArrayList<JugadorPuntuacion> resultados = obtenerPuntuacionesOrdenadas();
        if (esFinDelJuego) {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_final, null);
            GridView gridResultados = dialogView.findViewById(R.id.gridResultados);
            TextView tvGanador = dialogView.findViewById(R.id.tvGanador);
            ResultadoAdapter adapter = new ResultadoAdapter(this, resultados);
            gridResultados.setAdapter(adapter);
            if (!resultados.isEmpty()) {
                int maxPuntuacion = resultados.get(0).getPuntuacion();
                ArrayList<String> ganadores = new ArrayList<>();

                for (JugadorPuntuacion jp : resultados) {
                    if (jp.getPuntuacion() == maxPuntuacion) {
                        ganadores.add(jp.getNombre());
                    }
                }
                String mensajeGanador;
                if (ganadores.size() > 1) {
                    mensajeGanador = "¡Empate entre " + TextUtils.join(", ", ganadores) +
                            " con " + maxPuntuacion + " puntos!";
                } else {
                    mensajeGanador = "¡El ganador es " + ganadores.get(0) +
                            " con " + maxPuntuacion + " puntos!";
                }
                tvGanador.setText(mensajeGanador);
            }
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            }
            dialog.show();
        } else {
            mostrarPuntuacionesActuales();
        }
    }
    private void mostrarPuntuacionesActuales() {
        ArrayList<JugadorPuntuacion> resultados = obtenerPuntuacionesOrdenadas();
        View dialogView = LayoutInflater.from(this).inflate(R.layout.alert_puntuaciones, null);
        GridView gridPuntuaciones = dialogView.findViewById(R.id.gridPuntuaciones);
        ResultadoAdapter adapter = new ResultadoAdapter(this, resultados);
        gridPuntuaciones.setAdapter(adapter);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        }

        dialog.show();
    }
    private ArrayList<JugadorPuntuacion> obtenerPuntuacionesOrdenadas() {
        ArrayList<JugadorPuntuacion> resultados = new ArrayList<>();
        CBaseDatos baseDatos = new CBaseDatos(this);
        for (String nombre : listaJugadores) {
            int puntuacion = calcularPuntuacion(nombre);
            resultados.add(new JugadorPuntuacion(nombre, puntuacion));
        }

        resultados.sort((j1, j2) -> j2.getPuntuacion() - j1.getPuntuacion());
        return resultados;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_opciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.item_tuti) {
            mostrarBotonReiniciar();
            return true;
        } else if (id == R.id.item_eliminar_jugador) {
            eliminarJugador();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void eliminarJugador() {
        if (listaJugadores != null && listaJugadores.size() == 2) {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_personalizado, null);
            TextView titulo = dialogView.findViewById(R.id.tituloDialogo);
            TextView subtitulo = dialogView.findViewById(R.id.subtituloDialogo);
            Button botonCancelar = dialogView.findViewById(R.id.botonCancelar);
            Button botonConfirmar = dialogView.findViewById(R.id.botonConfirmar);

            titulo.setText("Eliminar Jugador");
            subtitulo.setText("No se pueden eliminar más jugadores, ya solo quedan 2.");
            botonConfirmar.setVisibility(View.GONE);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            botonCancelar.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
            return;
        }

        if (listaJugadores != null && !listaJugadores.isEmpty()) {
            String jugadorEnTurno = listaJugadores.get(turnoActual);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_personalizado, null);
            TextView titulo = dialogView.findViewById(R.id.tituloDialogo);
            TextView subtitulo = dialogView.findViewById(R.id.subtituloDialogo);
            Button botonCancelar = dialogView.findViewById(R.id.botonCancelar);
            Button botonConfirmar = dialogView.findViewById(R.id.botonConfirmar);

            titulo.setText("Eliminar Jugador");
            subtitulo.setText("¿Seguro que deseas eliminar a " + jugadorEnTurno + "?");

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            botonCancelar.setOnClickListener(v -> dialog.dismiss());

            botonConfirmar.setOnClickListener(v -> {
                CBaseDatos baseDatos = new CBaseDatos(this);
                baseDatos.eliminarJugador(jugadorEnTurno);
                listaJugadores.remove(jugadorEnTurno);

                if (!listaJugadores.isEmpty()) {
                    if (turnoActual >= listaJugadores.size()) {
                        turnoActual = 0;
                    }
                    actualizarTurno();
                } else {
                    textViewJugadorTurno.setText("No quedan jugadores.");
                }
                dialog.dismiss();
            });

            dialog.show();
        }
    }
    private void mostrarBotonReiniciar() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tuti, null);
        TextView tituloDialogo = dialogView.findViewById(R.id.tituloDialogo);
        TextView subtituloDialogo = dialogView.findViewById(R.id.subtituloDialogo);
        Button botonCancelar = dialogView.findViewById(R.id.botonCancelar);
        Button botonConfirmar = dialogView.findViewById(R.id.botonConfirmar);
        tituloDialogo.setText("¡La TUTI ha salido!");
        subtituloDialogo.setText("¿Quieres jugar de nuevo?");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        botonCancelar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        botonConfirmar.setOnClickListener(v -> {
            reiniciarJuegoSinPuntuacion();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            dialog.dismiss();
        });
        dialog.show();
    }
    private void reiniciarJuego() {
        for (String jugador : listaJugadores) {
            casillasCompletadasPorJugador.put(jugador, 0);
        }
        CBaseDatos baseDatos = new CBaseDatos(this);
        for (String jugador : listaJugadores) {
            baseDatos.borrarPuntuaciones(jugador);
        }
        Intent intent = new Intent(JuegoActivity.this, JuegoActivity.class);
        intent.putStringArrayListExtra("jugadores", new ArrayList<>(listaJugadores));
        startActivity(intent);
        finish();
    }
    private void reiniciarJuegoSinPuntuacion() {
        for (String jugador : listaJugadores) {
            casillasCompletadasPorJugador.put(jugador, 0);
        }
        CBaseDatos baseDatos = new CBaseDatos(this);
        baseDatos.borrarTodasLasPuntuaciones();
        recreate();
    }
}
