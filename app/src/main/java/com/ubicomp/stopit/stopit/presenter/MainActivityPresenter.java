package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubicomp.stopit.stopit.R;

public class MainActivityPresenter extends AppCompatActivity {

    private DatabaseReference mDatabase;
    static public int USERS_COUNT;
    static public String USERNAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_presenter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException exception) {
            Log.d("StopIt", "Couldn't hide bar title");
        }

        final Button registerButton = findViewById(R.id.registerButton);
        final TextView idText = findViewById(R.id.idText);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("USERS_COUNT").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    USERS_COUNT = dataSnapshot.getValue(Integer.class);
                    Log.d("StopIt", "" + USERS_COUNT);
                } catch (DatabaseException exception) {
                    Log.d("StopIt", "No existing USERS_COUNT, setting it to 0");
                    USERS_COUNT = 0;
                }
                if (USERS_COUNT == 0)
                    mDatabase.child("USERS_COUNT").setValue(1);

                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText usernameText = findViewById(R.id.usernameText);
                        USERNAME = usernameText.getText().toString();
                        USERNAME += "_id_" + ++USERS_COUNT;
                        if (!USERNAME.matches("")) {
                            mDatabase.child(USERNAME + "_id_" + USERS_COUNT).child("user_id").setValue(USERS_COUNT);
                            mDatabase.child("USERS_COUNT").setValue(USERS_COUNT);
                            idText.setText(getString(R.string.userIDText, USERNAME, USERS_COUNT));
                            registerButton.setVisibility(View.GONE);
                            usernameText.setVisibility(View.GONE);
                            Toast.makeText(MainActivityPresenter.this, "Registered OK",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivityPresenter.this, "You did not enter a username",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void spiralTest(View view) {
        Intent intent= new Intent(this,SpiralActivityPresenter.class);
        intent.putExtra("background","spiral");
        startActivity(intent);
    }

    public void shapeTest(View view) {
        Intent intent= new Intent(this,SpiralActivityPresenter.class);
        intent.putExtra("background","square");
        startActivity(intent);
    }
}
