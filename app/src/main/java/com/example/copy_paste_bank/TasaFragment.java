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

public class TasaFragment extends Fragment {
    private GlobalData glData = GlobalData.getInstance(this.getContext());
    private CurrencyEditText input1;
    private TextView mText1;
    private CurrencyEditText input2;
    private Button mButt1;

    private int saveIdx = 2;
    private Double[] saveList = {(double)0};

    private boolean isEsFormat = true;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasa, container, false);

        input1 = view.findViewById(R.id.et_tasa_dol);
        input2 = view.findViewById(R.id.et_tasa_bs);
        mText1 = view.findViewById(R.id.vt_tasa);
        mButt1 = view.findViewById(R.id.butt1);

        input1.setHint("Ingrese Dolares");
        input2.setHint("Ingrese Dolares");

        saveList = glData.getListCalc(saveIdx);
        isEsFormat = glData.getIsEsFormat();

        input1.setText(Basic.setFormatAlternate(saveList[1], isEsFormat));
        input2.setText(Basic.setFormatAlternate(saveList[0], isEsFormat));
        mText1.setText(Basic.setFormatAlternate(saveList[2], isEsFormat)+" Bs");

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

        input2.addTextChangedListener(new TextWatcher() {
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

        setCalc();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void setCalc(){
        Double valueA = input1.getNumericValue();
        Double valueB = input2.getNumericValue();

        Double mResValue = (double)0;
        if (valueA > 0) {
            mResValue = valueB / valueA;
            mText1.setText(Basic.setFormatAlternate(mResValue, isEsFormat)+" Bs");
        }
        else {
            mText1.setText("");
        }

        // Guarda los valores globalmente: Bolivares, Dolares, Tasa
        glData.setListCalc(new Double[]{input2.getNumericValue(), input1.getNumericValue(), mResValue}, saveIdx);
    }

    /**
     * Método para actualizar el fragmento sin recrearlo
     */
    public void refresh() {
        saveList = glData.getListCalc(saveIdx);
        isEsFormat = glData.getIsEsFormat();
        if (input1 != null && input2 != null) {
            Double value1 = input1.getNumericValue();
            Double value2 = input2.getNumericValue();
            if (isEsFormat) {
                input1.setLocale("ES");
                input2.setLocale("ES");

                input1.setText(Basic.setFormatterEs(value1));
                input2.setText(Basic.setFormatterEs(value2));
            } else {
                input1.setLocale("EN");
                input2.setLocale("EN");

                input1.setText(Basic.setFormatterEn(value1));
                input2.setText(Basic.setFormatterEn(value2));
            }
            setCalc();
        }
    }
}