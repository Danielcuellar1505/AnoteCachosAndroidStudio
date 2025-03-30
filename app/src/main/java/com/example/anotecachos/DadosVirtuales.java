package com.example.anotecachos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DadosVirtuales {
    private Random random = new Random();
    private static final int MAX_CIRCLES = 6;
    private static final int SQUARE_SIZE = 180;
    private static final int MAX_ROTATIONS = 3;

    private List<Dice> topDices = new ArrayList<>();
    private List<Dice> bottomDices = new ArrayList<>();
    private AlertDialog dialog;
    private int rotationCount = 0;
    private Button rotateButton;

    public void mostrarDialogoDados(Context context) {
        initializeDices();

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_dice_game, null);

        GridView topGridView = dialogView.findViewById(R.id.gridTop);
        GridView bottomGridView = dialogView.findViewById(R.id.gridBottom);
        DiceAdapter topAdapter = new DiceAdapter(context, topDices, true, SQUARE_SIZE, SQUARE_SIZE);
        DiceAdapter bottomAdapter = new DiceAdapter(context, bottomDices, false, SQUARE_SIZE, SQUARE_SIZE);
        topGridView.setAdapter(topAdapter);
        bottomGridView.setAdapter(bottomAdapter);

        topGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (topDices.get(position).getCircleCount() > 0) {
                moveDiceToBottom(position, topAdapter, bottomAdapter);
            }
        });
        bottomGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (bottomDices.get(position).getCircleCount() > 0) {
                moveDiceToTop(position, topAdapter, bottomAdapter);
            }
        });
        rotateButton = dialogView.findViewById(R.id.btnRotate);
        rotateButton.setOnClickListener(v -> {
            if (rotationCount < MAX_ROTATIONS) {
                if (!topDices.isEmpty()) {
                    rotateTopDices(topAdapter);
                    rotationCount++;
                    updateRotateButtonText();
                } else {
                    Toast.makeText(context, "No hay dados para girar", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Has alcanzado el límite de giros", Toast.LENGTH_SHORT).show();
            }
        });

        updateRotateButtonText();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setCancelable(false);
        dialog = builder.create();

        Button closeButton = dialogView.findViewById(R.id.botonCancelar);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        Button toggleVisibilityButton = dialogView.findViewById(R.id.btnToggleVisibility);
        toggleVisibilityButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (dialog != null && dialog.getWindow() != null) {
                            dialog.getWindow().getDecorView().setVisibility(View.INVISIBLE);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (dialog != null && dialog.getWindow() != null) {
                            dialog.getWindow().getDecorView().setVisibility(View.VISIBLE);
                        }
                        return true;
                }
                return false;
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    private void initializeDices() {
        topDices.clear();
        bottomDices.clear();
        rotationCount = 0;
        for (int i = 0; i < 5; i++) {
            topDices.add(new Dice(0));
        }
        updateRotateButtonText();
    }
    private void updateRotateButtonText() {
        if (rotateButton != null) {
            rotateButton.setText("Girar (" + (MAX_ROTATIONS - rotationCount) + " tiros)");

            rotateButton.setEnabled(rotationCount < MAX_ROTATIONS);
            if (rotationCount >= MAX_ROTATIONS) {
                rotateButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else {
                rotateButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2089dc")));
            }
        }
    }
    private void moveDiceToBottom(int position, DiceAdapter topAdapter, DiceAdapter bottomAdapter) {
        if (position < topDices.size()) {
            Dice dice = topDices.remove(position);
            bottomDices.add(dice);
            topAdapter.notifyDataSetChanged();
            bottomAdapter.notifyDataSetChanged();
        }
    }
    private void moveDiceToTop(int position, DiceAdapter topAdapter, DiceAdapter bottomAdapter) {
        if (topDices.size() < 5 && position < bottomDices.size()) {
            Dice dice = bottomDices.remove(position);
            topDices.add(dice);
            topAdapter.notifyDataSetChanged();
            bottomAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(dialog.getContext(), "Solo puedes tener 5 cuadrados arriba", Toast.LENGTH_SHORT).show();
        }
    }
    private void rotateTopDices(DiceAdapter adapter) {
        for (Dice dice : topDices) {
            dice.setCircleCount(random.nextInt(MAX_CIRCLES) + 1);
        }
        adapter.notifyDataSetChanged();
    }
}