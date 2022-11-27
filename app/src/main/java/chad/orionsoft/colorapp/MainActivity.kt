package chad.orionsoft.colorapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import chad.orionsoft.colorapp.databinding.ActivityMainBinding
import chad.orionsoft.colorapp.databinding.ContentMainBinding
import org.json.JSONArray
import java.io.File
import java.util.*

private const val BACKGROUND: Int = 0
private const val TEXT = 1
private const val BGPREF = "bg"
private const val TPREF = "txt"

class MainActivity : AppCompatActivity() {

    var backgroundColor = 0
    var textColor1 = 0
    private var whiteText = false
    private lateinit var cStack:ColorStack
    private lateinit var prefs:SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private var secondInstance = false
    private var oldName = ""
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val contentBinding : ContentMainBinding = binding.contentMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefEditor = prefs.edit()

        if(intent.hasExtra("bg") && intent.hasExtra("text")) {
            backgroundColor = intent.getIntExtra("bg", 0)
            textColor1 = intent.getIntExtra("text", 0)
            oldName = intent.getStringExtra("name") as String
            secondInstance = true
        } else {
            backgroundColor = prefs.getInt(BGPREF, Color.rgb(200,200,200))
            textColor1 = prefs.getInt(TPREF, Color.rgb(0,0,0))
        }

