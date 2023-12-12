package com.etoala.comicapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.etoala.comicapp.databinding.ActivityVerComicBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class VerComicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerComicBinding

    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    var comicId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerComicBinding.inflate(layoutInflater)
        setContentView(binding.root)


        comicId = intent.getStringExtra("comicId")!!
        loadComicDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadComicDetails() {
        Log.d(TAG, "loadComicDetails: Get Pdf URL from db")

        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child(comicId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")
                    loadComicFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun loadComicFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadComicFromUrl: Get Pdf from firebase storage using URL")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadComicFromUrl: pdf got from url")

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(true)
                    .onPageChange { page, pageCount ->
                        val currentPage = page+1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadComicFromUrl: $currentPage/$pageCount")
                    }
                    .onError {t ->
                        Log.d(TAG,"loadComicFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadComicFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "loadComicFromUrl: Failed to get pdf due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }

}