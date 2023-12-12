package com.etoala.comicapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.etoala.comicapp.databinding.ActivityEditarComicBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditarComicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarComicBinding
    
    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    private var comicId = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArrayList: ArrayList<String>

    private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarComicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comicId = intent.getStringExtra("comicId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()

        loadComicInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadComicInfo() {
        Log.d(TAG, "loadComicInfo: Loading comic info")

        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child(comicId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    Log.d(TAG, "onDataChange: Loading comic category info")
                    val refComicCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refComicCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value

                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this, "Introduce el nombre del Comic", Toast.LENGTH_SHORT).show()
        }
        else if(description.isEmpty()){
            Toast.makeText(this, "Introduce una descripción", Toast.LENGTH_SHORT).show()
        }
        else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Elije una categoría", Toast.LENGTH_SHORT).show()
        }
        else{
            uploadPdf()
        }
    }

    private fun uploadPdf() {
        Log.d(TAG, "uploadPdf: Starting uploading pdf info...")
        progressDialog.setMessage("Cargando Información del Comic...")
        progressDialog.show()


        val hashMap = HashMap<String, Any>()
        hashMap["title"]="$title"
        hashMap["description"]="$description"
        hashMap["categoryId"]="$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child(comicId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "uploadPdf: Update successfully...")
                Toast.makeText(this, "Actualización exitosa", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Error al actualizar debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Elige una opción")
            .setItems(categoriesArray){dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}