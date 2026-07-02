package com.rezi.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.rezi.finalproject.ui.theme.FinalProjectTheme

// 1. THE DATA (This is the blueprint for our To-Do items)
data class Todo(val id: Int, val title: String, val completed: Boolean)

// 2. THE DATABASE CONNECTION (Retrofit)
interface ApiService {
    @GET("todos")
    suspend fun getTodos(): List<Todo>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://jsonplaceholder.typicode.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)

// 3. MVVM ARCHITECTURE (The ViewModel)
class TodoViewModel : ViewModel() {
    var todoList by mutableStateOf<List<Todo>>(emptyList())
        private set

    init {
        fetchTodos()
    }

    fun fetchTodos() {
        viewModelScope.launch {
            try {
                // Get the first 20 items from the internet database
                todoList = apiService.getTodos().take(20)
            } catch (e: Exception) {
                // Ignore errors for simplicity
            }
        }
    }
}

// 4. THE SCREEN (Menu, List, and New Feature)
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjectTheme {
                val viewModel: TodoViewModel = viewModel()
                val context = LocalContext.current
                var showMenu by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // REQUIREMENT: Must have a Menu
                        TopAppBar(
                            title = { Text("Final Project App") },
                            actions = {
                                IconButton(onClick = { showMenu = !showMenu }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Refresh Data") },
                                        onClick = {
                                            viewModel.fetchTodos()
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    // REQUIREMENT: Must have a List
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(viewModel.todoList) { todo ->
                            ListItem(
                                headlineContent = { Text(todo.title) },
                                supportingContent = { Text(if (todo.completed) "Done" else "Pending") },
                                trailingContent = {
                                    // REQUIREMENT: A feature not used before (Share Intent)
                                    IconButton(onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Look at this task: ${todo.title}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Task"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share")
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}