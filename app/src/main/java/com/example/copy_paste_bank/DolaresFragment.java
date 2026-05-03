package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DolaresFragment extends Fragment {

    private GlobalData glData = GlobalData.getInstance(this.getContext());
    private CurrencyEditText input1;
    private TextView mText1;
    private TextView mText2;
    private Button mButt1;

    private int saveIdx = 1;
    private Double[] saveList = {(double)0};

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dolares, container, false);

        input1 = view.findViewById(R.id.et_dolares);
        mText1 = view.findViewById(R.id.vt_bolivares);
        mText2 = view.findViewById(R.id.vt_tasa);
        mButt1 = view.findViewById(R.id.butt1);

        input1.setHint("Ingrese dólares");

        saveList = glData.getListCalc(saveIdx);

        input1.setText(Basic.setFormatterEs(saveList[1]));
        mText1.setText(Basic.setFormatterEs(saveList[0]+" Bs"));
        mText2.setText(Basic.setFormatterEs(saveList[2]+" Bs"));

        input1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                setCalc();
            }
        });

        mButt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mText1.getText() == "" || mText1.getText().equals("0")){
                    Basic.msg("Valor no VALIDO!");
                }
                else {
                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(AppContextProvider.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Clip Data", mText1.getText());
                    clipboard.setPrimaryClip(clipData);
                    Basic.msg("Copiado al Portapapeles");
                }
            }
        });

        refresh();

        return view;
    }
    @SuppressLint("SetTextI18n")
    private void setCalc(){
        Double mDollar = glData.getTasaDolar();
        Double mResValue = (double)0;
        if (mDollar > 0) {
            mResValue = input1.getNumericValue() * mDollar;
            mText1.setText(Basic.setFormatterEs(mResValue)+" Bs");
        }
        else {
            mText1.setText("0");
        }

        mText2.setText(Basic.setFormatterEs(mDollar)+" Bs");

        // Guarda los valores globalmente: Bolivares, Dolares, Tasa
        glData.setListCalc(new Double[]{mResValue, input1.getNumericValue(), mDollar}, saveIdx);
    }

    /**
     * Método para actualizar el fragmento sin recrearlo
     */
    public void refresh() {
        if (input1 != null) {
            setCalc();
        }
    }
}