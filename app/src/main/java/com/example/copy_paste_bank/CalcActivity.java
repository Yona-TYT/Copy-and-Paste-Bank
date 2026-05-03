package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CalcActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialButtonToggleGroup toggleGroup;
    private FrameLayout contentContainer;

    private Button mButt1;
    private Button mButt2;

    private Spinner mSpin1;
    private Spinner mSpin2;

    private List<String> mSpinL2 = Arrays.asList("Bolivares", "Dolar", "Tasa");
    private Double[] listCalc = {(double)0, (double)0, (double)0};
    private int currSel2 = 1;
    private int currFrag = 0;


    private CurrencyEditText mInput1;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getAppContext());

    Fragment currFragment = new BolivaresFragment();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Se configura el Boton nav Back -----------------------------------------------
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                CalcActivity.this.finish();
            }
        };
        onBackPressedDispatcher.addCallback(this, callback);
        //---------------------------------------------------------------------------------

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);

//        //Activate ToolBar
//        Toolbar myToolbar = findViewById(R.id.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Calculadora de Dolar");
        actionBar.setDisplayShowHomeEnabled(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mInput1 =  findViewById(R.id.input1);
        mButt1 = findViewById(R.id.butt1);
        mButt2 = findViewById(R.id.butt2);
        mSpin1 = findViewById(R.id.spin1);
        mSpin2 = findViewById(R.id.spin2);

        mButt1.setOnClickListener(this);
        mButt2.setOnClickListener(this);

        //Limpia los valores guardados
        glData.cleanListCalc();

        //Para la lista de Monitores de dolar ------------------------------------------------------
        SelecAdapter adapt1 = new SelecAdapter(this, glData.getSpinTasa());
        mSpin1.setAdapter(adapt1);
        //mSpin1.setSelection(currSel1); //Set La Moneda como default
        mSpin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                glData.setOptTasa(i);

                Double mDollar = GetDollar.mDollar.get(i);
                glData.setTasaDolar(mDollar);

                int inputIdx = GetDollar.mDollar.size()-1;


                if(i == inputIdx){
                    if(mDollar > 0){
                        mInput1.setText(Basic.setFormatAlternate(mDollar.toString(), glData.getIsEsFormat()));
                    }
                    Basic.setReadOnly(mInput1, false);
                }
                else {
                    mInput1.setText(Basic.setFormatAlternate(mDollar.toString(), glData.getIsEsFormat()));
                    Basic.setReadOnly(mInput1, true);
                }

                View spinnerSel = mSpin1.getSelectedView();
                if(spinnerSel != null) {
                    TextView mView = spinnerSel.findViewWithTag(i);
                    mView.setText(glData.getSpinTasa().get(i));
                    //Basic.msg(""+mSpin1.getSelectedView().findViewWithTag(i));
                }

                // Recarga el fragment actual
                recargarFragment(currFragment);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                Double mDollar = mInput1.getNumericValue();
                if (mDollar > 0) {
                    glData.setTasaDolar(mDollar);
                    int inputIdx = GetDollar.mDollar.size()-1;
                    if(inputIdx == glData.optTasa ) {
                        GetDollar.mDollar.set(inputIdx, mDollar);
                    }
                }
                else {
                    glData.setTasaDolar(0);
                }

                // Recarga el fragment actual
                recargarFragment(currFragment);
            }
        });


        //Para la lista de Selector de Save ------------------------------------------------------
        SelecAdapter adapt2 = new SelecAdapter(this, mSpinL2);
        mSpin2.setAdapter(adapt2);
        mSpin2.setSelection(currSel2); // opcion como default
        mSpin2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currSel2 = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        toggleGroup = findViewById(R.id.toggleGroup);
        contentContainer = findViewById(R.id.content_container);

        // Seleccionar la primera opción por defecto
        toggleGroup.check(R.id.btn_bolivares);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_bolivares) {
                currFrag = 0;
                mSpin2.setSelection(1);
                mostrarFragment(new BolivaresFragment());
            }
            else if (checkedId == R.id.btn_dolares) {
                currFrag = 1;
                mSpin2.setSelection(0);
                mostrarFragment(new DolaresFragment());
            }
            else if (checkedId == R.id.btn_tasa) {
                currFrag = 2;
                mSpin2.setSelection(2);
                mostrarFragment(new TasaFragment());
            }
        });

        // Fragmento inicial
        mostrarFragment(new BolivaresFragment());
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        Basic.msg(""+glData.getOptTasa());
//        mSpin1.setSelection(glData.getOptTasa());
//        mSpin2.setSelection(currSel2);
//
//    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        GetDollar mGet = new GetDollar(AppContextProvider.getAppContext(), CalcActivity.this, mSpin1 , mInput1);
        try {
            GetDollar.urlRun();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarFragment(Fragment fragment) {
        currFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_container, fragment)
                .commit();
    }

    private void recargarFragment(Fragment fragment) {
        if (fragment != null) {
            if (fragment instanceof DolaresFragment) {
                ((DolaresFragment) fragment).refresh();
            } else if (fragment instanceof BolivaresFragment) {
                ((BolivaresFragment) fragment).refresh();
            } else if (fragment instanceof TasaFragment) {
                ((TasaFragment) fragment).refresh();
            }
        }
    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //Boton que recarga el precio dolar
        if (itemId == R.id.butt1) {
            GetDollar mGet = new GetDollar(AppContextProvider.getAppContext(), CalcActivity.this, mSpin1, mInput1);
            try {
                GetDollar.urlRun();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (itemId == R.id.butt2) {
            glData.setSendValue(glData.getListCalc(currFrag)[currSel2]);
            this.finish();
        }
    }
}