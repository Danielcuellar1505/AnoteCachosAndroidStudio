package com.example.anotecachos;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class OpcionAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> opciones;

    public OpcionAdapter(@NonNull Context context, List<String> opciones) {
        super(context, R.layout.item_opcion_imagen, opciones);
        this.context = context;
        this.opciones = opciones;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_opcion_imagen, parent, false);
        }
        String opcion = opciones.get(position);
        ImageView imageViewOpcion = convertView.findViewById(R.id.imageViewOpcion);
        TextView textViewOpcion = convertView.findViewById(R.id.textViewOpcion);
        textViewOpcion.setTypeface(null, Typeface.BOLD);

        if (opcion.equals("Limpiar")) {
            imageViewOpcion.setImageResource(R.drawable.limpiar);
            textViewOpcion.setText("Limpiar");
            imageViewOpcion.setVisibility(View.VISIBLE);
        } else if (opcion.equals("Anular")) {
            imageViewOpcion.setImageResource(R.drawable.anular);
            textViewOpcion.setText("Anular");
            imageViewOpcion.setVisibility(View.VISIBLE);
        } else if (opcion.startsWith("Huevo")) {
            imageViewOpcion.setImageResource(R.drawable.huevo);
            textViewOpcion.setText("Huevo");
            imageViewOpcion.setVisibility(View.VISIBLE);
        } else if (opcion.startsWith("Mano")) {
            imageViewOpcion.setImageResource(R.drawable.mano);
            textViewOpcion.setText("Mano");
            imageViewOpcion.setVisibility(View.VISIBLE);
        } else if (opcion.startsWith("Grande")) {
            imageViewOpcion.setImageResource(R.drawable.grande);
            textViewOpcion.setText("Grande");
            imageViewOpcion.setVisibility(View.VISIBLE);
        } else {
            imageViewOpcion.setVisibility(View.GONE);
            textViewOpcion.setText(opcion);
        }
        return convertView;
    }
}