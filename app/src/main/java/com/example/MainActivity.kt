package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.RecordRepository
import com.example.ui.DocuGridApp
import com.example.ui.DocuGridViewModel
import com.example.ui.DocuGridViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = RecordRepository(database.recordDao())
        val viewModel = ViewModelProvider(
            this,
            DocuGridViewModelFactory(repository)
        )[DocuGridViewModel::class.java]

        setContent {
            DocuGridApp(viewModel = viewModel)
        }
    }
}
