package com.example.u17lite.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import com.example.u17lite.R
import com.example.u17lite.fragments.ComicListFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val CHECK_UPDATE_TIME = 5 * 60 * 1000L
        const val prefix = "http://app.u17.com/v3/appV3_3/android/phone/list/getRankComicList?" +
                "period=total&type=2"
        const val postfix = "&come_from=xiaomi" +
                "&serialNumber=7de42d2e" +
                "&v=450010" +
                "&model=MI+6" +
                "&android_id=f5c9b6c9284551ad"
    }

    var isForeground = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        isForeground = true
        Thread {
            while (isForeground) {
                Log.d("TAG", "alive")
                Thread.sleep(CHECK_UPDATE_TIME)
            }
            Log.d("TAG", "terminate")
        }.start()

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        val fragment = ComicListFragment.newInstance(prefix, postfix)
        supportFragmentManager.beginTransaction().let {
            if (it.isEmpty) {
                it.add(R.id.fragmentHolder, fragment).commit()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isForeground = false
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(true) // Do not iconify the widget; expand it by default

        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_download -> {
                startActivity(Intent(this, TestActivity::class.java))
            }
            R.id.nav_star -> {

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
