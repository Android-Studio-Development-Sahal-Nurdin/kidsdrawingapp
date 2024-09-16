package hal.tutorials.kidsdrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

// 1. Kelas ini digunakan sebagai tampilan kustom (view).
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // 2. Deklarasi variabel
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
//    7. buat arraylist yang immutable baru
    private val mPaths = ArrayList<CustomPath>()

//    14. add undo button
    private val mUndoPaths = ArrayList<CustomPath>()

    // 5. Konstruktor di dalam blok init digunakan untuk inisialisasi objek.
    init {
        setupDrawing()
    }

//    14. Undo
    fun onClickUndo(){
        if(mPaths.size > 0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }

    // 4. Metode ini digunakan untuk mengatur parameter gambar awal.
    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color // Set warna gambar awal menjadi hitam.
        mDrawPaint!!.style = Paint.Style.STROKE // Mengatur gaya lukisan menjadi garis.
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // Mengatur bentuk sambungan antar garis menjadi bulat.
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // Mengatur bentuk ujung garis menjadi bulat.
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mBrushSize = 20.toFloat() // Mengatur ukuran kuas awal menjadi 20.

    }

    // 6. Metode ini dipanggil ketika ukuran tampilan berubah.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Membuat bitmap baru dengan ukuran yang sesuai dengan tampilan.
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        // Membuat objek canvas yang terkait dengan bitmap.
        canvas = Canvas(mCanvasBitmap!!)
    }

    // Metode ini dipanggil ketika tampilan perlu digambar.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Menggambar bitmap (gambar yang telah digambar sebelumnya) pada tampilan.
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for(path in mPaths){
            // Mengatur ketebalan garis (ukuran kuas) yang akan digunakan.
            mDrawPaint!!.strokeWidth = path.brushThickness
            // Mengatur warna garis yang akan digunakan.
            mDrawPaint!!.color = path.color
            // Menggambar path (jejak) yang sedang digambar ke dalam canvas.
            canvas.drawPath(path, mDrawPaint!!)
        }


        if (!mDrawPath!!.isEmpty) {
            // Mengatur ketebalan garis (ukuran kuas) yang akan digunakan.
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            // Mengatur warna garis yang akan digunakan.
            mDrawPaint!!.color = mDrawPath!!.color
            // Menggambar path (jejak) yang sedang digambar ke dalam canvas.
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    // Metode ini dipanggil saat ada interaksi sentuhan pada tampilan.
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> { // Ketika tombol mouse/touch ditekan
                // Mengatur warna dan ketebalan garis yang akan digambar.
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                // Mengatur ulang path (jejak) yang sedang digambar.
                mDrawPath!!.reset()
                // Memindahkan path ke titik awal sentuhan.
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> { // Ketika tombol mouse/touch digeser
                // Menambahkan garis ke path saat sentuhan digeser.
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> { // Ketika tombol mouse/touch dilepas
               mPaths.add(mDrawPath!!)
                // Membuat path baru untuk menggambar jejak selanjutnya.
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        // Meminta sistem untuk menggambar kembali tampilan.
        invalidate()
        return true
    }

//    8
    fun setSizeForBrush(newSize: Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

//   11.  ketika button pallet diclick maka warna brush akan berubah
    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }


    // 3. Kelas CustomPath digunakan untuk menyimpan path (jejak) gambar.
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {
        // Dapat ditambahkan metode atau properti khusus di sini jika diperlukan.


    }
}
