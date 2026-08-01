package com.custom.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Timer
import kotlin.concurrent.timerTask
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("wavelauncher")
        }
    }

    external fun getSystemStats(): String

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var drawerLayout: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var statsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statsTextView = findViewById(R.id.statsTextView)
        glSurfaceView = findViewById(R.id.glSurfaceView)
        drawerLayout = findViewById(R.id.drawerLayout)
        recyclerView = findViewById(R.id.recyclerView)

        // Init OpenGL ES 2.0 Engine
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(WaveRenderer())

        // App Drawer Setup
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        loadApps()

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            drawerLayout.visibility = if (drawerLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Loop pour le Dashboard (RAM, CPU, Temp)
        Timer().scheduleAtFixedRate(timerTask {
            val stats = getSystemStats()
            runOnUiThread { statsTextView.text = stats }
        }, 0, 2000) // MAJ toutes les 2 secondes
    }

    override fun onBackPressed() {
        if (drawerLayout.visibility == View.VISIBLE) {
            drawerLayout.visibility = View.GONE
        } else {
            // Ignorer le back button sur l'écran d'accueil
        }
    }

    private fun loadApps() {
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.category.LAUNCHER)
        val availableActivities = packageManager.queryIntentActivities(i, 0)
        
        recyclerView.adapter = AppAdapter(availableActivities, packageManager) { info ->
            val intent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
            if (intent != null) {
                startActivity(intent)
                drawerLayout.visibility = View.GONE
            }
        }
    }
}

class WaveRenderer : GLSurfaceView.Renderer {
    external fun initGL()
    external fun resizeGL(width: Int, height: Int)
    external fun stepGL()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) { initGL() }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) { resizeGL(width, height) }
    override fun onDrawFrame(gl: GL10?) { stepGL() }
}

class AppAdapter(
    private val apps: List<ResolveInfo>,
    private val pm: PackageManager,
    private val onClick: (ResolveInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.label.text = app.loadLabel(pm)
        holder.icon.setImageDrawable(app.loadIcon(pm))
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = apps.size
}
