package catt.sample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.include_content.*
import kotlinx.android.synthetic.main.include_menu.*

class DemoActivity : AppCompatActivity() {
    private val _TAG: String = DemoActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        menuTextView.setOnClickListener {
            Log.e(_TAG, "##################    onClick: menuTextView")
            Toast.makeText(applicationContext, "onClick: menuTextView", Toast.LENGTH_SHORT).show()

        }
        contentTextView.setOnClickListener {
            Log.e(_TAG, "@@@@@@@@@@@@@@@@@@    onClick: contentTextView")
            Toast.makeText(applicationContext, "onClick: contentTextView", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.clearFindViewByIdCache()
    }
}