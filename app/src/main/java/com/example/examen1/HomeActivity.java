package com.example.examen1;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.examen1.Adaptadores.ListViewPersonasAdapter;
import com.example.examen1.Models.Persona;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Persona> listPersonas = new ArrayList<Persona>();
    ArrayAdapter<Persona> arrayAdapterPersona;
    ListViewPersonasAdapter listViewPersonasAdapter;
    LinearLayout linearLayoutEditar;
    ListView listViewPersonas;

    EditText inputNombre, inputTelefono, inputCorreo;
    Button btnCancelar, btn_cerrar_sesion;

    Persona personaSeleccionada;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        inputNombre = findViewById(R.id.inputNombre);
        inputTelefono = findViewById(R.id.inputTelefono);
        inputCorreo = findViewById(R.id.inputCorreo);
        btnCancelar = findViewById(R.id.btnCancelar);
        btn_cerrar_sesion = findViewById(R.id.btn_cerrar);

        listViewPersonas = findViewById(R.id.listViewPersonas);
        linearLayoutEditar = findViewById(R.id.linearLayoutEditar);

        listViewPersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                personaSeleccionada = (Persona) parent.getItemAtPosition(position);
                inputNombre.setText(personaSeleccionada.getNombres());
                inputTelefono.setText(personaSeleccionada.getTelefono());
                inputCorreo.setText(personaSeleccionada.getCorreo());

                //Hacemos visible el linearLayout
                linearLayoutEditar.setVisibility(View.VISIBLE);

            }

        });
        btn_cerrar_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(HomeActivity.this, "Sesión Cerrada!", Toast.LENGTH_SHORT).show();
                gologing();
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayoutEditar.setVisibility(View.GONE);
                personaSeleccionada = null;
            }
        });

        inicializarFirebase();
        listarPersonas();
    }
    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void listarPersonas() {
        databaseReference.child("Personas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listPersonas.clear();
                for (DataSnapshot objSnaptshot : dataSnapshot.getChildren()) {
                    Persona p = objSnaptshot.getValue(Persona.class);
                    listPersonas.add(p);
                }

                //Iniciar nuestro Adaptador
                listViewPersonasAdapter = new ListViewPersonasAdapter(HomeActivity.this, listPersonas);
                //arrayAdapterPersona = new ArrayAdapter<Persona>(MainActivity.this, android.R.layout.simple_list_item_1, listPersonas);
                listViewPersonas.setAdapter(listViewPersonasAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crud_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String nombres = inputNombre.getText().toString();
        String telefono = inputTelefono.getText().toString();
        String correo = inputCorreo.getText().toString();

        switch (item.getItemId()) {
            case R.id.menu_agregar:
                insertar();
                break;
            case R.id.menu_guardar:
                if (personaSeleccionada != null) {
                    if (validarInputs() == false) {
                        Persona p = new Persona();
                        p.setIdpersona(personaSeleccionada.getIdpersona());
                        p.setNombres(nombres);
                        p.setTelefono(telefono);
                        p.setCorreo(correo);
                        databaseReference.child("Personas").child(p.getIdpersona()).setValue(p);
                        Toast.makeText(this, "Actualizado Correctamente", Toast.LENGTH_LONG).show();
                        linearLayoutEditar.setVisibility(View.GONE);
                        personaSeleccionada = null;
                    }
                } else {
                    Toast.makeText(this, "Seleccione una Persona", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_eliminar:
                if (personaSeleccionada != null){
                    Persona p2 = new Persona();
                    p2.setIdpersona(personaSeleccionada.getIdpersona());
                    databaseReference.child("Personas").child(p2.getIdpersona()).removeValue();
                    linearLayoutEditar.setVisibility(View.GONE);
                    personaSeleccionada = null;
                    Toast.makeText(this, "Eliminado Correctamente", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this, "Seleccione a una persona para eliminar", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void insertar() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(
                HomeActivity.this
        );
        View mView = getLayoutInflater().inflate(R.layout.insertar, null);
        Button btnInsertar = (Button) mView.findViewById(R.id.btnInsertar);
        final EditText mInputNombres = (EditText) mView.findViewById(R.id.inputNombre);
        final EditText mInputTelefono = (EditText) mView.findViewById(R.id.inputTelefono);
        final EditText mInputCorreo = (EditText) mView.findViewById(R.id.inputCorreo);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        btnInsertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombres = mInputNombres.getText().toString();
                String telefono = mInputTelefono.getText().toString();
                String correo = mInputCorreo.getText().toString();
                if (nombres.isEmpty() || nombres.length() < 3) {
                    showError(mInputNombres, "Nombre Invalido(Min. 3 letras)");
                } else if (telefono.isEmpty() || telefono.length() < 9) {
                    showError(mInputTelefono, "Telefono Invalido (Min. 9 números)");
                } else {
                    Persona p = new Persona();
                    p.setIdpersona(UUID.randomUUID().toString());
                    p.setNombres(nombres);
                    p.setTelefono(telefono);
                    p.setCorreo(correo);
                    databaseReference.child("Personas").child(p.getIdpersona()).setValue(p);
                    Toast.makeText(HomeActivity.this, "Registro Correctamente", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void showError(EditText input, String s) {
        input.requestFocus();
        input.setError(s);
    }

    public boolean validarInputs() {
        String nombre = inputNombre.getText().toString();
        String telefono = inputTelefono.getText().toString();
        String correo = inputCorreo.getText().toString();
        if (nombre.isEmpty() || nombre.length() < 3) {
            showError(inputNombre, "Nombre Inválido(Min. 3 letras)");
        } else if (telefono.isEmpty() || telefono.length() < 9) {
            showError(inputTelefono, "Telefóno Inválido (Min. 9 números)");
            return true;
        }
        return false;
    }

    private void gologing(){
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}
