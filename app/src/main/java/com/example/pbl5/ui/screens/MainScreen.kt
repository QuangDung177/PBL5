package com.example.pbl5.ui.screens

import android.content.Context
import android.net.Uri // Import thêm để sử dụng Uri.parse
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.pbl5.data.RaspberryPiRepository
import com.example.pbl5.ui.components.*
import com.example.pbl5.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, navController: NavHostController) {
    val serialId by viewModel.serialId
    val isLoading by viewModel.isLoading
    val isRefreshing by viewModel.isRefreshing
    val errorMessage by viewModel.errorMessage
    val isConnected by viewModel.isConnected
    val raspberryPiData by viewModel.raspberryPiData
    val deadFishData by viewModel.deadFishData
    val turbidityData by viewModel.turbidityData
    val turbidityDistribution by viewModel.turbidityDistribution
    val userDisplayName by viewModel.userDisplayName
    val notifications by viewModel.notifications

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { RaspberryPiRepository(context) }
    var showStreamDialog by remember { mutableStateOf(false) }
    var streamError by remember { mutableStateOf<String?>(null) }

    // Create a gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FBFF),
            Color(0xFFF0F4FA)
        )
    )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = userDisplayName,
                notifications = notifications,
                onNotificationsUpdated = { viewModel.loadNotifications() }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "Home",
                onTabSelected = { tab ->
                    navController.navigate(tab) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Serial input section
                SerialInput(
                    serialId = serialId,
                    onSerialIdChange = { viewModel.serialId.value = it },
                    onConnectClick = { viewModel.connectToRaspberryPi() },
                    onRefreshClick = { viewModel.refreshData() },
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    isConnected = isConnected,
                    errorMessage = errorMessage
                )

                // Show data only if connected
                raspberryPiData?.let { piData ->
                    Spacer(modifier = Modifier.height(8.dp))

                    // Fish statistics card
                    FishStats(
                        totalFishCount = piData.totalFishCount,
                        deadFishCount = deadFishData?.count ?: 0
                    )

                    // Turbidity statistics
                    turbidityData?.let { turbidity ->
                        TurbidityStats(
                            value = turbidity.value,
                            status = turbidity.status,
                            timestamp = turbidity.timestamp?.time,
                            deviceStatus = piData.status
                        )
                    } ?: run {
                        EmptyStateCard(
                            message = "Chưa có dữ liệu độ đục nước",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Turbidity chart
                    TurbidityChart(distribution = turbidityDistribution)

                    // Add Stream Button if connected
                    if (isConnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (serialId.isNotEmpty()) {
                                    showStreamDialog = true
                                    coroutineScope.launch {
                                        try {
                                            repository.setCameraState(serialId, true)
                                            streamError = null
                                        } catch (e: Exception) {
                                            streamError = "Lỗi khi bật camera: ${e.message}"
                                        }
                                    }
                                } else {
                                    streamError = "Vui lòng nhập Serial ID!"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Xem Stream")
                        }
                    }

                    // Add some space at the bottom
                    Spacer(modifier = Modifier.height(16.dp))
                } ?: run {
                    // Show empty state when not connected
                    EmptyStateCard(
                        message = "Chưa kết nối với Raspberry Pi",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Display stream error if any
                streamError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Show Stream Dialog if triggered
            if (showStreamDialog) {
                StreamDialog(
                    rtspUrl = "rtsp://192.168.1.11:8554/video0_unicast",
                    context = context,
                    onDismiss = {
                        showStreamDialog = false
                        coroutineScope.launch {
                            try {
                                repository.setCameraState(serialId, false)
                                streamError = null
                            } catch (e: Exception) {
                                streamError = "Lỗi khi tắt camera: ${e.message}"
                            }
                        }
                    },
                    onError = { errorMessage ->
                        streamError = errorMessage
                    }
                )
            }
        }
    }
}

@Composable
fun StreamDialog(
    rtspUrl: String,
    context: Context,
    onDismiss: () -> Unit,
    onError: (String) -> Unit
) {
    var streamStatus by remember { mutableStateOf("Đang kết nối đến stream...") }
    var showRetryButton by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    val maxRetries = 3

    // Khởi tạo LibVLC
    val libVLC = remember {
        LibVLC(context, arrayListOf(
            "--rtsp-tcp", // Sử dụng TCP cho RTSP (ổn định hơn UDP)
            "--no-video-title-show", // Ẩn tiêu đề video
            "--verbose=2", // Cấp độ log chi tiết
            "--network-caching=1000" // Tăng caching network (1 giây)
        ))
    }

    // Khởi tạo MediaPlayer
    val mediaPlayer = remember { MediaPlayer(libVLC) }

    // Khởi tạo VLCVideoLayout
    val vlcVideoLayout = remember { VLCVideoLayout(context) }

    // Hàm retry stream
    fun retryStream(url: String) {
        if (retryCount < maxRetries) {
            val uri = Uri.parse(url)
            val media = Media(libVLC, uri)
            mediaPlayer.media = media
            mediaPlayer.play()
            media.release()
            android.util.Log.d("VLCDebug", "Retry stream (Lần ${retryCount + 1}/$maxRetries)")
        }
    }

    // Gắn MediaPlayer với VLCVideoLayout và xử lý sự kiện
    LaunchedEffect(Unit) {
        // Gắn MediaPlayer với VLCVideoLayout
        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)
        android.util.Log.d("VLCDebug", "Attached MediaPlayer to VLCVideoLayout")

        // Cấu hình media và phát stream
        val uri = Uri.parse(rtspUrl)
        val media = Media(libVLC, uri)
        mediaPlayer.media = media
        mediaPlayer.play()
        media.release()
        android.util.Log.d("VLCDebug", "Playing stream from: $rtspUrl")

        // Đặt listener cho MediaPlayer
        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    streamStatus = "Stream đã sẵn sàng!"
                    showRetryButton = false
                    retryCount = 0
                    android.util.Log.d("VLCDebug", "Stream playing successfully")
                }
                MediaPlayer.Event.Buffering -> {
                    streamStatus = "Đang tải stream..."
                    android.util.Log.d("VLCDebug", "Buffering...")
                }
                MediaPlayer.Event.EndReached -> {
                    streamStatus = "Stream đã kết thúc."
                    showRetryButton = true
                    android.util.Log.d("VLCDebug", "Stream ended")
                }
                MediaPlayer.Event.EncounteredError -> {
                    val errorMessage = "Lỗi phát stream (Lần ${retryCount + 1}/$maxRetries): Không xác định (Kiểm tra log VLC)"
                    streamStatus = errorMessage
                    android.util.Log.e("VLCDebug", errorMessage)
                    if (retryCount < maxRetries) {
                        retryCount++
                        retryStream(rtspUrl)
                        streamStatus = "Đang thử lại (Lần $retryCount)..."
                    } else {
                        showRetryButton = true
                        onError(errorMessage)
                    }
                }
            }
        }
    }

    // Dọn dẹp tài nguyên khi Dialog bị hủy
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.detachViews()
            mediaPlayer.stop()
            mediaPlayer.release()
            libVLC.release()
            android.util.Log.d("VLCDebug", "Released resources")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    factory = { vlcVideoLayout },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = streamStatus,
                    color = if (streamStatus.contains("Lỗi")) Color.Red else Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (showRetryButton) {
                        Button(
                            onClick = {
                                streamStatus = "Đang thử lại thủ công..."
                                showRetryButton = false
                                retryCount = 0
                                retryStream(rtspUrl)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Thử lại")
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}