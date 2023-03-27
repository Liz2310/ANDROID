package com.vicentearmenta.androidtriviatesting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vicentearmenta.androidtriviatesting.R;
import com.vicentearmenta.androidtriviatesting.database.DatabaseOperations;
import com.vicentearmenta.androidtriviatesting.databinding.ActivityQuestion1Binding;
import com.vicentearmenta.androidtriviatesting.models.Answer;
import com.vicentearmenta.androidtriviatesting.models.Question;

import java.util.List;

public class Question1Activity extends AppCompatActivity {
    ActivityQuestion1Binding binding;

    DatabaseOperations mDBOperations;

    String userId;
    String questionsAlreadyAsked;
    int finalCorrectAnswerRdBtn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestion1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userId = intent.getStringExtra("USERID");
        questionsAlreadyAsked = intent.getStringExtra("QUESTIONS");

        mDBOperations = new DatabaseOperations(Question1Activity.this);

        /* Esto aplica solo para la primer pregunta */
        // boton back invisible
        binding.backButton.setVisibility(View.INVISIBLE);

        // boton next not enabled pq no quiere le de next sin antes contestar la pregunta
        binding.nextButton.setEnabled(false);

        // traer la siguiente pregunta
        Question question = mDBOperations.getNextQuestion(questionsAlreadyAsked);

        // metodo para sacar la pregunta y ponerla en el texto de edittext
        binding.questionText.setText(question.getQuestionText());

        // agarrar el id de la pregunta para que la imagen haga match con ella
        String drawableName = "image" + question.getQuestionID();

        // poner la imagen
        // nombre imagen, tipo de imagen, paquete en donde esta la imagen
        binding.imagePlaceholder.setImageResource(getResources().getIdentifier(drawableName, "drawable", getPackageName()));

        // concatenar las preguntas ya hechas
        questionsAlreadyAsked = questionsAlreadyAsked + "," + question.getQuestionID();

        List<Answer> answers = question.getAllAnswers();

        // acomodar preguntas en la actividad
        for(int i=0; i < 4; i++){
            // get child at trae hijo en la vista por entero
            RadioButton tempRadioButton = (RadioButton) binding.rgAnswers.getChildAt(i);
            Answer tempAnswer = answers.get(i);

            // comparar si la respuesta por la que se esta iterando, su id es igual
            // al de la respuesta correcta
            if(question.getQuestionAnswer().equals(tempAnswer.getAnswerId())){
                finalCorrectAnswerRdBtn = tempRadioButton.getId();
            }

            // poniendo texto de la answer actual en la iteracion
            tempRadioButton.setText(tempAnswer.getAnswerText());
        }

        binding.rgAnswers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                binding.nextButton.setEnabled(true);
                // vamos a revisar que eligio el usuario
                evaluateAnswerSelection(radioGroup, i);
            }
        });

        // cuando demos click al next button para la sig pregunta
        // pasar a la sig activity el userid y las preguntas preguntadas
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Question1Activity.this, Question2Activity.class);
                intent.putExtra("USERID", userId);
                intent.putExtra("QUESTIONS", questionsAlreadyAsked);
                startActivity(intent);
            }
        });
    }


    public int evaluateAnswerSelection(RadioGroup radioGroup, int selectedAnswer){
        int score = 0;
        // siempre colorear el radio button que es correcto, aunque se haya equivocado
        RadioButton tempRdButton = findViewById(finalCorrectAnswerRdBtn);
        tempRdButton.setButtonDrawable(R.drawable.ic_right); // icono del radio button

        // cambiar color icono de check
        tempRdButton.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#9AD680")));

        if(selectedAnswer == finalCorrectAnswerRdBtn){
            mDBOperations.updateScore(getIntent().getExtras().getString("USERID"));
            score++;
        }else {
            // si el que el usuario selecciono es el incorrecto, ponerlo mal (nomas esa)
            RadioButton tempRdButton2 = findViewById(selectedAnswer);
            tempRdButton2.setButtonDrawable(R.drawable.ic_wrong);
            tempRdButton2.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#93000A")));
        }

        // no clickear botones despues de evaluar la respuesta
        for(int i=0; i < radioGroup.getChildCount(); i++){
            radioGroup.getChildAt(i).setClickable(false);
        }

        return score;
    }
}