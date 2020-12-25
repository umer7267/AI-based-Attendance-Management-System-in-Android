package com.quince.registeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quince.registeradmin.common.Common;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context context = MainActivity.this;

    private EditText id, name, email, pass;
    private Button register;

    private String belongs_to, dept;

    private Spinner university, department;

    private ProgressDialog progressDialog;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = findViewById(R.id.unique_id);
        name = findViewById(R.id.name);
        university = findViewById(R.id.choose_uni);
        department = findViewById(R.id.choose_dept);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        register = findViewById(R.id.register);

        initSpinner();

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Common.COL_ADMINS);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmpty(id) || isEmpty(name) || TextUtils.isEmpty(belongs_to) || TextUtils.isEmpty(dept) || isEmpty(email) || isEmpty(pass)) {
                    Toast.makeText(context, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                } else {
                    registerAdmin(getString(id), getString(name), belongs_to, dept, getString(email), getString(pass));
                }
            }
        });

    }

    private void initSpinner() {
        ArrayAdapter<String> UniversitiesArray = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.universities)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(getResources().getColor(R.color.default_color));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.black));
                }
                return view;
            }
        };

        UniversitiesArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        university.setAdapter(UniversitiesArray);

        university.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                belongs_to = (String) parent.getItemAtPosition(position);
                if (belongs_to.equals("Choose University")){
                    TextView tv = (TextView) view;
                    tv.setTextColor(getResources().getColor(R.color.default_color));
                    belongs_to = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> DepartmentsArray = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.departments)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(getResources().getColor(R.color.default_color));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.black));
                }
                return view;
            }
        };

        DepartmentsArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        department.setAdapter(DepartmentsArray);

        department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dept = (String) parent.getItemAtPosition(position);
                if (dept.equals("Choose Department")){
                    TextView tv = (TextView) view;
                    tv.setTextColor(getResources().getColor(R.color.default_color));
                    dept = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void registerAdmin(String id, String name, String university, String dept, String email, String pass) {
        progressDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Registering Admin...");
        progressDialog.show();

        Map<String, Object> adminData = new HashMap<>();

        adminData.put(Common.KEY_ID, id);
        adminData.put(Common.KEY_NAME, name);
        adminData.put(Common.KEY_BELONGS_TO, university);
        adminData.put(Common.KEY_DEPT, dept);
        adminData.put(Common.KEY_EMAIL, email);
        adminData.put(Common.KEY_PASSWORD, pass);

        mDatabaseReference.child(university).child(dept)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Admin Already Registered in this Department", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    mDatabaseReference.child(university)
                                            .child(dept)
                                            .child(id)
                                            .setValue(adminData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(context, "Admin Registered Successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private boolean isEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString());
    }

    private String getString(EditText editText) {
        return editText.getText().toString();
    }
}
