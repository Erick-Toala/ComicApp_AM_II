package com.etoala.comicapp

import android.app.Application
import android.app.ProgressDialog
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale
import java.util.*
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.HashMap

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }


    companion object {

        fun formatTimeStamp(timestamp:Long):String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis =timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize (pdfUrl:String, pdfTitle: String, sizeTv: TextView){
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetaData ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb>=1){
                        sizeTv.text= "${String.format("%.2f", mb)}MB"
                    }
                    else if (kb>=1){
                        sizeTv.text= "${String.format("%.2f", kb)}KB"
                    }
                    else {
                        sizeTv.text= "${String.format("%.2f", bytes)}bytes"
                    }
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metada due to ${e.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ){
            val TAG = "PDF_THUMBNAIL_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes ->
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                            progressBar.visibility= View.INVISIBLE
                            if(pagesTv != null){
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView){
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        fun deleteComic(context: Context, comicId: String, comicUrl: String, comicTitle: String){
            val TAG = "DELETE_COMIC_TAG"

            Log.d(TAG, "deleteComic: deleting...")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Espere Por favor")
            progressDialog.setMessage("Eliminando $comicTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteComic: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(comicUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteComic: Deleted from storage")
                    Log.d(TAG, "deleteComic: Deleting from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Comics")
                    ref.child(comicId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Eliminado exitosamente", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteComic: Deleted from db too...")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "deleteComic: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "deleteComic: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context, "Failed to delete from storage due to ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementComicViewCount(comicId: String){
            val ref = FirebaseDatabase.getInstance().getReference("Comics")
            ref.child(comicId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = "${snapshot.child("viewsCount").value}"
                        if(viewsCount=="" || viewsCount=="null"){
                            viewsCount="0"
                        }
                        val newViewsCount = viewsCount.toLong() + 1

                        val hasMap = HashMap<String, Any>()
                        hasMap["viewsCount"] = newViewsCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Comics")
                        dbRef.child(comicId)
                            .updateChildren(hasMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }


        public fun removeFromFavorite(context: Context, comicId: String){
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFromFavorite: Removing from fav")

            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(comicId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: Removed from fav")
                    Toast.makeText(context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "removeFromFavorite:  Failed to remove fav due to ${e.message}")
                    Toast.makeText(context, "No se pudo eliminar de favoritos debido a ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }

}