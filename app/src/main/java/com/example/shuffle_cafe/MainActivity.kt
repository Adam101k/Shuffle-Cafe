package com.example.shuffle_cafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Shuffle_CafeTheme {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.fillMaxHeight(0.04f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                            .border(width = 1.dp, color = Color.Black, RoundedCornerShape(32.dp))
                            .background(Color.White, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {                    }
                    Spacer(modifier = Modifier.fillMaxHeight(0.04f).weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                            .border(width = 1.dp, color = Color.Black, RoundedCornerShape(32.dp))
                            .background(Color.LightGray, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {                    }
                    Spacer(modifier = Modifier.fillMaxHeight(0.04f))
                }
            }
        }
    }
}


