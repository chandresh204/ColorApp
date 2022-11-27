package chad.orionsoft.colorapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import chad.orionsoft.colorapp.databinding.ActivityShareBinding

class ShareActivity : AppCompatActivity() {

    private val binding: ActivityShareBinding by lazy {
        ActivityShareBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        binding.shareText.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (intent.hasExtra("bg") && intent.hasExtra("text")) {
                val backgroundColor = intent.getIntExtra("bg", 0)
                val textColor = intent.getIntExtra("text", 0)
                binding.shareBG.setBackgroundColor(backgroundColor)
                binding.shareText.setTextColor(textColor)
        }

        Toast.makeText(applicationContext, "please take a ScreenShot and share it", Toast.LENGTH_LONG).show()
    }
}
