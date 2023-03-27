package com.vicentearmenta.androidtriviatesting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vicentearmenta.androidtriviatesting.R;
import com.vicentearmenta.androidtriviatesting.database.DatabaseOperations;
import com.vicentearmenta.androidtriviatesting.databinding.ActivityResultBinding;
import com.vicentearmenta.androidtriviatesting.models.Answer;
import com.vicentearmenta.androidtriviatesting.models.Question;

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;

    DatabaseOperations mDBOperations;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDBOperations = new DatabaseOperations(com.vicentearmenta.androidtriviatesting.activities.ResultActivity.this);
        List<List<String>> users = mDBOperations.getTopUsers();

        for(int i=0; i < users.size(); i++){
            TextView tempTextView1 = (TextView) binding.topUsersNames.getChildAt(i);
            tempTextView1.setText(users.get(i).get(0));

            TextView tempTextView2 = (TextView) binding.topUsersScores.getChildAt(i);
            tempTextView2.setText(users.get(i).get(1));
        }

    }

}