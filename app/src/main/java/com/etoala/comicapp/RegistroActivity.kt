package com.etoala.comicapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        initEvents()
    }

    //ir a categorias despues de registrarse
    fun initEvents(){
        val registrarseButton: Button =findViewById<Button>(R.id.registrarseBtn)
        registrarseButton.setOnClickListener{
            val intent= Intent(this, CategoriasActivity::class.java)
            startActivity(intent)

        }
    }
}