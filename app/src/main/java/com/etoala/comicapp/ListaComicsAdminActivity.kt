package com.etoala.comicapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import com.etoala.comicapp.databinding.ActivityListaComicsAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListaComicsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaComicsAdminBinding

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    private var categoryId = ""
    private var category = ""

    private lateinit var pdfArrayAdmin: ArrayList<ModelPdf>

    private lateinit var  adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaComicsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.subTittleTv.text = category

        loadPdfList()

        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfAdmin.filter!!.filter(s)
                }catch (e: Exception){
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPdfList() {
        pdfArrayAdmin = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayAdmin.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)
                        if (model!=null){
                            pdfArrayAdmin.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    adapterPdfAdmin = AdapterPdfAdmin(this@ListaComicsAdminActivity, pdfArrayAdmin)
                    binding.comicsRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}