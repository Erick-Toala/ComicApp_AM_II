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

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{

    private var context: Context

    public var pdfArrayList: ArrayList<ModelPdf>

    private val filterList: ArrayList<ModelPdf>

    private lateinit var binding: FilaComicAdminBinding

    private var filter: FilterPdfAdmin? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = FilaComicAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        holder.titleTv.text = title
        holder.description.text = description
        holder.dateTv.text = formattedDate

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetallesComicActivity::class.java)
            intent.putExtra("comicId", pdfId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
        val comicId = model.id
        val comicUrl = model.url
        val comicTitle = model.title

        val options = arrayOf("Editar", "Eliminar")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Elige una opción")
            .setItems(options){dialog, position ->
                if (position==0){
                    val intent = Intent(context, EditarComicActivity::class.java)
                    intent.putExtra("comicId", comicId)
                    context.startActivity(intent)
                }
                else if (position == 1){
                    MyApplication.deleteComic(context, comicId, comicUrl, comicTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }


    inner class HolderPdfAdmin(itemView: View):RecyclerView.ViewHolder(itemView){

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val description = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }
}