package chad.orionsoft.colorapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chad.orionsoft.colorapp.databinding.ActivityOpenColorsBinding
import org.json.JSONArray
import java.io.File

class OpenColors : AppCompatActivity() {

    private val cList = ArrayList<ColorObject1>()
    private lateinit var cAdapter:CAdapter
    private val binding: ActivityOpenColorsBinding by lazy {
        ActivityOpenColorsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
          /*  View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION */
        binding.colorRecycler.layoutManager = GridLayoutManager(applicationContext,2)
        makeColorList()
        binding.colorRecycler.recycledViewPool.setMaxRecycledViews(0,0)
        cAdapter = CAdapter(cList)
        binding.colorRecycler.adapter = cAdapter
    }

    private fun makeColorList() {
        val appDir =  applicationContext.getExternalFilesDir("saved")
        val files = appDir?.listFiles()
        if(!files.isNullOrEmpty()) {
            for (file in files) {
                val name = file.nameWithoutExtension
                val ext = file.extension
                if(ext == "jrgb") {
                    val buff = ByteArray(file.length().toInt())
                    val reader = file.inputStream()
                    val read = reader.read(buff)
                    val jStr = String(buff, 0, read)
                    val jArray = JSONArray(jStr)
                    val bgColor = jArray.get(0) as Int
                    val txColor = jArray.get(1) as Int
                    cList.add(ColorObject1(name, bgColor, txColor, file))
                }
            }
        }
    }

    inner class CAdapter(private val colors: ArrayList<ColorObject1>): RecyclerView.Adapter<CAdapter.CHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CHolder {
            return CHolder(layoutInflater.inflate(R.layout.one_color_layout, parent, false))
        }

        override fun getItemCount(): Int = colors.size

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: CHolder, position: Int) {
           val layout = holder.layout
            val nameText = holder.nameText
            val mainText = holder.mainText
            val shareImage = holder.shareImage
            val deleteImage = holder.deleteImage
            val openButton = holder.openButton
            layout.setBackgroundColor(colors[position].bgColor)
            nameText.text = colors[position].cName
            mainText.setTextColor(colors[position].textColor2)
            deleteImage.setOnClickListener {
                val builder = AlertDialog.Builder(this@OpenColors).apply {
                    setMessage(nameText.text.toString())
                    setPositiveButton("YES") { _, _ ->
                        if(colors[position].file.delete()) {
                            Toast.makeText(applicationContext, "${colors[position].cName} deleted", Toast.LENGTH_SHORT).show()
                            colors.remove(colors[position])
                            cAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(applicationContext, "failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                    setNegativeButton("NO") { _, _ ->

                    }
                }
                val dialog = builder.create()
                dialog.setTitle("Sure to delete this?")
                dialog.show()
            }
            shareImage.setOnClickListener {
                val builder = AlertDialog.Builder(this@OpenColors).apply {
                    setMessage("Please select one option")
                    setNeutralButton("Share as Image") { _, _ ->
                        val i = Intent(applicationContext, ShareActivity::class.java)
                        i.putExtra("bg", colors[position].bgColor)
                        i.putExtra("text", colors[position].textColor2)
                        startActivity(i)
                    }
                    setPositiveButton("Share Details") { _, _ ->
                        val bgColor = colors[position].bgColor
                        val textColor = colors[position].textColor2
                        val bgRed = Color.red(bgColor)
                        val bgGreen = Color.green(bgColor)
                        val bgBlue = Color.blue(bgColor)
                        val bgHex = MainActivity.convertToHex(bgRed, bgGreen, bgBlue)
                        val info1 = "Background color = $bgHex\n Red = $bgRed, Green = $bgGreen, Blue = $bgBlue"
                        val tRed = Color.red(textColor)
                        val tGreen = Color.green(textColor)
                        val tBlue = Color.blue(textColor)
                        val tHex = MainActivity.convertToHex(tRed, tGreen, tRed)
                        val info2 = "text color = $tHex\n Red = $tRed, Green = $tGreen, Blue = $tBlue"
                        val shareIntent = Intent().apply {
                            type = "text/plain"
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "$info1\n\n$info2")
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share with: "))
                    }
                }
                val dialog = builder.create()
                dialog.setTitle("Sharing Colors...")
                dialog.show()
            }
            openButton.setOnClickListener {
                val i = Intent(applicationContext, MainActivity::class.java)
                i.putExtra("name", colors[position].cName)
                i.putExtra("bg", colors[position].bgColor)
                i.putExtra("text", colors[position].textColor2)
                startActivity(i)
            }
            openButton.setOnLongClickListener {
                Toast.makeText(applicationContext, "Created by Chandersh204", Toast.LENGTH_LONG).show()
                true
            }
        }

        inner class CHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val layout: ConstraintLayout = itemView.findViewById(R.id.savedColorLayout)
            val nameText: TextView = itemView.findViewById(R.id.savedColorName)
            val mainText: TextView = itemView.findViewById(R.id.textPreview)
            val shareImage :ImageView = itemView.findViewById(R.id.colorShareButton)
            val deleteImage: ImageView = itemView.findViewById(R.id.colorRemoveButton)
            val openButton: Button = itemView.findViewById(R.id.openButton)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        cList.clear()
        makeColorList()
        cAdapter.notifyDataSetChanged()
        super.onResume()
    }

    class ColorObject1(val cName:String, val bgColor:Int, val textColor2:Int, val file: File)
}
