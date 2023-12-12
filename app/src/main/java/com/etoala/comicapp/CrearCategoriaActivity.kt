package com.etoala.comicapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.etoala.comicapp.databinding.ActivityCrearCategoriaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CrearCategoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearCategoriaBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty()){
            Toast.makeText(this, "Ingresar Categoría...", Toast.LENGTH_SHORT).show()
        }
        else{
            addCateroryFirebase()
        }
    }

    private fun addCateroryFirebase() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val hasMap = HashMap<String, Any>()
        hasMap["id"] = "$timestamp"
        hasMap["category"] = category
        hasMap["timestamp"] = timestamp
        hasMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hasMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Agregado exitosamente...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo agregar debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}