package com.vicentearmenta.androidtriviatesting.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.vicentearmenta.androidtriviatesting.models.Answer;
import com.vicentearmenta.androidtriviatesting.models.Question;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {

    private SQLiteDatabase mDatabase;
    private final DatabaseHelper mHelper;

    public DatabaseOperations(Context context){
        mHelper = new DatabaseHelper(context);
        this.open();
    }

    public void open() throws SQLException{
        mDatabase = mHelper.getWritableDatabase();
    }

    public void close(){
        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
        }
    }

    public String insertUsername(String username){
        // revisar si la instancia esta abierta
        if (!mDatabase.isOpen()){
            this.open();
        }

        mDatabase.beginTransaction();
        // entra en modo de transaccion (si algo sale mal, se puede hacer roll back)
        // hasta que se le de un succesful (commit) la transaccion
        // queda finalizada, esta se ve reflejada en la bd

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_RS_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_RS_SCORE, 0);
        long lastRowID = mDatabase.insert(DatabaseHelper.TABLE_RESULT, null, values);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        this.close();

        return Long.toString(lastRowID);
    }

    public int updateScore(String userID){
        if (!mDatabase.isOpen()){
            this.open();
        }

        ContentValues values = new ContentValues();

        Cursor c = mDatabase.rawQuery("SELECT RSScore FROM result WHERE _id = " + userID, null);
        c.moveToFirst();
        int value = c.getInt(0);
        c.close();

        values.put(DatabaseHelper.COLUMN_RS_SCORE,
                value + 1);


        int rowsUpdated = mDatabase.update(DatabaseHelper.TABLE_RESULT,
                values,
                DatabaseHelper.COLUMN_RS_ID + " = ?", // condicion para saber donde (es el id de la ronda); el signo de interrogacion es un placeholder (userID va ahi)
                new String[]{ userID });

        this.close();

        return rowsUpdated; // si este regresa 0, significa que no se inserto correctamente
    }

    public Question getNextQuestion(String questionsAlreadyAsked){
        if (!mDatabase.isOpen()){
            this.open();
        }

        String questionId = null;
        String questionText = null;
        String questionAnswer = null;

        // un cursor son como stored procedures
        Cursor cursor = mDatabase.query(
                DatabaseHelper.TABLE_QUESTION,
                new String[]{ // arreglo pq son varias columnas las que estan en la tabla
                        DatabaseHelper.COLUMN_QT_ID,
                        DatabaseHelper.COLUMN_QT_TEXT,
                        DatabaseHelper.COLUMN_QT_ANSWER
                },
                DatabaseHelper.COLUMN_QT_ID + " NOT IN ( " + questionsAlreadyAsked + " )", //preguntas que ya fueron hechas WHEre
                null, // arreglo de los datos necesarios en el placeholder, en este caso es solo unoo para el placeholder (?),  // el valor para la condicion
                null,
                null,
                "RANDOM()",
                "1"
        );

        // solo hace una iteracion
        while(cursor.moveToNext()){
            // se le ocupa decir donde esta el indice de la columna
            // getColumnIndexOrThrow devuelve donde esta esa columna que estamos buscando
            questionId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QT_ID));
            questionText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QT_TEXT));
            questionAnswer = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QT_ANSWER));
        }

        cursor.close(); // siempre hay que cerrar los cursores

        List<Answer> options = new ArrayList<>();

        // opcion A - D traer la opcion correcta
        cursor = mDatabase.query(
                DatabaseHelper.TABLE_ANSWER,
                new String[]{
                        DatabaseHelper.COLUMN_AW_ID,
                        DatabaseHelper.COLUMN_AW_TEXT
                },
                DatabaseHelper.COLUMN_AW_ID + " = ?", // seleccion (SELECT * FROM tabla WHERE = ?) esta parte es la del WHERE
                new String[]{ questionAnswer },
                null,
                null,
                null
        );

        while(cursor.moveToNext()){
            Answer option = new Answer();
            option.setAnswerId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AW_ID)));
            option.setAnswerText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AW_TEXT)));

            options.add(option);
        }

        cursor.close();

        // opciones restantes (incorrectas)
        cursor = mDatabase.query(
                DatabaseHelper.TABLE_ANSWER,
                new String[]{
                        DatabaseHelper.COLUMN_AW_ID,
                        DatabaseHelper.COLUMN_AW_TEXT
                },
                DatabaseHelper.COLUMN_AW_ID + " NOT IN ( ? )", // seleccion (SELECT * FROM tabla WHERE = ?) esta parte es la del WHERE
                new String[]{ questionAnswer },
                null,
                null,
                "RANDOM()",
                "3"
        );

        while(cursor.moveToNext()){
            Answer option = new Answer();
            option.setAnswerId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AW_ID)));
            option.setAnswerText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AW_TEXT)));

            options.add(option);
        }

        cursor.close();

        Question nextQuestion = new Question(
                questionId,
                questionText,
                questionAnswer,
                options.get(0),
                options.get(1),
                options.get(2),
                options.get(3)
        );
        
        return  nextQuestion;

    }


    @SuppressLint("Range")
    public List<List<String>> getTopUsers(){
        List<List<String>> users = new ArrayList<>();

        Cursor cursor = mDatabase.rawQuery("SELECT RSUserName, RSScore " +
                "FROM result " +
                "ORDER BY RSScore " +
                "DESC LIMIT 10", null);

        while (cursor.moveToNext()) {
           List<String> user = new ArrayList<>();
           user.add(cursor.getString(cursor.getColumnIndex("RSUserName")));
           user.add(cursor.getString(cursor.getColumnIndex("RSScore")));
           users.add(user);
        }

        cursor.close();

        return users;
    }

}

// Operaciones CRUD en la tabla