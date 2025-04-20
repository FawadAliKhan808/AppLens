package com.android.applens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.applens.model.AppInfo
import com.android.applens.ui.theme.*
import com.android.applens.utils.drawableToBitmap
import com.android.applens.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Background.toArgb()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            AppLensTheme {
                val viewModel: AppViewModel = viewModel()
                val fullList by viewModel.appList.collectAsState()
                var query by remember { mutableStateOf("") }
                var selectedFilter by remember { mutableStateOf(SortOption.A_Z) }
                var isDropdownOpen by remember { mutableStateOf(false) }
                val focusManager = LocalFocusManager.current


                LaunchedEffect(Unit) { viewModel.loadApps() }

                val filteredList = remember(query, selectedFilter, fullList) {
                    val filtered = fullList.filter {
                        it.appName.contains(query, ignoreCase = true)
                    }
                    when (selectedFilter) {
                        SortOption.A_Z -> filtered.sortedBy { it.appName.lowercase(Locale.getDefault()) }
                        SortOption.Z_A -> filtered.sortedByDescending { it.appName.lowercase(Locale.getDefault()) }
                        SortOption.MostRecentlyUsed -> filtered.sortedByDescending { it.lastUsedTime }
                        SortOption.LeastRecentlyUsed -> filtered.sortedBy { it.lastUsedTime }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Background)
                            ) {
                                Text(
                                    text = "Installed Apps",
                                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )

                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp)),
                                        singleLine = true,
                                        placeholder = { Text("Search apps...", color = Color.LightGray) },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = BlueDark,
                                            unfocusedContainerColor = BlueDark,
                                            disabledContainerColor = BlueDark,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        trailingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box {
                                        IconButton(onClick = {
                                            isDropdownOpen = !isDropdownOpen
                                            focusManager.clearFocus()
                                        }) {
                                            Icon(Icons.Filled.Tune, contentDescription = "Sort", tint = Color.White)
                                        }


                                        DropdownMenu(
                                            expanded = isDropdownOpen,
                                            onDismissRequest = { isDropdownOpen = false },
                                            modifier = Modifier.background(BlueDark)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("A-Z", color = Color.White) },
                                                onClick = {
                                                    selectedFilter = SortOption.A_Z
                                                    isDropdownOpen = false
                                                    focusManager.clearFocus()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Z-A", color = Color.White) },
                                                onClick = {
                                                    selectedFilter = SortOption.Z_A
                                                    isDropdownOpen = false
                                                    focusManager.clearFocus()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Most Recently Used", color = Color.White) },
                                                onClick = {
                                                    selectedFilter = SortOption.MostRecentlyUsed
                                                    isDropdownOpen = false
                                                    focusManager.clearFocus()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Least Recently Used", color = Color.White) },
                                                onClick = {
                                                    selectedFilter = SortOption.LeastRecentlyUsed
                                                    isDropdownOpen = false
                                                    focusManager.clearFocus()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(filteredList) { app ->
                            AppRow(app)
                        }
                    }
                }
            }
        }
    }
}


//App Detail Cards
@Composable
fun AppRow(app: AppInfo) {
    val context = LocalContext.current
    val bitmap = remember(app.packageName) {
        app.icon?.let { drawableToBitmap(it) }
            ?: ContextCompat.getDrawable(context, R.drawable.ic_default_app)?.let { drawableToBitmap(it) }
    }

    val lastUsed = if (app.lastUsedTime == 0L) "A long time ago"
    else SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(app.lastUsedTime))

    val unused = System.currentTimeMillis() - app.lastUsedTime > 7 * 24 * 60 * 60 * 1000L

    fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(BackgroundDark, shape = MaterialTheme.shapes.medium)
            .clickable {
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("app", app)
                }
                context.startActivity(intent)
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = app.appName,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Last used: $lastUsed",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color.Gray
            )

            if (unused) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚠️ Consider uninstalling",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (unused) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { uninstallApp(app.packageName) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Uninstall",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black
                )
            }
        }
    }
}

enum class SortOption {
    A_Z, Z_A, MostRecentlyUsed, LeastRecentlyUsed
}
