package com.example.anotecachos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
public class JugadorAdapter extends ArrayAdapter<String> {
    private Context contexto;
    private ArrayList<String> listaJugadores;
    public JugadorAdapter(Context contexto, ArrayList<String> listaJugadores) {
        super(contexto, 0, listaJugadores);
        this.contexto = contexto;
        this.listaJugadores = listaJugadores;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(contexto).inflate(R.layout.item_jugador, parent, false);
        }

        TextView nombreJugadorTextView = convertView.findViewById(R.id.textViewNombreJugador);
        ImageButton botonEliminar = convertView.findViewById(R.id.btnEliminarJugador);

        String nombreJugador = listaJugadores.get(position);
        nombreJugadorTextView.setText(nombreJugador);

        botonEliminar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(contexto).inflate(R.layout.dialog_personalizado, null);
            TextView tituloDialogo = dialogView.findViewById(R.id.tituloDialogo);
            TextView subtituloDialogo = dialogView.findViewById(R.id.subtituloDialogo);
            Button botonCancelar = dialogView.findViewById(R.id.botonCancelar);
            Button botonConfirmar = dialogView.findViewById(R.id.botonConfirmar);

            tituloDialogo.setText("Confirmar eliminación");
            subtituloDialogo.setText("¿Estás seguro de que quieres eliminar a " + nombreJugador + "?");

            AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
            builder.setView(dialogView);

            AlertDialog alertDialog = builder.create();

            botonCancelar.setOnClickListener(v1 -> alertDialog.dismiss());

            botonConfirmar.setOnClickListener(v12 -> {
                CBaseDatos baseDatos = new CBaseDatos(contexto);
                baseDatos.eliminarJugador(nombreJugador);
                listaJugadores.remove(position);
                notifyDataSetChanged();
                alertDialog.dismiss();
            });
            alertDialog.show();
        });
        return convertView;
    }
}
