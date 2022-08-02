package com.developer.soutos16lockscreen.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.developer.soutos16lockscreen.R
import com.developer.soutos16lockscreen.model.StoreModel
import com.developer.soutos16lockscreen.receiver.StoreRecycler
import com.sout.applewatch.applewatchsystemmanager.Api.API
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_store.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class StoreActivity : AppCompatActivity()  ,StoreRecycler.Listener{

    private var BASE_URL = "https://raw.githubusercontent.com/"
    lateinit var compositeDisposable : CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()
        setContentView(R.layout.activity_store)

        compositeDisposable = CompositeDisposable()
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (isNetworkAvailbale()){
            getData()
        }


    };


    fun  isNetworkAvailbale():Boolean{
        val conManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val internetInfo =conManager.activeNetworkInfo
        return internetInfo!=null && internetInfo.isConnected
    }
    fun getData(){
        var retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build().create(API::class.java)

        compositeDisposable.add(retrofit.getData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::DataHandler)
        )
    }

    fun DataHandler(storeModel: List<StoreModel>){
        storeModel?.let {
            recyclerView.adapter = StoreRecycler(it as ArrayList<StoreModel>,this@StoreActivity)
        }
    }

    override fun onItemClickListener(storeModel: StoreModel) {
        var url = storeModel.link
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    fun transparentStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this,MainActivity::class.java)
        startActivity(i)
        finish()
    }
}