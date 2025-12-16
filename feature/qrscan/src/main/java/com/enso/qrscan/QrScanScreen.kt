package com.enso.qrscan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.qrscan.parser.LottoTicketInfo
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    viewModel: QrScanViewModel = hiltViewModel(),
    onScanSuccess: (LottoTicketInfo) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QrScanEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is QrScanEffect.ScanSuccess -> {
                    onScanSuccess(effect.ticketInfo)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onEvent(QrScanEvent.StartScan)
        }
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                viewModel.onEvent(QrScanEvent.StartScan)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR 코드 스캔") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 카메라는 항상 표시하되, 성공 후에는 스캔 처리 안 함
            CameraPreview(
                onQrCodeDetected = { content, bounds ->
                    if (!uiState.isSuccess) {
                        viewModel.onEvent(QrScanEvent.ProcessQrCode(content, bounds))
                    }
                },
                onBoundsUpdate = { bounds ->
                    if (!uiState.isSuccess) {
                        viewModel.onEvent(QrScanEvent.UpdateDetectedBounds(bounds))
                    }
                }
            )

            // QR 코드 감지 오버레이
            uiState.detectedBounds?.let { bounds ->
                QrOverlay(
                    bounds = bounds,
                    isSuccess = uiState.isSuccess
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isSuccess) {
                        "QR 코드 인식 완료!"
                    } else {
                        "로또 용지의 QR 코드를 카메라에 비춰주세요"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.isSuccess) {
                        Color(0xFF00FF88)  // 밝은 민트 그린
                    } else {
                        Color.White.copy(alpha = 0.9f)
                    },
                    fontWeight = if (uiState.isSuccess) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun QrOverlay(
    bounds: QrCodeBounds,
    isSuccess: Boolean
) {
    // 애니메이션: bounds가 변경될 때마다 0에서 1로 애니메이션
    var animationTrigger by remember { mutableStateOf(0) }
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(bounds) {
        animationStarted = false
        animationTrigger++
        kotlinx.coroutines.delay(50)
        animationStarted = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "qr_box_animation"
    )

    // 네온 색상 (시안 -> 성공 시 밝은 녹색)
    val primaryColor = if (isSuccess) Color(0xFF00FF88) else Color(0xFF00D4FF)
    val glowColor = if (isSuccess) Color(0xFF00FF88) else Color(0xFF00D4FF)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val screenWidth = size.width
        val screenHeight = size.height

        if (bounds.cornerPoints.size >= 4) {
            val imageWidth = bounds.sourceImageWidth.toFloat()
            val imageHeight = bounds.sourceImageHeight.toFloat()
            val rotation = bounds.rotationDegrees

            val rotatedImageWidth: Float
            val rotatedImageHeight: Float
            if (rotation == 90 || rotation == 270) {
                rotatedImageWidth = imageHeight
                rotatedImageHeight = imageWidth
            } else {
                rotatedImageWidth = imageWidth
                rotatedImageHeight = imageHeight
            }

            val scaleX = screenWidth / rotatedImageWidth
            val scaleY = screenHeight / rotatedImageHeight
            val scale = maxOf(scaleX, scaleY)

            val offsetX = (screenWidth - rotatedImageWidth * scale) / 2f
            val offsetY = (screenHeight - rotatedImageHeight * scale) / 2f

            fun transformPoint(point: PointF): Offset {
                return Offset(
                    point.x * scale + offsetX,
                    point.y * scale + offsetY
                )
            }

            val transformedCorners = bounds.cornerPoints.map { transformPoint(it) }

            val centerX = transformedCorners.map { it.x }.average().toFloat()
            val centerY = transformedCorners.map { it.y }.average().toFloat()

            val initialScale = 1.2f
            val currentScale = initialScale - (initialScale - 1f) * animationProgress

            val animatedCorners = transformedCorners.map { corner ->
                val dx = (corner.x - centerX) * currentScale
                val dy = (corner.y - centerY) * currentScale
                Offset(centerX + dx, centerY + dy)
            }

            // QR 박스 크기 계산
            val qrWidth = maxOf(
                kotlin.math.abs(animatedCorners[1].x - animatedCorners[0].x),
                kotlin.math.abs(animatedCorners[2].x - animatedCorners[3].x)
            )
            val cornerLength = qrWidth * 0.25f

            // 글로우 효과 (바깥쪽부터 그리기)
            val glowLayers = listOf(
                24f to 0.08f,
                18f to 0.12f,
                12f to 0.18f,
                6f to 0.25f
            )

            // 코너 L자 그리기 함수
            fun drawCornerL(
                corner: Offset,
                cornerIndex: Int,
                strokeWidth: Float,
                color: Color
            ) {
                // 코너 인덱스에 따른 방향 결정
                // 0: 좌상단, 1: 우상단, 2: 우하단, 3: 좌하단
                val (nextCorner, prevCorner) = when (cornerIndex) {
                    0 -> animatedCorners[1] to animatedCorners[3]
                    1 -> animatedCorners[2] to animatedCorners[0]
                    2 -> animatedCorners[3] to animatedCorners[1]
                    else -> animatedCorners[0] to animatedCorners[2]
                }

                // 다음 코너 방향으로의 단위 벡터
                val toNextDx = nextCorner.x - corner.x
                val toNextDy = nextCorner.y - corner.y
                val toNextLen = kotlin.math.sqrt(toNextDx * toNextDx + toNextDy * toNextDy)
                val nextUnitX = toNextDx / toNextLen
                val nextUnitY = toNextDy / toNextLen

                // 이전 코너 방향으로의 단위 벡터
                val toPrevDx = prevCorner.x - corner.x
                val toPrevDy = prevCorner.y - corner.y
                val toPrevLen = kotlin.math.sqrt(toPrevDx * toPrevDx + toPrevDy * toPrevDy)
                val prevUnitX = toPrevDx / toPrevLen
                val prevUnitY = toPrevDy / toPrevLen

                // L자 Path 생성
                val lPath = Path().apply {
                    // 이전 코너 방향으로 L자 시작점
                    moveTo(
                        corner.x + prevUnitX * cornerLength,
                        corner.y + prevUnitY * cornerLength
                    )
                    // 코너점
                    lineTo(corner.x, corner.y)
                    // 다음 코너 방향으로 L자 끝점
                    lineTo(
                        corner.x + nextUnitX * cornerLength,
                        corner.y + nextUnitY * cornerLength
                    )
                }

                drawPath(
                    path = lPath,
                    color = color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }

            // 글로우 레이어 그리기 (코너 L자)
            glowLayers.forEach { (width, alpha) ->
                animatedCorners.forEachIndexed { index, corner ->
                    drawCornerL(
                        corner = corner,
                        cornerIndex = index,
                        strokeWidth = width,
                        color = glowColor.copy(alpha = alpha * animationProgress)
                    )
                }
            }

            // 메인 코너 L자 그리기
            animatedCorners.forEachIndexed { index, corner ->
                drawCornerL(
                    corner = corner,
                    cornerIndex = index,
                    strokeWidth = 4f,
                    color = primaryColor.copy(alpha = animationProgress)
                )
            }

            // 코너 포인트에 작은 빛나는 점
            animatedCorners.forEach { corner ->
                // 글로우
                drawCircle(
                    color = glowColor.copy(alpha = 0.3f * animationProgress),
                    radius = 12f,
                    center = corner
                )
                drawCircle(
                    color = glowColor.copy(alpha = 0.5f * animationProgress),
                    radius = 8f,
                    center = corner
                )
                // 메인 점
                drawCircle(
                    color = primaryColor.copy(alpha = animationProgress),
                    radius = 5f,
                    center = corner
                )
                // 하이라이트
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f * animationProgress),
                    radius = 2f,
                    center = Offset(corner.x - 1f, corner.y - 1f)
                )
            }

            // 성공 시 체크마크
            if (isSuccess && animationProgress > 0.5f) {
                val checkAlpha = ((animationProgress - 0.5f) * 2f).coerceIn(0f, 1f)
                val checkSize = qrWidth * 0.35f

                val checkPath = Path().apply {
                    moveTo(centerX - checkSize * 0.35f, centerY)
                    lineTo(centerX - checkSize * 0.05f, centerY + checkSize * 0.3f)
                    lineTo(centerX + checkSize * 0.4f, centerY - checkSize * 0.25f)
                }

                // 체크마크 글로우
                listOf(20f to 0.15f, 14f to 0.25f, 8f to 0.4f).forEach { (width, alpha) ->
                    drawPath(
                        path = checkPath,
                        color = Color(0xFF00FF88).copy(alpha = alpha * checkAlpha),
                        style = Stroke(
                            width = width,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                }

                // 메인 체크마크
                drawPath(
                    path = checkPath,
                    color = Color.White.copy(alpha = checkAlpha),
                    style = Stroke(
                        width = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onQrCodeDetected: (String, QrCodeBounds) -> Unit,
    onBoundsUpdate: (QrCodeBounds?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy, onQrCodeDetected, onBoundsUpdate)
                    }
                }

            try {
                cameraProviderFuture.get().let { cameraProvider ->
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQrCodeDetected: (String, QrCodeBounds) -> Unit,
    onBoundsUpdate: (QrCodeBounds?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                var foundLottoQr = false

                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { content ->
                            if (content.contains("?v=")) {
                                foundLottoQr = true

                                // cornerPoints 추출
                                barcode.cornerPoints?.let { points ->
                                    if (points.size >= 4) {
                                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                        val bounds = QrCodeBounds(
                                            cornerPoints = points.map { point ->
                                                PointF(point.x.toFloat(), point.y.toFloat())
                                            },
                                            sourceImageWidth = imageProxy.width,
                                            sourceImageHeight = imageProxy.height,
                                            rotationDegrees = rotationDegrees
                                        )
                                        onBoundsUpdate(bounds)
                                        onQrCodeDetected(content, bounds)
                                    }
                                }
                            }
                        }
                    }
                }

                // 로또 QR 코드가 없으면 bounds 제거
                if (!foundLottoQr) {
                    onBoundsUpdate(null)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                onBoundsUpdate(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
