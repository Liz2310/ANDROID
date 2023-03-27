package com.vicentearmenta.androidtriviatesting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.vicentearmenta.androidtriviatesting.R;
import com.vicentearmenta.androidtriviatesting.database.DatabaseOperations;
import com.vicentearmenta.androidtriviatesting.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    DatabaseOperations mDBOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Data Base CRUD operations
        // m pq es privado (convencion para identificar objetos que solo se usaran en el programa)
        mDBOperations = new DatabaseOperations(MainActivity.this);

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(binding.editName.getText())){
                    Toast.makeText(MainActivity.this, R.string.toastEmptyName, Toast.LENGTH_LONG).show();
                }
                else{
                    String userId = mDBOperations.insertUsername(binding.editName.getText().toString());

                    Intent intent = new Intent(MainActivity.this, Question1Activity.class);
                    startActivity(intent);

                    // putextra es para pasar datos de actividad a actividad
                    intent.putExtra("USERID", userId);
                    intent.putExtra("QUESTIONS", "0"); // preguntas que ya fueron asked
                    startActivity(intent);
                }
            }
        });
    }
}