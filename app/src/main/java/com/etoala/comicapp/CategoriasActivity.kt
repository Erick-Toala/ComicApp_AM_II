package com.etoala.comicapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.cardview.widget.CardView

class CategoriasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        initEvents()

    }

    fun initEvents(){
        val cardView:CardView=findViewById<CardView>(R.id.CategoriaCienciaFiccionCw)
        cardView.setOnClickListener{
            val intent=Intent(this, ListaComicsAdminActivity::class.java)
            startActivity(intent)
        }
        val buttonCategoria:Button=findViewById<Button>(R.id.botonCrearCategoria)
        buttonCategoria.setOnClickListener{
            val intent=Intent(this, CrearCategoriaActivity::class.java)
            startActivity(intent)
        }
        val buttonComic:Button=findViewById<Button>(R.id.botonAnadirComic)
        buttonComic.setOnClickListener{
            val intent=Intent(this, AnadirComicActivity::class.java)
            startActivity(intent)
        }
        val imageButton: ImageButton =findViewById<ImageButton>(R.id.salirBoton)
        imageButton.setOnClickListener{
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}