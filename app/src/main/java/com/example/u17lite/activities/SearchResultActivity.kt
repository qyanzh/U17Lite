package com.example.u17lite.activities

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.u17lite.R
import com.example.u17lite.fragments.ComicListFragment

class SearchResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            supportActionBar?.title = "\"$query\"的搜索结果"
            val prefix = "http://app.u17.com/v3/appV3_3/android/phone/search/searchResult?" +
                    "q=$query"
            val postfix = "&come_from=xiaomi" +
                    "&serialNumber=7de42d2e" +
                    "&v=4500102" +
                    "&model=MI+6" +
                    "&android_id=f5c9b6c9284551ad"
            val fragment = ComicListFragment.newInstance(prefix, postfix)
            supportFragmentManager.beginTransaction().add(R.id.fragmentHolder, fragment).commit()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
