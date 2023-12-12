package com.etoala.comicapp

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.etoala.comicapp.databinding.ActivityDetallesComicBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class DetallesComicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesComicBinding

    private companion object{
        const val TAG = "COMIC_DETAILS_TAG"
    }

    private var comicId = ""

    private var comicTitle = ""
    private var comicUrl = ""

    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallesComicBinding.inflate(layoutInflater)
        setContentView(binding.root)


        comicId = intent.getStringExtra("comicId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            checkIsFavorite()
        }

        MyApplication.incrementComicViewCount(comicId)

        loadComicDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readComicBtn.setOnClickListener {
            val intent = Intent(this, VerComicActivity::class.java)
            intent.putExtra("comicId", comicId)
            startActivity(intent)
        }

        binding.downloadComicBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadComic()
            }
            else{
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted, LETS request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //codigo add/delete favoritos
        binding.favoriteComicBtn.setOnClickListener {
            if(firebaseAuth.currentUser == null){
                Toast.makeText(this, "No has iniciado sesión", Toast.LENGTH_SHORT).show()
            }
            else {
                if(isInMyFavorite){
                    MyApplication.removeFromFavorite(this, comicId)
                }
                else{
                    addToFavorite()
                }
            }
        }

        //dasd
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted: Boolean ->
        if(isGranted){
            Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
            downloadComic()
        }
        else {
            Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadComic(){

        Log.d(TAG, "downloadComic: Descargando Comic ")
        progressDialog.setMessage("Descargando Comic")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance() .getReferenceFromUrl(comicUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                Log.d(TAG, "downloadComic: Comic downloaded...")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadComic: Failed to download comic due to ${e.message}")
                Toast.makeText(this, "No se pudo descargar el comic debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: saving downloaded comic")

        val nameWhithExtention = "$comicTitle.pdf"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdir()

            val filePath = downloadsFolder.path +"/"+ nameWhithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Guardado en la carpeta de descargas", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Guardado en la carpeta de descargas")
            progressDialog.dismiss()
            incrementDownloadCount()

        }
        catch (e: Exception){
            Log.d(TAG, "saveToDownloadsFolder: failed to save due to ${e.message}")
            Toast.makeText(this, "No se pudo guardar debido a ${e.message} ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child(comicId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count $downloadsCount")

                    if(downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }

                    val newdownloadsCount: Long = downloadsCount.toLong()+1
                    Log.d(TAG, "onDataChange: New Download Count $newdownloadsCount")

                    val hasMap: HashMap<String, Any> = HashMap()
                    hasMap["downloadCount"] = newdownloadsCount

                    val dbRef = FirebaseDatabase.getInstance().getReference("Comics")
                    dbRef.child(comicId)
                        .updateChildren(hasMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count increment")
                        }
                        .addOnFailureListener {e ->
                            Log.d(TAG, "onDataChange: FAILED to increment due to ${e.message}")
                        }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun loadComicDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child(comicId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    comicTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    comicUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    var date = MyApplication.formatTimeStamp(timestamp.toLong())


                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage("$comicUrl", "$comicTitle", binding.pdfView, binding.progressBar, binding.pagesTv)

                    MyApplication.loadPdfSize("$comicUrl", "$comicTitle", binding.sizeTv)

                    binding.titleTv.text = comicTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadCount
                    binding.dateTv.text = date



                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: Checking if comic in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(comicId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite){
                        Log.d(TAG, "onDataChange: available in favorites")
                        binding.favoriteComicBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite, 0, 0)
                        binding.favoriteComicBtn.text = "Eliminar Favorito"
                    }
                    else {
                        Log.d(TAG, "onDataChange: not available in favorites")
                        binding.favoriteComicBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_blanco, 0, 0)
                        binding.favoriteComicBtn.text = "Añadir Favorito"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    private fun addToFavorite(){
        val timestamp = System.currentTimeMillis()

        val hasMap = HashMap<String, Any>()
        hasMap["comicId"] = comicId
        hasMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(comicId)
            .setValue(hasMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Added to fav")
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "addToFavorite: Failed to add fav due to ${e.message}")
                Toast.makeText(this, "No se pudo agregar a favoritos debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}