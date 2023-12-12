package com.etoala.comicapp

import android.app.AlertDialog
import android.app.Application.ActivityLifecycleCallbacks
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.etoala.comicapp.databinding.ActivityAnadirComicBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AnadirComicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnadirComicBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri: Uri? = null

    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnadirComicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Por favor")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        //{}
        if(title.isEmpty()){
            Toast.makeText(this, "Introduce el nombre del Comic", Toast.LENGTH_SHORT).show()
        }
        else if(description.isEmpty()){
            Toast.makeText(this, "Introduce una descripción", Toast.LENGTH_SHORT).show()
        }
        else if(category.isEmpty()){
            Toast.makeText(this, "Elije una categoría", Toast.LENGTH_SHORT).show()
        }
        else if(pdfUri == null){
            Toast.makeText(this, "Elije el comic (PDF)", Toast.LENGTH_SHORT).show()
        }
        else{
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")
        progressDialog.setMessage("Cargando Comic...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Comics/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener{taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadPdfUrl, timestamp)

            }
            .addOnFailureListener{e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo cargar debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Subiendo información en pdf...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] ="$uid"
        hashMap["id"]="$timestamp"
        hashMap["title"]="$title"
        hashMap["description"]="$description"
        hashMap["categoryId"]="$selectedCatedoryId"
        hashMap["url"]="$uploadPdfUrl"
        hashMap["timestamp"]=timestamp
        hashMap["viewsCount"]=0
        hashMap["downloadCount"]=0


        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Cargado", Toast.LENGTH_SHORT).show()
                pdfUri=null
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo cargar debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private var selectedCatedoryId =""
    private var selectedCatedoryTitle =""

    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Elegir categoría")
            .setItems(categoriesArray){dialog, which ->
                selectedCatedoryTitle = categoryArrayList[which].category
                selectedCatedoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCatedoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCatedoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCatedoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )
}