package com.etoala.comicapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.etoala.comicapp.databinding.FragmentComicUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ComicUserFragment : Fragment {

    private lateinit var binding: FragmentComicUserBinding

    companion object {
        private const val TAG = "COMICS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid:String):ComicUserFragment{
            val fragment = ComicUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentComicUserBinding.inflate(LayoutInflater.from(context), container, false)

        Log.d(TAG, "onCreateView: Category: $category")
        if(category == "Todo"){
            loadAllComics()
        }
        else if(category == "Más vistos"){
            loadMostViewedDownloadedComic("viewsCount")
        }
        else if(category == "Más descargado"){
            loadMostViewedDownloadedComic("downloadCount")
        }
        else {
            loadCategorizeComics()
        }

        binding.searchEt.addTextChangedListener { object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }
        }}
        return binding.root
    }

    private fun loadAllComics() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)

                    pdfArrayList.add(model!!)
                }
                adapterPdfUser= AdapterPdfUser(context!!, pdfArrayList)

                binding.comicRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadMostViewedDownloadedComic(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)

                    pdfArrayList.add(model!!)
                }
                adapterPdfUser=AdapterPdfUser(context!!, pdfArrayList)

                binding.comicRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun loadCategorizeComics() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Comics")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)

                        pdfArrayList.add(model!!)
                    }
                    adapterPdfUser=AdapterPdfUser(context!!, pdfArrayList)

                    binding.comicRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }




}