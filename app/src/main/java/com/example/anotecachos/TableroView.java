package com.example.anotecachos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TableroView extends View {
    private Paint paintLineas, paintDivisor, paintTexto;
    private final int numColumnas = 3;
    private int numFilas = 4;
    private int cellSize;
    private int startY;
    private String jugadorActual;
    private String[][] nombresCeldas = {
            {"Bala", "Escalera", "Cuadra"},
            {"Duke", "Full", "Quinas"},
            {"Trenes", "Poker", "Senas"},
            {"Grande 1", "", "Grande 2"}
    };
    private ArrayList<String> listaJugadores;
    private Paint paintTextoNegrita;
    private boolean turnoCambiado = false;
    private TextView textViewPuntuacion; // Nuevo: Referencia a textViewPuntuacion

    public TableroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inicializarPinturas();
    }

    public void setListaJugadores(ArrayList<String> listaJugadores) {
        this.listaJugadores = listaJugadores;
    }

    public void setTurnoCambiado(boolean turnoCambiado) {
        this.turnoCambiado = turnoCambiado;
    }

    // Nuevo: Método para pasar la referencia de textViewPuntuacion
    public void setTextViewPuntuacion(TextView textViewPuntuacion) {
        this.textViewPuntuacion = textViewPuntuacion;
    }

    private void inicializarPinturas() {
        paintLineas = new Paint();
        paintLineas.setColor(Color.BLACK);
        paintLineas.setStrokeWidth(8);
        paintLineas.setStyle(Paint.Style.STROKE);

        paintDivisor = new Paint();
        paintDivisor.setColor(Color.BLACK);
        paintDivisor.setStrokeWidth(10);

        paintTexto = new Paint();
        paintTexto.setColor(Color.BLACK);
        paintTexto.setTextSize(40);
        paintTexto.setTextAlign(Paint.Align.CENTER);

        paintTextoNegrita = new Paint();
        paintTextoNegrita.setColor(Color.BLACK);
        paintTextoNegrita.setTextSize(70);
        paintTextoNegrita.setTextAlign(Paint.Align.CENTER);
        paintTextoNegrita.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private Map<String, Map<String, String>> puntuacionesTotales = new HashMap<>();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int marginHorizontal = 10;
        int marginTop = 10;

        int drawableWidth = width - 2 * marginHorizontal;
        int tableroHeight = (int) (height * 0.7);
        cellSize = tableroHeight / 3;
        int espacioInferior = height - tableroHeight;
        int espacioEntreSecciones = (int) (espacioInferior * 0.1);
        int alturaCeldasInferiores = (int) (espacioInferior * 0.8);
        startY = tableroHeight + espacioEntreSecciones + marginTop;

        dibujarLineasInternas(canvas, width, tableroHeight, startY, alturaCeldasInferiores, marginHorizontal, marginTop);

        CBaseDatos baseDatos = new CBaseDatos(getContext());
        ArrayList<String> puntuaciones = baseDatos.obtenerPuntuaciones(jugadorActual);

        Map<String, String> puntuacionesJugador = new HashMap<>();
        for (String puntuacion : puntuaciones) {
            String[] partes = puntuacion.split(": ");
            if (partes.length == 2) {
                puntuacionesJugador.put(partes[0], partes[1]);
            }
        }

        for (int fila = 0; fila < numFilas; fila++) {
            for (int col = 0; col < numColumnas; col++) {
                int left, right;

                if (fila == 3 && col == 0) {
                    left = marginHorizontal + col * drawableWidth / numColumnas;
                    right = marginHorizontal + drawableWidth / 2;
                } else if (fila == 3 && col == 2) {
                    left = marginHorizontal + drawableWidth / 2;
                    right = marginHorizontal + (col + 1) * drawableWidth / numColumnas;
                } else {
                    left = marginHorizontal + col * drawableWidth / numColumnas;
                    right = left + drawableWidth / numColumnas;
                }

                int top = marginTop + ((fila < 3) ? fila * cellSize : startY);
                int bottom = top + ((fila < 3) ? cellSize : alturaCeldasInferiores);

                float textX = left + (right - left) / 2;
                float textY = top + (bottom - top) / 2 - ((paintTexto.descent() + paintTexto.ascent()) / 2);

                String nombreCelda = nombresCeldas[fila][col];
                String valorAsignado = puntuacionesJugador.get(nombreCelda);

                canvas.drawText(nombreCelda, textX, textY, paintTexto);

                if (valorAsignado != null && !valorAsignado.equals("0")) {
                    float imageY = textY + paintTexto.getTextSize() + 5;

                    // Verifica si el valor es "00" para dibujar la imagen de anular
                    if (valorAsignado.equals("00")) {
                        Drawable anularDrawable = getResources().getDrawable(R.drawable.anular);
                        int imageSize = (int) (cellSize * 0.2);
                        int imageLeft = (int) (textX - imageSize / 2);
                        anularDrawable.setBounds(imageLeft, (int) imageY, imageLeft + imageSize, (int) (imageY + imageSize));
                        anularDrawable.draw(canvas);
                    }
                    // Verifica si el nombre de la celda es Escalera, Full o Poker para agregar imágenes adicionales
                    else if (nombreCelda.equals("Escalera") || nombreCelda.equals("Full") || nombreCelda.equals("Poker")) {
                        if (valorAsignado.equals("20") || valorAsignado.equals("30") || valorAsignado.equals("40")) {
                            Drawable huevoDrawable = getResources().getDrawable(R.drawable.huevo);
                            int imageSize = (int) (cellSize * 0.2);
                            int imageLeft = (int) (textX - imageSize / 2);
                            huevoDrawable.setBounds(imageLeft, (int) imageY, imageLeft + imageSize, (int) (imageY + imageSize));
                            huevoDrawable.draw(canvas);
                        } else if (valorAsignado.equals("25") || valorAsignado.equals("35") || valorAsignado.equals("45")) {
                            Drawable manoDrawable = getResources().getDrawable(R.drawable.mano);
                            int imageSize = (int) (cellSize * 0.2);
                            int imageLeft = (int) (textX - imageSize / 2);
                            manoDrawable.setBounds(imageLeft, (int) imageY, imageLeft + imageSize, (int) (imageY + imageSize));
                            manoDrawable.draw(canvas);
                        }
                    }
                    // Verifica si el valor es "50" para agregar la imagen de "grande"
                    else if (valorAsignado.equals("50")) {
                        Drawable grandeDrawable = getResources().getDrawable(R.drawable.grande);
                        int imageSize = (int) (cellSize * 0.2);
                        int imageLeft = (int) (textX - imageSize / 2);
                        grandeDrawable.setBounds(imageLeft, (int) imageY, imageLeft + imageSize, (int) (imageY + imageSize));
                        grandeDrawable.draw(canvas);
                    }
                    // Verifica si el nombre de la celda es Balas, Duke, Trenes, Cuadras, Quinas o Senas
                    else if (nombreCelda.equals("Balas") || nombreCelda.equals("Duke") || nombreCelda.equals("Trenes") ||
                            nombreCelda.equals("Cuadras") || nombreCelda.equals("Quinas") || nombreCelda.equals("Senas")) {
                        // Solo se muestra la puntuación en estas celdas
                        canvas.drawText(valorAsignado, textX, imageY + paintTextoNegrita.getTextSize(), paintTextoNegrita);
                    }
                    // Para las demás celdas se sigue el comportamiento anterior de mostrar puntuación normal
                    else {
                        canvas.drawText(valorAsignado, textX, imageY + paintTextoNegrita.getTextSize(), paintTextoNegrita);
                    }
                }
            }
        }

        int diagonalHeight = (int) (alturaCeldasInferiores * 0.9);
        int diagonalWidth = (int) (cellSize * 0.2);

        int startX = marginHorizontal + (drawableWidth / 2) + 50;
        int endX = startX - diagonalWidth;
        int endY = startY + diagonalHeight;

        canvas.drawLine(startX, startY, endX, endY, paintDivisor);
    }


    private void dibujarLineasInternas(Canvas canvas, int width, int tableroHeight, int startY, int alturaCeldasInferiores, int marginHorizontal, int marginTop) {
        canvas.drawLine(marginHorizontal + width / 3, marginTop, marginHorizontal + width / 3, marginTop + tableroHeight, paintLineas);
        canvas.drawLine(marginHorizontal + 2 * width / 3, marginTop, marginHorizontal + 2 * width / 3, marginTop + tableroHeight, paintLineas);

        int startXHorizontal = marginHorizontal;
        int endXHorizontal = width - marginHorizontal;

        canvas.drawLine(startXHorizontal, marginTop + cellSize, endXHorizontal, marginTop + cellSize, paintLineas);
        canvas.drawLine(startXHorizontal, marginTop + 2 * cellSize, endXHorizontal, marginTop + 2 * cellSize, paintLineas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int width = getWidth();

            int fila = y < startY ? y / cellSize : 3;
            int columna = x / (width / numColumnas);

            String nombreCelda = nombresCeldas[fila][columna];

            // Verifica si la celda está vacía (sin nombre)
            if (!nombreCelda.isEmpty()) {
                mostrarDialogo(getContext(), nombreCelda, jugadorActual);
            }

            return true;
        }
        return super.onTouchEvent(event);
    }


    private void mostrarDialogo(Context context, String nombreCelda, String jugadorActual) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_opciones, null);
        GridView gridViewOpciones = dialogView.findViewById(R.id.gridViewOpciones);
        String[] opciones = obtenerOpcionesPorCelda(nombreCelda);
        OpcionAdapter adapter = new OpcionAdapter(context, Arrays.asList(opciones));
        gridViewOpciones.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Opciones en " + nombreCelda)
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            TextView title = dialog.findViewById(android.R.id.title);
            if (title != null) {
                title.setTextColor(Color.BLACK);
            }
        });

        // En TableroView, modificar el onItemClickListener:
        gridViewOpciones.setOnItemClickListener((parent, view, position, id) -> {
            String valorSeleccionado = opciones[position];
            String valorNumerico = obtenerValorNumerico(valorSeleccionado);

            if (!turnoCambiado) {
                CBaseDatos baseDatos = new CBaseDatos(context);
                boolean exito = baseDatos.agregarPuntuacion(jugadorActual, nombreCelda, valorNumerico);

                if (exito) {
                    invalidate();

                    if (context instanceof JuegoActivity) {
                        JuegoActivity actividad = (JuegoActivity) context;
                        actividad.actualizarContadorCasillas(jugadorActual, nombreCelda, valorNumerico);

                        // Actualizar puntuación inmediatamente
                        int puntuacion = actividad.calcularPuntuacion(jugadorActual);
                        textViewPuntuacion.setText(String.valueOf(puntuacion));

                        // Verificar fin del juego con pequeño retardo
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            actividad.verificarFinDelJuego();
                        }, 300);
                    }
                }
            }
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }





    private int calcularPuntuacion(String jugador) {
        CBaseDatos baseDatos = new CBaseDatos(this.getContext());
        ArrayList<String> puntuaciones = baseDatos.obtenerPuntuaciones(jugador);

        int puntuacionTotal = 0;

        for (String puntuacion : puntuaciones) {
            String[] partes = puntuacion.split(": ");
            if (partes.length == 2) {
                String valor = partes[1];
                try {
                    // Sumar solo los valores numéricos (ignorar "00" y otros no numéricos)
                    if (!valor.equals("00")) {
                        puntuacionTotal += Integer.parseInt(valor);
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores no numéricos
                }
            }
        }

        return puntuacionTotal;
    }

    private String obtenerValorNumerico(String valorSeleccionado) {
        switch (valorSeleccionado) {
            case "Limpiar":
                return "0";
            case "Anular":
                return "00";
            case "Huevo:20":
                return "20";
            case "Mano:25":
                return "25";
            case "Huevo:30":
                return "30";
            case "Mano:35":
                return "35";
            case "Huevo:40":
                return "40";
            case "Mano:45":
                return "45";
            case "Grande:50":
                return "50";
            default:
                return valorSeleccionado;
        }
    }

    public void setJugadorActual(String jugador) {
        this.jugadorActual = jugador;
    }

    private String[] obtenerOpcionesPorCelda(String nombreCelda) {
        switch (nombreCelda) {
            case "Bala":
                return new String[]{"Limpiar", "Anular", "1", "2", "3", "4", "5"};
            case "Duke":
                return new String[]{"Limpiar", "Anular", "2", "4", "6", "8", "10"};
            case "Trenes":
                return new String[]{"Limpiar", "Anular", "3", "6", "9", "12", "15"};
            case "Cuadra":
                return new String[]{"Limpiar", "Anular", "4", "8", "12", "16", "20"};
            case "Quinas":
                return new String[]{"Limpiar", "Anular", "5", "10", "15", "20", "25"};
            case "Senas":
                return new String[]{"Limpiar", "Anular", "6", "12", "18", "24", "30"};
            case "Escalera":
                return new String[]{"Limpiar", "Anular", "Huevo:20", "Mano:25"};
            case "Full":
                return new String[]{"Limpiar", "Anular", "Huevo:30", "Mano:35"};
            case "Poker":
                return new String[]{"Limpiar", "Anular", "Huevo:40", "Mano:45"};
            case "Grande 1":
            case "Grande 2":
                return new String[]{"Limpiar", "Anular", "Grande:50"};
            default:
                return new String[]{""};
        }
    }
}