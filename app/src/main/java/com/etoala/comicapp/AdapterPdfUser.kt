package com.etoala.comicapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.etoala.comicapp.databinding.FilaComicAdminBinding
import com.etoala.comicapp.databinding.FilaComicUserBinding

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable{

    private var context: Context

    public var pdfArrayList: ArrayList<ModelPdf>

    private val filterList: ArrayList<ModelPdf>

    private lateinit var binding: FilaComicUserBinding

    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = FilaComicUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfArrayList[position]
        val comicId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        holder.titleTv.text = title
        holder.description.text = description
        holder.dateTv.text = formattedDate

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        MyApplication.loadPdfSize(url, title, holder.sizeTv)


        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetallesComicActivity::class.java)
            intent.putExtra("comicId", comicId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val description = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }




}