package com.example.anotecachos;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.List;

public class DiceAdapter extends BaseAdapter {
    private Context context;
    private List<Dice> dices;
    private boolean isTop;
    private int squareSizeTop;
    private int squareSizeBottom;

    public DiceAdapter(Context context, List<Dice> dices, boolean isTop,
                       int squareSizeTop, int squareSizeBottom) {
        this.context = context;
        this.dices = dices;
        this.isTop = isTop;
        this.squareSizeTop = squareSizeTop;
        this.squareSizeBottom = squareSizeBottom;
    }

    @Override
    public int getCount() {
        return dices.size();
    }

    @Override
    public Object getItem(int position) {
        return dices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout square;
        int size = squareSizeTop; // Usar siempre el tamaño superior

        if (convertView == null) {
            square = new FrameLayout(context);
            square.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            square.setBackgroundResource(R.drawable.square_border);
        } else {
            square = (FrameLayout) convertView;
        }

        square.removeAllViews();

        Dice dice = dices.get(position);
        if (dice.getCircleCount() == 0) {
            // Mostrar texto "Gira" si no tiene círculos
            TextView textView = new TextView(context);
            textView.setText("Gira");
            textView.setTextSize(18);
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER);
            square.addView(textView);

            square.setClickable(false);
            square.setFocusable(false);
            square.setEnabled(false);
        } else {
            // Mostrar círculos
            for (int j = 0; j < dice.getCircleCount(); j++) {
                View circle = createCircle(dice.getCircleCount(), j, size);
                square.addView(circle);
            }
        }

        return square;
    }

    private View createCircle(int circleCount, int position, int squareSize) {
        View circle = new View(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(40, 40);

        // Ajustar posiciones según el número de círculos
        float scale = squareSize / 250f;

        switch (circleCount) {
            case 1:
                params.leftMargin = (int)(95 * scale);
                params.topMargin = (int)(95 * scale);
                break;
            case 2:
                if (position == 0) {
                    params.leftMargin = (int)(40 * scale);
                    params.topMargin = (int)(40 * scale);
                } else {
                    params.leftMargin = (int)(160 * scale);
                    params.topMargin = (int)(160 * scale);
                }
                break;
            case 3:
                if (position == 0) {
                    params.leftMargin = (int)(25 * scale);
                    params.topMargin = (int)(25 * scale);
                } else if (position == 1) {
                    params.leftMargin = (int)(95 * scale);
                    params.topMargin = (int)(95 * scale);
                } else {
                    params.leftMargin = (int)(170 * scale);
                    params.topMargin = (int)(170 * scale);
                }
                break;
            case 4:
                if (position == 0) {
                    params.leftMargin = (int)(45 * scale);
                    params.topMargin = (int)(45 * scale);
                } else if (position == 1) {
                    params.leftMargin = (int)(145 * scale);
                    params.topMargin = (int)(45 * scale);
                } else if (position == 2) {
                    params.leftMargin = (int)(45 * scale);
                    params.topMargin = (int)(145 * scale);
                } else {
                    params.leftMargin = (int)(145 * scale);
                    params.topMargin = (int)(145 * scale);
                }
                break;
            case 5:
                if (position == 0) {
                    params.leftMargin = (int)(45 * scale);
                    params.topMargin = (int)(45 * scale);
                } else if (position == 1) {
                    params.leftMargin = (int)(150 * scale);
                    params.topMargin = (int)(45 * scale);
                } else if (position == 2) {
                    params.leftMargin = (int)(45 * scale);
                    params.topMargin = (int)(150 * scale);
                } else if (position == 3) {
                    params.leftMargin = (int)(150 * scale);
                    params.topMargin = (int)(150 * scale);
                } else {
                    params.leftMargin = (int)(95 * scale);
                    params.topMargin = (int)(95 * scale);
                }
                break;
            case 6:
                if (position < 3) {
                    // Aumenté el 15 a 25 (más separación lateral) y 20 a 30 (más separación superior)
                    params.leftMargin = (int) ((int)((position % 3) * 60 + 35) * scale);
                    params.topMargin = (int)(50 * scale); // Más separación del borde superior
                } else {
                    // Aumenté el 15 a 25 (más separación lateral) y 120 a 130 (más separación inferior)
                    params.leftMargin = (int) ((int)(((position - 3) % 3) * 60 + 35) * scale);
                    params.topMargin = (int)(150 * scale); // Más separación del borde inferior
                }
                break;
        }

        circle.setLayoutParams(params);
        circle.setBackgroundResource(R.drawable.circle_shape);

        ObjectAnimator animator = ObjectAnimator.ofFloat(circle, "alpha", 0f, 1f);
        animator.setDuration(300);
        animator.start();

        return circle;
    }
}