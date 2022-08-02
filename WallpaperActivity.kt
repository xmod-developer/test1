package com.developer.soutos16lockscreen.activity

import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.developer.soutos16lockscreen.R
import com.developer.soutos16lockscreen.adapter.WallAdapter
import com.developer.soutos16lockscreen.api.APIWall
import com.developer.soutos16lockscreen.model.netModel.NetWallModel
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_wallpaper.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL


class WallpaperActivity : AppCompatActivity() , WallAdapter.ListenerWall{


    private val BASE_URL = "https://raw.githubusercontent.com/"
    private lateinit var composite : CompositeDisposable
    companion object{
        var myBitmap : Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()
        setContentView(R.layout.activity_wallpaper)

        //// init
        composite = CompositeDisposable();

        ////// ADS Setting.....
        UnityAds.initialize(
            this@WallpaperActivity, MainActivity.GameID, MainActivity.TestMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                 //   Toast.makeText(this@WallpaperActivity, "SDK Working", Toast.LENGTH_SHORT).show()
                }
                override fun onInitializationFailed(
                    unityAdsInitializationError: UnityAds.UnityAdsInitializationError,
                    s: String
                ) {
                //    Toast.makeText(this@WallpaperActivity, "SDK  Not Working", Toast.LENGTH_SHORT).show()
                }
            })

        val iUnityAdsShowListener: IUnityAdsShowListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                s: String,
                unityAdsShowError: UnityAds.UnityAdsShowError,
                s1: String
            ) {
                UnityAds.load(MainActivity.ADID) // Add Placement ID
                UnityAds.show(this@WallpaperActivity, MainActivity.ADID) // Context and Add Placement ID
            }

            override fun onUnityAdsShowStart(s: String) {}
            override fun onUnityAdsShowClick(s: String) {}
            override fun onUnityAdsShowComplete(
                s: String,
                unityAdsShowCompletionState: UnityAds.UnityAdsShowCompletionState
            ) {
            }
        }
        loadInterstitial() // Add Placement ID

        if (MainActivity.adCounter %5 == 0){
            UnityAds.show(this@WallpaperActivity, MainActivity.ADID)
            MainActivity.adCounter = MainActivity.adCounter + 1;
            MainActivity.adShared.edit().putInt("ad", MainActivity.adCounter).commit()
        }else{
            MainActivity.adCounter = MainActivity.adCounter + 1;
            MainActivity.adShared.edit().putInt("ad", MainActivity.adCounter).commit()
        }
////////////////////////////////////////////////////////////////////////////////////////////////////


        wall_reccycler.layoutManager = GridLayoutManager(this,2)
        if (isNetworkAvailbale()){
            getData()
        }
        
    };



    private fun transparentStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    private fun loadInterstitial() {
        if (MainActivity.isShowUnityAd){
            if (UnityAds.isInitialized()) {
                UnityAds.load(MainActivity.ADID)
            } else {
                Handler().postDelayed({
                    UnityAds.load(MainActivity.ADID) }, 5000)
            }
        }
    }

    override fun onBackPressed() {
        var i = Intent(this,MainActivity::class.java)
        startActivity(i)
        finish()
    }

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
            .build().create(APIWall::class.java)

        composite.add(retrofit.getData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::DataHandler)
        )
    }

    fun DataHandler(storeModel: List<NetWallModel>){
        storeModel?.let {
            wall_reccycler.adapter = WallAdapter(it as ArrayList<NetWallModel>,this@WallpaperActivity)
        }
    }

    override fun onWallClick(wallModel : NetWallModel) {
        var alert = AlertDialog.Builder(this)
            .setMessage("Wallpaper Set")
            .setPositiveButton("Done",DialogInterface.OnClickListener { dialog, which ->
                //Toast.makeText(this, "${wallModel.wall}", Toast.LENGTH_SHORT).show()


               // alertLoading.visibility = View.VISIBLE
                val dialog = ProgressDialog.show(
                    this, "",
                    "Loading. Please wait...", true
                )
                LoadImage(this).execute(wallModel.Picture)

              //  val d : Drawable = BitmapDrawable(resources, image)
               // MainActivity.wallpaperShared.edit().putString("wallpaper",encoded).commit()

            })
            .setNegativeButton("Cancel",DialogInterface.OnClickListener { dialog, which ->

            })
        alert.show()
    }



    class LoadImage(var context : WallpaperActivity) : AsyncTask<String?, Void?, Bitmap?>() {


        override fun onPostExecute(bitmap: Bitmap?) {
          //  imageView.setImageBitmap(bitmap)
            myBitmap = bitmap
            /// LOCK SCREEN WALLPAPER SET
            val wpManager = WallpaperManager.getInstance(context)
            wpManager.setBitmap(myBitmap, null, true, WallpaperManager.FLAG_LOCK)

            MainActivity.wallpaperShared.edit().putString("wallpaper",bitmap?.let { encodeTobase64(it) }).commit()
            var i = Intent(context,MainActivity::class.java)
            context.startActivity(i)
            context.finish()
        }

        fun encodeTobase64(image: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b: ByteArray = baos.toByteArray()
            val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
            return imageEncoded
        }

        override fun doInBackground(vararg params: String?): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val inputStream: InputStream = URL(params[0]).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitmap
        }
    }

};