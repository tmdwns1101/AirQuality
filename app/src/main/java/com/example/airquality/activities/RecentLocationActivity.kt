package com.example.airquality.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airquality.adpater.RecentLocationAdapter
import com.example.airquality.databinding.ActivityRecentLocationBinding
import com.example.airquality.model.LatLonData
import com.example.airquality.repository.Repository
import com.example.airquality.repository.room.entity.RecentLocation
import com.example.airquality.viewModels.RecentLocationViewModel
import kotlinx.coroutines.launch

class RecentLocationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRecentLocationBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: RecentLocationAdapter

    private val viewModel: RecentLocationViewModel by lazy {
        ViewModelProvider(
            this,
            RecentLocationViewModel.Factory(application)
        )[RecentLocationViewModel::class.java]
    }

    private val repository: Repository by lazy {
        Repository.getInstance(application)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setObserve()
        setView()

    }

    private fun setView() {
        adapter = RecentLocationAdapter().apply {
            setOnItemClickListener(object : RecentLocationAdapter.OnItemClickListener {
                override fun onItemClick(v: View, item: RecentLocation) {
                    lifecycleScope.launch {

                        var recentLocation = repository.getRecentLocation(item)
                        var latLonData = LatLonData(recentLocation?.latitude ?: 0.0, recentLocation?.longitude ?: 0.0)
                        setMain(latLonData)

                    }
                }

                override fun onItemLongClick(v: View, item: RecentLocation) {
                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(this@RecentLocationActivity)
                    builder.setTitle("최근 조회한 장소 삭제")
                    builder.setMessage("정말 삭제하시겠습니까?")
                    builder.setNegativeButton("취소", null)
                    builder.setPositiveButton(
                        "네 "
                    ) { _, _ -> viewModel.deleteRecentLocation(item) }
                    builder.show()
                }
            })
        }
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

    }

    private fun setObserve() {
        viewModel.recentLocationList.observe(this) {
            adapter.setData(it)
        }
    }

    private fun setMain(latLonData: LatLonData) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("latitude", latLonData?.latitude ?: 0.0)
        intent.putExtra("longitude",latLonData?.longitude ?: 0.0)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }
}