package com.example.anotecachos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
public class ResultadoAdapter extends BaseAdapter {
    private List<JugadorPuntuacion> datos;
    private LayoutInflater inflater;
    public ResultadoAdapter(Context context, List<JugadorPuntuacion> datos) {
        this.datos = datos;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datos.size();
    }

    @Override
    public Object getItem(int position) {
        return datos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_resultado, parent, false);
            holder = new ViewHolder();
            holder.textViewNombre = convertView.findViewById(R.id.textViewNombre);
            holder.textViewPuntuacion = convertView.findViewById(R.id.textViewPuntuacion);
            holder.imageViewPosicion = convertView.findViewById(R.id.imageViewPosicion);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        JugadorPuntuacion jugador = (JugadorPuntuacion) getItem(position);
        holder.textViewNombre.setText(jugador.getNombre());
        holder.textViewPuntuacion.setText(String.valueOf(jugador.getPuntuacion()) + " pts");
        holder.imageViewPosicion.setVisibility(View.VISIBLE);
        switch(position) {
            case 0:
                holder.imageViewPosicion.setImageResource(R.drawable.primero);
                break;
            case 1:
                holder.imageViewPosicion.setImageResource(R.drawable.segundo);
                break;
            case 2:
                holder.imageViewPosicion.setImageResource(R.drawable.tercero);
                break;
            default:
                holder.imageViewPosicion.setVisibility(View.GONE);
        }

        ViewGroup parentLayout = (ViewGroup) convertView;
        switch(position) {
            case 0:
                parentLayout.setBackgroundResource(R.drawable.borde_amarillo);
                break;
            case 1:
                parentLayout.setBackgroundResource(R.drawable.borde_gris);
                break;
            case 2:
                parentLayout.setBackgroundResource(R.drawable.borde_marron);
                break;
            default:
                parentLayout.setBackgroundResource(R.drawable.borde_normal);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textViewNombre;
        TextView textViewPuntuacion;
        ImageView imageViewPosicion;
    }
}

