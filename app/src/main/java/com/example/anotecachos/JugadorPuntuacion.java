package com.example.anotecachos;
public class JugadorPuntuacion {
    private String nombre;
    private int puntuacion;

    public JugadorPuntuacion(String nombre, int puntuacion) {
        this.nombre = nombre;
        this.puntuacion = puntuacion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntuacion() {
        return puntuacion;
    }
}