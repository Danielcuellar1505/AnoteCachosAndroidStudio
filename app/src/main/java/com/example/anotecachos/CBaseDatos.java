package com.example.anotecachos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class CBaseDatos extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "jugadores.db";
    private static final int VERSION_BD = 2;
    public static final String TABLA_JUGADORES = "jugadores";
    public static final String TABLA_PUNTUACIONES = "puntuaciones";
    private static final String COLUMNA_ID = "id";
    public static final String COLUMNA_NOMBRE = "nombre";

    // Columnas de la tabla puntuaciones
    private static final String COLUMNA_PUNTUACION_ID = "id";
    public static final String COLUMNA_NOMBRE_JUGADOR = "nombre_jugador";
    public static final String COLUMNA_CELDA = "celda";
    public static final String COLUMNA_VALOR = "valor";

    public CBaseDatos(Context contexto) {
        super(contexto, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryCrearTablaJugadores = "CREATE TABLE " + TABLA_JUGADORES + " ("
                + COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMNA_NOMBRE + " TEXT NOT NULL)";
        db.execSQL(queryCrearTablaJugadores);

        String queryCrearTablaPuntuaciones = "CREATE TABLE " + TABLA_PUNTUACIONES + " ("
                + COLUMNA_NOMBRE_JUGADOR + " TEXT NOT NULL, "
                + COLUMNA_CELDA + " TEXT NOT NULL, "
                + COLUMNA_VALOR + " TEXT NOT NULL, "
                + "id_local INTEGER NOT NULL, "
                + "PRIMARY KEY (" + COLUMNA_NOMBRE_JUGADOR + ", id_local))";
        db.execSQL(queryCrearTablaPuntuaciones);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_JUGADORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_PUNTUACIONES);
        onCreate(db);
    }
    public boolean agregarJugador(String nombre) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues valores = new ContentValues();
            valores.put(COLUMNA_NOMBRE, nombre);
            long resultado = db.insert(TABLA_JUGADORES, null, valores);
            return resultado != -1;
        }
    }

    public ArrayList<String> obtenerJugadores() {
        ArrayList<String> listaJugadores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLA_JUGADORES, null);

        if (cursor.moveToFirst()) {
            do {
                listaJugadores.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaJugadores;
    }

    public void eliminarJugador(String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLA_JUGADORES, COLUMNA_NOMBRE + "=?", new String[]{nombre});
        db.close();
    }

    public boolean agregarPuntuacion(String nombreJugador, String celda, String valor) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor cursor = db.query(
                    TABLA_PUNTUACIONES,
                    new String[]{COLUMNA_NOMBRE_JUGADOR, COLUMNA_CELDA},
                    COLUMNA_NOMBRE_JUGADOR + "=? AND " + COLUMNA_CELDA + "=?",
                    new String[]{nombreJugador, celda},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                return actualizarPuntuacion(nombreJugador, celda, valor);
            } else {
                ContentValues valores = new ContentValues();
                valores.put(COLUMNA_NOMBRE_JUGADOR, nombreJugador);
                valores.put(COLUMNA_CELDA, celda);
                valores.put(COLUMNA_VALOR, valor);
                valores.put("id_local", obtenerProximoIdLocal(db, nombreJugador));

                long resultado = db.insert(TABLA_PUNTUACIONES, null, valores);
                return resultado != -1;
            }
        } finally {
            db.close();
        }
    }

    public ArrayList<String> obtenerPuntuaciones(String nombreJugador) {
        ArrayList<String> puntuaciones = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLA_PUNTUACIONES + " WHERE " + COLUMNA_NOMBRE_JUGADOR + "=?", new String[]{nombreJugador});

        if (cursor.moveToFirst()) {
            do {
                int indiceCelda = cursor.getColumnIndex(COLUMNA_CELDA);
                int indiceValor = cursor.getColumnIndex(COLUMNA_VALOR);

                if (indiceCelda >= 0 && indiceValor >= 0) {
                    String celda = cursor.getString(indiceCelda);
                    String valor = cursor.getString(indiceValor);
                    puntuaciones.add(celda + ": " + valor);
                } else {
                    throw new IllegalArgumentException("Una o más columnas no existen en el Cursor.");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return puntuaciones;
    }

    private int obtenerProximoIdLocal(SQLiteDatabase db, String nombreJugador) {
        int proximoId = 1;
        Cursor cursor = db.rawQuery(
                "SELECT MAX(id_local) FROM " + TABLA_PUNTUACIONES + " WHERE " + COLUMNA_NOMBRE_JUGADOR + "=?",
                new String[]{nombreJugador}
        );

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            proximoId = cursor.getInt(0) + 1;
        }
        cursor.close();
        return proximoId;
    }

    public boolean actualizarPuntuacion(String nombreJugador, String celda, String valor) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues valores = new ContentValues();
            valores.put(COLUMNA_VALOR, valor);

            int filasAfectadas = db.update(
                    TABLA_PUNTUACIONES,
                    valores,
                    COLUMNA_NOMBRE_JUGADOR + "=? AND " + COLUMNA_CELDA + "=?",
                    new String[]{nombreJugador, celda}
            );

            return filasAfectadas > 0;
        } finally {
            db.close();
        }
    }
    public boolean borrarPuntuaciones(String nombreJugador) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLA_PUNTUACIONES, COLUMNA_NOMBRE_JUGADOR + "=?", new String[]{nombreJugador});
        int filasBorradas = db.delete(TABLA_JUGADORES, "nombre = ?", new String[]{nombreJugador});
        db.close();
        return filasBorradas > 0;
    }
    public boolean todosLosJugadoresCompletaron() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] categoriasRequeridas = {"Bala", "Duke", "Trenes", "Cuadra", "Quinas", "Senas",
                "Escalera", "Full", "Poker", "Grande 1", "Grande 2"};
        int totalCategorias = categoriasRequeridas.length;
        Cursor cursorJugadores = db.rawQuery("SELECT " + COLUMNA_NOMBRE + " FROM " + TABLA_JUGADORES, null);

        if (cursorJugadores.getCount() == 0) {
            cursorJugadores.close();
            return false;
        }

        boolean todosCompletaron = true;
        while (cursorJugadores.moveToNext()) {
            String jugador = cursorJugadores.getString(0);
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(DISTINCT " + COLUMNA_CELDA + ") " +
                            "FROM " + TABLA_PUNTUACIONES + " " +
                            "WHERE " + COLUMNA_NOMBRE_JUGADOR + " = ? " +
                            "AND " + COLUMNA_CELDA + " IN ('" + String.join("','", categoriasRequeridas) + "') " +
                            "AND " + COLUMNA_VALOR + " NOT IN ('0', '00')",
                    new String[]{jugador});

            if (cursor.moveToFirst()) {
                int categoriasCompletadas = cursor.getInt(0);
                Log.d("BD_DEBUG", jugador + " tiene " + categoriasCompletadas + " de " + totalCategorias);
                if (categoriasCompletadas < totalCategorias) {
                    todosCompletaron = false;
                }
            }
            cursor.close();

            if (!todosCompletaron) break;
        }
        cursorJugadores.close();
        db.close();
        Log.d("BD_DEBUG", "Todos completaron: " + todosCompletaron);
        return todosCompletaron;
    }
    public void borrarPuntuaciones() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM puntuaciones");
        db.close();
    }
    public void borrarTodasLasPuntuaciones() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLA_PUNTUACIONES, null, null);
        } finally {
            db.close();
        }
    }
    public boolean jugadorCompletoTodasCasillas(String nombreJugador) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] casillasRequeridas = {"Bala", "Duke", "Trenes", "Cuadra", "Quinas", "Senas",
                "Escalera", "Full", "Poker", "Grande 1", "Grande 2"};

        try {
            String query = "SELECT COUNT(DISTINCT " + COLUMNA_CELDA + ") " +
                    "FROM " + TABLA_PUNTUACIONES + " " +
                    "WHERE " + COLUMNA_NOMBRE_JUGADOR + " = ? " +
                    "AND " + COLUMNA_CELDA + " IN ('" + String.join("','", casillasRequeridas) + "') " +
                    "AND " + COLUMNA_VALOR + " NOT IN ('0', '00')";

            Cursor cursor = db.rawQuery(query, new String[]{nombreJugador});

            if (cursor.moveToFirst()) {
                int casillasCompletadas = cursor.getInt(0);
                return casillasCompletadas >= casillasRequeridas.length;
            }
            return false;
        } finally {
            db.close();
        }
    }
}