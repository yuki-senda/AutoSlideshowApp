package jp.techacademy.yuki.senda.autoslideshowapp

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import jp.techacademy.yuki.senda.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS_REQUEST_CODE = 100
    private lateinit var resolver: ContentResolver
    private lateinit var cursor: Cursor

    private var mTimer: Timer? = null

    private var mTimerSec = 0.0
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        //進むボタンを押した時の処理
        binding.btForward.setOnClickListener {
            if (cursor.moveToNext() == false) {
                cursor.moveToFirst()
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                binding.slideImage.setImageURI(imageUri)
            } else {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                binding.slideImage.setImageURI(imageUri)
            }
        }

        //戻るボタンを押した時の処理
        binding.btBack.setOnClickListener {
            if (cursor.moveToPrevious() == false) {
                cursor.moveToLast()
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                binding.slideImage.setImageURI(imageUri)
            } else {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                binding.slideImage.setImageURI(imageUri)
            }
        }

        //再生・停止ボタンを押した時の処理
        binding.btStartStop.setOnCheckedChangeListener { buttonView, isChecked ->
            //再生状態のとき
            if (isChecked) {
                //進むボタンと戻るボタンを無効化
                binding.btForward.isEnabled = false
                binding.btBack.isEnabled = false

                //タイマー機能
                if(mTimer == null){
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask(){
                        override fun run(){
                            mTimerSec += 0.1
                            //ワーカースレッドでの処理
                            mHandler.post{
                                if (cursor.moveToNext() == false) {
                                    cursor.moveToFirst()
                                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                    val id = cursor.getLong(fieldIndex)
                                    val imageUri =
                                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                                    binding.slideImage.setImageURI(imageUri)
                                } else {
                                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                    val id = cursor.getLong(fieldIndex)
                                    val imageUri =
                                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                                    binding.slideImage.setImageURI(imageUri)
                                }
                            }
                        }
                    },2000,2000)
                }

            //停止状態の時
            } else {
                //進むボタンと戻るボタンを有効化
                binding.btForward.isEnabled = true
                binding.btBack.isEnabled = true

                //タイマーを破棄し、初期化
                if(mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        }
    }

    //ユーザの選択結果の判別
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )!!

        if (cursor.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            binding.slideImage.setImageURI(imageUri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cursor != null) {
            cursor.close()
        }
    }
}