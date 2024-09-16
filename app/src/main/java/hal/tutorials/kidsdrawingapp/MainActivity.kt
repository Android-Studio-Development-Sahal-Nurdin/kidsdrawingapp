package hal.tutorials.kidsdrawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null

//    10 akses warna
    private var mImageButtonCurrentPaint: ImageButton? = null

//    17
    var customProgressDialog: Dialog?= null

//    13
    val openGalleryLAUNCHER: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data!=null){ // untuk memilih gambar yang dinginkan dan override dari activity_main.xml
            val imageBackground : ImageView = findViewById(R.id.iv_background)

            imageBackground.setImageURI(result.data?.data) // URI adalah path location yang ada pada local storage
        }
}

//    12.
    val requestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
    permissions.entries.forEach{
        val permissionName = it.key
        val isGranted = it.value
        if(isGranted){
            Toast.makeText(this, "Permission granted now you can read the storage files.", Toast.LENGTH_LONG).show()
//            13
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            openGalleryLAUNCHER.launch(pickIntent)
        } else {
            if(permissionName == Manifest.permission.READ_MEDIA_IMAGES){
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show()
            }
        }
    }

}


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())

//        10. akses tiap color
        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed) // ketika press maka bordernya berubah
        )

//        untuk button pada MainActivity
        val ib_brush :  ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
        }

//        14. UNTUK TOMBOL UNDO
        val ibUndo :  ImageButton = findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener{
            drawingView?.onClickUndo()
        }

//        16. UNTUK SAVE
        val ibSave :  ImageButton = findViewById(R.id.ib_save)
        ibSave.setOnClickListener{
            if(isReadStorageAllowed()){
//                17
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitMapFromView(flDrawingView))

                }
            }
        }

//        12.
        val ibGallery: ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener{
            requestStoragePermission()
        }
    }

    //        9. untuk mengatur ukuran dari brush
    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")

        val smallBtn : ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn : ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn : ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

//    11. ini akan menambahkan method di main agar color muncul
    fun paintClicked(view: View){
//        Toast.makeText(this, "clicked paint", Toast.LENGTH_LONG).show()
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString() // ini akan manggil si tag di activty_main.xml
            drawingView?.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed) // ketika press maka bordernya berubah
            )
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal) // ketika press maka bordernya berubah
            )
            mImageButtonCurrentPaint = view // hold dari warna yang sedang dipakai
        }
    }
//    16 fungsi di bawah untuk latest version android yang tidak butuh permission jadi kita punya fitur untuk mengantisipasinya
    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        return result == PackageManager.PERMISSION_GRANTED
    }

//    12. ADD ALERT DIALOG:
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
            showRelationalDialog("Kids Drawing App", "Kids Drawing App " + "needs to Access Your External Storage")
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES,
//                16
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }

    }

    
    private fun showRelationalDialog(title: String, message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setTitle(title).setMessage(message).setPositiveButton("Cancel"){
        dialog, _ -> dialog.dismiss()
    }
    builder.create().show()
    }

//    15
    private fun getBitMapFromView(view: View): Bitmap{
        val returnedBitMap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnedBitMap)
    val bgDrawable = view.background
    if(bgDrawable != null){
        bgDrawable.draw(canvas)
    }else{
        canvas.drawColor(Color.WHITE)
    }
    view.draw(canvas)
    return returnedBitMap
    }

//    16
    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String{
        var result = ""
        withContext(Dispatchers.IO){
            if(mBitmap != null) {
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

//                    file location
                    val f = File(externalCacheDir?.absoluteFile.toString()+ File.separator + "KidDrawingApp_" + System.currentTimeMillis()/1000 + ".png")
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread{
                        cancelProgressDialog()
                        if (result.isNotEmpty()){
                            Toast.makeText(this@MainActivity, "File saved successfully: $result", Toast.LENGTH_LONG).show()
//                            18
                            setUpEnablingFeatures(FileProvider.getUriForFile(baseContext,"hal.tutorials.kidsdrawingapp.fileprovider",f))
                        } else {
                            Toast.makeText(this@MainActivity, "Something went wrong while saving the file.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception){
                    result =""
                    e.printStackTrace()
                }
            }
        }
    return result
    }

//    17
    private fun showProgressDialog(){
        customProgressDialog = Dialog(this@MainActivity)
    customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
    customProgressDialog?.show()
    }

    private fun cancelProgressDialog(){
        if(customProgressDialog != null){
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    // 18
    private fun setUpEnablingFeatures(uri: Uri){
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "image/jpeg"
        startActivity(Intent.createChooser(intent, "Share image via "))
    }


}