        setBackgroundColor(Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor))
        setTextColor(Color.red(textColor1), Color.green(textColor1), Color.blue(textColor1))
        cStack = ColorStack()
        cStack.mainState = ColorObject(backgroundColor, textColor1)

        contentBinding.seekBGRed.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            var g:Int = 0
            var b:Int = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setBackgroundColor(progress, g, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                g = Color.green(backgroundColor)
                b = Color.blue(backgroundColor)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                cStack.addNewState(ColorObject(backgroundColor, textColor1))
            }

        })

        contentBinding.seekBGGreen.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            var r:Int = 0
            var b:Int = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setBackgroundColor(r, progress, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                r = Color.red(backgroundColor)
                b = Color.blue(backgroundColor)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                cStack.addNewState(ColorObject(backgroundColor, textColor1))
            }

        })

        contentBinding.seekBGBlue.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            var r:Int = 0
            var g:Int = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setBackgroundColor(r, g, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                r = Color.red(backgroundColor)
                g = Color.green(backgroundColor)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                cStack.addNewState(ColorObject(backgroundColor, textColor1))
            }

        })

        contentBinding.seekTRed.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener {
            var g = 0
            var b = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setTextColor(progress, g, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                g = Color.green(textColor1)
                b = Color.blue(textColor1)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                cStack.addNewState(ColorObject(backgroundColor, textColor1))
            }

        })

        contentBinding.seekTGreen.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener {
            var r = 0
            var b = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setTextColor(r, progress, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                r = Color.red(textColor1)
                b = Color.blue(textColor1)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                cStack.addNewState(ColorObject(backgroundColor, textColor1))
            }

        })

        contentBinding.seekTBlue.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener {
            var r = 0
            var g = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setTextColor(r, g, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                r = Color.red(textColor1)
                g = Color.green(textColor1)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                cStack.addNewState(ColorObject(backgroundColor, textColor1))
            }

        })

        contentBinding.seekBright.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener {

            private var curR = 0
            private var curG = 0
            private var curB = 0
            private var newBGColor = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val change = 50 - progress
                    val r1 = curR - change
                    val g1 = curG - change
                    val b1 = curB - change
                    newBGColor = updateBrightnessColors(r1, g1, b1)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                contentBinding.brightnessButtons.visibility = View.VISIBLE
                toggleColorsInfo(false)
                val bg = cStack.mainState.bgColor
                curR = Color.red(bg)
                curG = Color.green(bg)
                curB = Color.blue(bg)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                contentBinding.brightDoneButton.setOnClickListener {
                    contentBinding.seekBright.progress = 50
                    val r1 = Color.red(newBGColor)
                    val g1 = Color.green(newBGColor)
                    val b1 = Color.blue(newBGColor)
                    setBackgroundColor(r1, g1, b1)
                    cStack.addNewState(ColorObject(backgroundColor, textColor1))
                    contentBinding.brightnessButtons.visibility = View.GONE
                    toggleColorsInfo(true)
                }
                contentBinding.brightCloseButton.setOnClickListener {
                    contentBinding.seekBright.progress = 50
                    val r1 = Color.red(backgroundColor)
                    val g1 = Color.green(backgroundColor)
                    val b1 = Color.blue(backgroundColor)
                    setBackgroundColor(r1, g1, b1)
                    contentBinding.brightnessButtons.visibility = View.GONE
                    toggleColorsInfo(true)
                }
            }

        })

        contentBinding.BGHexText.setOnClickListener {
            showHexDialog(BACKGROUND, contentBinding.BGHexText.text.toString())
        }
        contentBinding.TEXTHexText.setOnClickListener {
            showHexDialog(TEXT, contentBinding.TEXTHexText.text.toString())
        }
        contentBinding.switchTextColor.setOnClickListener {
            val color = if (whiteText) {
                whiteText = false
                Color.BLACK
            } else {
                whiteText = true
                Color.WHITE
            }
            contentBinding.textView5.setTextColor(color)
            contentBinding.textView6.setTextColor(color)
            contentBinding.textView7.setTextColor(color)
            contentBinding.BGHexText.setTextColor(color)
            contentBinding.TEXTHexText.setTextColor(color)
        }
    }

    fun toggle(v: View) {
        when(v.id) {
            R.id.BGButton -> {
                contentBinding.seekBarsBackground.visibility = View.VISIBLE
                contentBinding.seekBarsText.visibility = View.GONE
                contentBinding.BGButton.setBackgroundColor(Color.YELLOW)
                contentBinding.TextButton.setBackgroundColor(Color.LTGRAY)
            }
            R.id.TextButton -> {
                contentBinding.seekBarsBackground.visibility = View.GONE
                contentBinding.seekBarsText.visibility = View.VISIBLE
                contentBinding.BGButton.setBackgroundColor(Color.LTGRAY)
                contentBinding.TextButton.setBackgroundColor(Color.YELLOW)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.color_menu, menu)
        if(secondInstance) {
            menu?.removeItem(R.id.menu_open)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_undo -> {
                val cl = cStack.undo()
                val bg = cl.bgColor
                val r1 = Color.red(bg)
                val g1 = Color.green(bg)
                val b1 = Color.blue(bg)
                setBackgroundColor(r1, g1, b1)
                val tx = cl.textColor2
                val r2 = Color.red(tx)
                val g2 = Color.green(tx)
                val b2 = Color.blue(tx)
                setTextColor(r2,g2,b2)
            }
            R.id.menu_redo -> {
                val cl = cStack.redo()
                val bg = cl.bgColor
                val r1 = Color.red(bg)
                val g1 = Color.green(bg)
                val b1 = Color.blue(bg)
                setBackgroundColor(r1, g1, b1)
                val tx = cl.textColor2
                val r2 = Color.red(tx)
                val g2 = Color.green(tx)
                val b2 = Color.blue(tx)
                setTextColor(r2,g2,b2)
            }
            R.id.menu_share -> {
                val builder = AlertDialog.Builder(this@MainActivity).apply {
                    setMessage("Please select one option")
                    setNeutralButton("Share as Image") { _, _ ->
                        val i = Intent(applicationContext, ShareActivity::class.java)
                        i.putExtra("bg", backgroundColor)
                        i.putExtra("text", textColor1)
                        startActivity(i)
                    }
                    setPositiveButton("Share Details") { _, _ ->
                        val bgColor = backgroundColor
                        val textColor = textColor1
                        val bgRed = Color.red(bgColor)
                        val bgGreen = Color.green(bgColor)
                        val bgBlue = Color.blue(bgColor)
                        val bgHex = convertToHex(bgRed, bgGreen, bgBlue)
                        val info1 = "Background color = $bgHex\n Red = $bgRed, Green = $bgGreen, Blue = $bgBlue"
                        val tRed = Color.red(textColor)
                        val tGreen = Color.green(textColor)
                        val tBlue = Color.blue(textColor)
                        val tHex = convertToHex(tRed, tGreen, tRed)
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
            R.id.menu_save -> {
                var done = false
                val builder = AlertDialog.Builder(this@MainActivity).apply {
                    setMessage("Please give it a name...")
                    val view = layoutInflater.inflate(R.layout.hex_dialog_layout, LinearLayout(applicationContext), false)
                    val text: TextView = view.findViewById(R.id.dialogText)
                    text.text = ""
                    val editText: EditText = view.findViewById(R.id.hexEditText)
                    setView(view)
                    editText.setText(oldName)
                    setPositiveButton("OK") { _, _  ->
                        if (editText.text.isNullOrEmpty()) {
                            editText.error = "Please provide a name"
                            Toast.makeText(applicationContext, "Please enter a name", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val name =editText.text.toString()
                        val appDir =  applicationContext.getExternalFilesDir("saved")
                        val file = File(appDir, "$name.jrgb")
                        if (!secondInstance) {
                            // check if file already exists
                            val files = appDir?.listFiles()
                            if (files!!.isNotEmpty() && files.contains(file)) {
                                editText.error = "This name already exists"
                                Toast.makeText(applicationContext, "Color with this name already exists, please " +
                                        "type a different name", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                        }
                        val jsonArray = JSONArray()
                        jsonArray.put(0, backgroundColor)
                        jsonArray.put(1, textColor1)
                        val writer = file.outputStream()
                        writer.write(jsonArray.toString().toByteArray())
                        writer.flush()
                        writer.close()
                        Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
                        done = true
                        if (secondInstance) {
                            finish()
                        }
                    }
                    setNegativeButton("CLOSE") { _, _ ->
                        done = true
                    }
                }
                val dialog = builder.create()
                dialog.setTitle("Save this colors?")
                dialog.setOnDismissListener {
                    if (!done) {
                        dialog.show()
                    }
                }
                dialog.show()
            }
            R.id.menu_open -> {
                startActivity(Intent(applicationContext, OpenColors::class.java))
            }
        }
        return true
    }

    private fun setBackgroundColor(r1:Int, g1:Int, b1:Int) {
        backgroundColor = Color.rgb(r1, g1, b1)
        contentBinding.mainBackground.setBackgroundColor(backgroundColor)
        contentBinding.seekBGRed.progress = r1
        contentBinding.seekBGGreen.progress = g1
        contentBinding.seekBGBlue.progress = b1
        contentBinding.BGRedText.text = r1.toString()
        contentBinding.BGGreenText.text = g1.toString()
        contentBinding.BGBlueText.text = b1.toString()
        contentBinding.BGHexText.text = convertToHex(r1,g1,b1)
    }

    private fun setTextColor(r1:Int, g1:Int, b1:Int) {
        textColor1 = Color.rgb(r1,g1,b1)
        contentBinding.mainText.setTextColor(textColor1)
        contentBinding.seekTRed.progress = r1
        contentBinding.seekTGreen.progress = g1
        contentBinding.seekTBlue.progress = b1
        contentBinding.TRedText.text = r1.toString()
        contentBinding.TGreenText.text = g1.toString()
        contentBinding.TBlueText.text = b1.toString()
        contentBinding.TEXTHexText.text = convertToHex(r1, g1, b1)
    }

    private fun updateBrightnessColors(r1:Int, g1: Int, b1:Int) : Int {
        var r=r1
        var g=g1
        var b=b1
        if(r1 < 0) {
            r=0
        }
        if(r1 > 255) {
            r=255
        }
        if(g1 < 0) {
            g=0
        }
        if(g1 > 255) {
            g=255
        }
        if(b1 < 0) {
            b=0
        }
        if(b1 > 255) {
            b=255
        }
        val bgColor1 = Color.rgb(r,g,b)
        contentBinding.mainBackground.setBackgroundColor(bgColor1)
        return bgColor1
    }

    private fun showHexDialog(selection1:Int, hexStr:String) {
        var done = false
        val builder = AlertDialog.Builder(this@MainActivity).apply {
            val view = layoutInflater.inflate(R.layout.hex_dialog_layout, LinearLayout(applicationContext), false)
            setView(view)
            val editText: EditText = view.findViewById(R.id.hexEditText)
            if (selection1 == BACKGROUND) {
                editText.setText(hexStr)
            }
            if (selection1 == TEXT) {
                editText.setText(hexStr)
            }
            setPositiveButton("OK") { _, _ ->
                val str = editText.text.toString()
                if (str.length != 6) {
                    Toast.makeText(applicationContext, "invalid value! please try again",Toast.LENGTH_LONG).show()
                    editText.error = "Invalid value"
                    return@setPositiveButton
                }
                val rStr = str.substring(0,2)
                val gStr = str.substring(2,4)
                val bStr = str.substring(4,6)
                try {
                    val tmpR = Integer.parseInt(rStr,16)
                    val tmpG = Integer.parseInt(gStr,16)
                    val tmpB = Integer.parseInt(bStr, 16)
                    if (selection1 == BACKGROUND) {
                        toggle(contentBinding.BGButton)
                        setBackgroundColor(tmpR, tmpG, tmpB)
                        cStack.addNewState(ColorObject(backgroundColor, textColor1))
                    }
                    if (selection1 == TEXT) {
                        toggle(contentBinding.TextButton)
                        setTextColor(tmpR, tmpG, tmpB)
                        cStack.addNewState(ColorObject(backgroundColor, textColor1))
                    }
                    done = true
                } catch (e: NumberFormatException) {
                    Toast.makeText(applicationContext, "invalid value! please try again",Toast.LENGTH_LONG).show()
                }
            }
            setNegativeButton("CANCEL") { _, _ ->
                done = true
            }
        }
        val dialog = builder.create()
        dialog.setTitle("Change color value")
        dialog.setOnDismissListener {
            if (!done) {
                dialog.show()
            }
        }
        dialog.show()
    }
    private fun toggleColorsInfo(show:Boolean) {
        if (show) {
            contentBinding.hexLayout.visibility = View.VISIBLE
            contentBinding.linearLayout2.visibility = View.VISIBLE
            contentBinding.seekBarsBackground.visibility = View.VISIBLE
            contentBinding.seekBarsText.visibility = View.VISIBLE
            toggle(contentBinding.BGButton)
        } else {
            contentBinding.hexLayout.visibility = View.GONE
            contentBinding.linearLayout2.visibility = View.GONE
            contentBinding.seekBarsBackground.visibility = View.GONE
            contentBinding.seekBarsText.visibility = View.GONE
        }
    }

    inner class ColorStack {

        var mainState = ColorObject(0,0)
        private val undoStack = Stack<ColorObject>()
        private val redoStack = Stack<ColorObject>()

        fun undo() : ColorObject {
            if (!undoStack.empty()) {
                redoStack.push(mainState)
                mainState = undoStack.pop()
            } else {
                Toast.makeText(applicationContext, "nothing to undo", Toast.LENGTH_SHORT).show()
            }
            return mainState
        }

        fun redo() : ColorObject {
            if (!redoStack.empty()) {
                undoStack.push(mainState)
                mainState = redoStack.pop()
            } else {
                Toast.makeText(applicationContext, "nothing to redo", Toast.LENGTH_SHORT).show()
            }
            return mainState
        }

        fun addNewState(cl:ColorObject) {
            redoStack.clear()
            undoStack.push(mainState)
            mainState = cl
            prefEditor.putInt(BGPREF, cl.bgColor).apply()
            prefEditor.putInt(TPREF, cl.textColor2).apply()
        }

    }

    class ColorObject(val bgColor:Int, val textColor2:Int)

    companion object {
        fun convertToHex(r1:Int, g1:Int, b1:Int) : String {
            var rH = Integer.toHexString(r1)
            var gH = Integer.toHexString(g1)
            var bH = Integer.toHexString(b1)
            if (rH.length == 1) {
                rH = "0$rH"
            }
            if (gH.length == 1) {
                gH = "0$gH"
            }
            if (bH.length == 1) {
                bH = "0$bH"
            }
            return "$rH$gH$bH"
        }
    }

    // easter egg at openColor button long click
}
