package com.android.applens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.applens.model.AppInfo
import com.android.applens.ui.theme.AppLensTheme
import com.android.applens.ui.theme.Background
import com.android.applens.utils.drawableToBitmap

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appInfo = intent.getParcelableExtra<AppInfo>("app")

        setContent {
            AppLensTheme {
                appInfo?.let {
                    AppDetailScreen(app = it, onBack = { finish() })
                }
            }
        }
    }
}

//Main UI
@Composable
fun AppDetailScreen(app: AppInfo, onBack: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(app.packageName) {
        try {
            val icon = context.packageManager.getApplicationIcon(app.packageName)
            drawableToBitmap(icon)
        } catch (e: Exception) {
            drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_default_app)!!)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppDetailTopBar(onBack = onBack)

            Spacer(modifier = Modifier.height(32.dp))

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(140.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = app.appName,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Version: ${app.versionName}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Package: ${app.packageName}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { uninstallApp(context, app.packageName) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Uninstall", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Open in Play Store", color = Color.Black)
            }
        }
    }
}


//TopBar
@Composable
fun AppDetailTopBar(onBack: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .height(56.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "App Details",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
    }
}


fun uninstallApp(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Error uninstalling the app", Toast.LENGTH_SHORT).show()
    }
}