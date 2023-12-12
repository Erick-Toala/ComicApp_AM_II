package com.etoala.comicapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.etoala.comicapp.databinding.FilaComicFavoritoBinding
import com.etoala.comicapp.databinding.FilaComicUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite: RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite > {

    private var context: Context

    private var comicsArrayList: ArrayList<ModelPdf>

    private lateinit var binding: FilaComicFavoritoBinding

    private var filter: FilterPdfUser? = null

    constructor(context: Context, comicsArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.comicsArrayList = comicsArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = FilaComicFavoritoBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        val model = comicsArrayList[position]

        loadComicDetails(model, holder)

        holder.itemView.setOnClickListener {
            val intent= Intent(context, DetallesComicActivity::class.java)
            intent.putExtra("comicId",model.id)
            context.startActivity(intent)
        }

        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id )
        }

    }

    private fun loadComicDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val comicId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.child(comicId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadCount = downloadCount.toLong()


                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory("$categoryId", holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage("$url","$title", holder.pdfView, holder.progressBar, null)
                    MyApplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.description.text = description
                    holder.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return comicsArrayList.size
    }

    inner class HolderPdfFavorite(itemView: View): RecyclerView.ViewHolder(itemView){

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val removeFavBtn = binding.removeFavBtn
        val description = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }


}