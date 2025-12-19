package com.enso.qrscan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.home.ui.components.MyLottoSection
import com.enso.home.ui.theme.BackgroundLight
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private data class QrBoxMetrics(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float
) {
    val area: Float = width * height
    val size: Float = maxOf(width, height)
}

private fun QrCodeBounds.toMetricsOrNull(): QrBoxMetrics? {
    if (cornerPoints.isEmpty()) return null
    val minX = cornerPoints.minOf { it.x }
    val maxX = cornerPoints.maxOf { it.x }
    val minY = cornerPoints.minOf { it.y }
    val maxY = cornerPoints.maxOf { it.y }
    val width = (maxX - minX).coerceAtLeast(1f)
    val height = (maxY - minY).coerceAtLeast(1f)
    return QrBoxMetrics(
        centerX = (minX + maxX) / 2f,
        centerY = (minY + maxY) / 2f,
        width = width,
        height = height
    )
}

private fun isSimilarBounds(
    previous: QrBoxMetrics,
    current: QrBoxMetrics,
    centerMoveThresholdRatio: Float = 0.05f, // QR 크기 대비 5%
    areaChangeThresholdRatio: Float = 0.10f  // 10%
): Boolean {
    val dx = previous.centerX - current.centerX
    val dy = previous.centerY - current.centerY
    val centerDistance = kotlin.math.sqrt(dx * dx + dy * dy)
    val centerThreshold = previous.size * centerMoveThresholdRatio

    val areaDeltaRatio = kotlin.math.abs(current.area - previous.area) / previous.area

    return centerDistance < centerThreshold && areaDeltaRatio < areaChangeThresholdRatio
}

/**
 * QR 스캐너 오버레이 스타일 상수
 */
private object QrOverlayStyle {
    // 색상
    val defaultColor = Color(0xFFA78BFA)    // 퍼플
    val successColor = Color(0xFF86EFAC)    // 민트 그린

    // 두께
    const val cornerStrokeWidth = 8f
    const val checkmarkStrokeWidth = 8f

    // 코너 설정
    const val cornerLengthRatio = 0.22f     // QR 크기 대비 코너 길이 비율
    const val cornerRadius = 8f             // 코너 둥글기

    // 애니메이션
    const val animationDuration = 350
    const val initialScaleRatio = 1.15f     // 애니메이션 시작 스케일
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    viewModel: QrScanViewModel = hiltViewModel(),
    onExit: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QrScanEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 전체 화면 카메라 프리뷰 (시스템 바 영역 포함)
        CameraPreview(
            isFlashEnabled = uiState.isFlashEnabled,
            onQrCodeDetected = { content, bounds ->
                if (!uiState.isSuccess && !uiState.isSaving) {
                    viewModel.onEvent(QrScanEvent.ProcessQrCode(content, bounds))
                }
            },
            onBoundsUpdate = { bounds ->
                viewModel.onEvent(QrScanEvent.UpdateDetectedBounds(bounds))
            },
            onFocusRequest = { x, y ->
                viewModel.onEvent(QrScanEvent.RequestFocus(x, y))
            }
        )

        // QR 코드 감지 오버레이
        uiState.detectedBounds?.let { bounds ->
            QrOverlay(
                bounds = bounds,
                isSuccess = uiState.isSuccess,
                isCurrentlyDetected = uiState.isCurrentlyDetected
            )
        }

        // 포커스 애니메이션
        if (uiState.isFocusing) {
            uiState.focusPoint?.let { point ->
                FocusIndicator(point = point)
            }
        }

        // 상단 버튼들 (뒤로가기, 플래시) - 상태바 inset 적용
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(statusBarPadding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 뒤로가기 버튼
            CircularIconButton(
                onClick = onExit,
                icon = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.qr_scan_back)
            )

            // 플래시 버튼
            CircularIconButton(
                onClick = { viewModel.onEvent(QrScanEvent.ToggleFlash) },
                icon = if (uiState.isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = if (uiState.isFlashEnabled) {
                    stringResource(R.string.qr_scan_flash_on)
                } else {
                    stringResource(R.string.qr_scan_flash_off)
                }
            )
        }

        // 하단 안내 문구
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isSuccess) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.onEvent(QrScanEvent.ResetAfterSuccess) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.qr_scan_continue))
                    }
                    TextButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.qr_scan_exit))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            val statusText = when (uiState.lastScanResult) {
                QrScanResult.Saved -> stringResource(R.string.qr_scan_saved)
                QrScanResult.Duplicate -> stringResource(R.string.qr_scan_duplicate)
                null -> stringResource(R.string.qr_scan_guide)
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (uiState.isSuccess) QrOverlayStyle.successColor else Color.White,
                fontWeight = if (uiState.isSuccess) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BackgroundLight,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    MyLottoSection(
                        tickets = uiState.tickets,
                        lottoResults = uiState.lottoResults,
                        currentRound = uiState.currentRound,
                        onCheckWinning = { ticketId ->
                            viewModel.onEvent(QrScanEvent.CheckWinning(ticketId))
                        },
                        onDeleteTicket = { ticketId ->
                            viewModel.onEvent(QrScanEvent.DeleteTicket(ticketId))
                        },
                        showViewAll = false,
                        enableDelete = false
                    )
                }
            }
        }

        // 스낵바 호스트
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun CircularIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

@Composable
private fun FocusIndicator(point: Offset) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(point) {
        animationStarted = false
        kotlinx.coroutines.delay(10)
        animationStarted = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "focus_animation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val initialRadius = 80f
        val finalRadius = 60f
        val currentRadius = initialRadius - (initialRadius - finalRadius) * animationProgress

        val initialAlpha = 1f
        val finalAlpha = 0f
        val currentAlpha = initialAlpha - (initialAlpha - finalAlpha) * animationProgress

        // 외부 원
        drawCircle(
            color = Color.White.copy(alpha = currentAlpha * 0.8f),
            radius = currentRadius,
            center = point,
            style = Stroke(width = 3f)
        )

        // 내부 원 (더 작은 원)
        drawCircle(
            color = Color.White.copy(alpha = currentAlpha * 0.4f),
            radius = currentRadius * 0.7f,
            center = point,
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun QrOverlay(
    bounds: QrCodeBounds,
    isSuccess: Boolean,
    isCurrentlyDetected: Boolean
) {
    var animationStarted by remember { mutableStateOf(false) }
    var shouldAnimateScale by remember { mutableStateOf(true) }
    var previousMetrics by remember { mutableStateOf<QrBoxMetrics?>(null) }
    var previousWasDetected by remember { mutableStateOf(false) }

    // 소스 이미지 좌표계에서 corner point를 부드럽게 이동시키기 위한 state
    val animatedSourceCorners = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(bounds, isCurrentlyDetected) {
        if (!isCurrentlyDetected) {
            previousWasDetected = false
            return@LaunchedEffect
        }

        val targetSourceCorners = bounds.cornerPoints.take(4).map { Offset(it.x, it.y) }
        val currentMetrics = bounds.toMetricsOrNull()
        val prevMetrics = previousMetrics

        val isFirstOrReDetected = !previousWasDetected
        val isMajorChange = when {
            prevMetrics == null || currentMetrics == null -> true
            else -> !isSimilarBounds(previous = prevMetrics, current = currentMetrics)
        }

        shouldAnimateScale = isFirstOrReDetected || isMajorChange

        // 이전 메트릭 갱신
        previousMetrics = currentMetrics
        previousWasDetected = true

        // corner 애니메이션 초기화/갱신
        if (animatedSourceCorners.size != 4) {
            animatedSourceCorners.clear()
            animatedSourceCorners.addAll(targetSourceCorners)
        } else if (isFirstOrReDetected) {
            // 재인식 시에는 점프(스케일 애니메이션으로 "등장" 느낌)
            for (i in 0 until 4) animatedSourceCorners[i] = targetSourceCorners[i]
        } else {
            // 위치가 부드럽게 이동하도록 0.25초 tween
            val startCorners = animatedSourceCorners.toList()
            val steps = 12
            val durationMillis = 250
            for (step in 1..steps) {
                val t = step.toFloat() / steps.toFloat()
                val eased = androidx.compose.animation.core.FastOutSlowInEasing.transform(t)
                for (i in 0 until 4) {
                    val start = startCorners[i]
                    val target = targetSourceCorners[i]
                    animatedSourceCorners[i] = Offset(
                        x = start.x + (target.x - start.x) * eased,
                        y = start.y + (target.y - start.y) * eased
                    )
                }
                kotlinx.coroutines.delay((durationMillis / steps).toLong())
            }
        }

        // 스케일 애니메이션 트리거
        if (shouldAnimateScale) {
            animationStarted = false
            kotlinx.coroutines.delay(50)
            animationStarted = true
        } else {
            animationStarted = true
        }
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = if (shouldAnimateScale) {
            tween(
                durationMillis = QrOverlayStyle.animationDuration,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "qr_box_animation"
    )

    val cornerColor = if (isSuccess) QrOverlayStyle.successColor else QrOverlayStyle.defaultColor
    val overlayAlpha = if (shouldAnimateScale) animationProgress else 1f

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

            val sourceCornersForDraw = if (animatedSourceCorners.size == 4) {
                animatedSourceCorners.map { Offset(it.x, it.y) }
            } else {
                bounds.cornerPoints.take(4).map { Offset(it.x, it.y) }
            }

            val transformedCorners = sourceCornersForDraw.map { src ->
                transformPoint(PointF(src.x, src.y))
            }

            val centerX = transformedCorners.map { it.x }.average().toFloat()
            val centerY = transformedCorners.map { it.y }.average().toFloat()

            val initialScale = QrOverlayStyle.initialScaleRatio
            val currentScale = if (shouldAnimateScale) {
                initialScale - (initialScale - 1f) * animationProgress
            } else {
                1f
            }

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
            val cornerLength = qrWidth * QrOverlayStyle.cornerLengthRatio
            val cornerRadius = QrOverlayStyle.cornerRadius

            // iOS 스타일 코너 L자 그리기
            fun drawRoundedCornerL(
                corner: Offset,
                cornerIndex: Int
            ) {
                val (nextCorner, prevCorner) = when (cornerIndex) {
                    0 -> animatedCorners[1] to animatedCorners[3]
                    1 -> animatedCorners[2] to animatedCorners[0]
                    2 -> animatedCorners[3] to animatedCorners[1]
                    else -> animatedCorners[0] to animatedCorners[2]
                }

                val toNextDx = nextCorner.x - corner.x
                val toNextDy = nextCorner.y - corner.y
                val toNextLen = kotlin.math.sqrt(toNextDx * toNextDx + toNextDy * toNextDy)
                val nextUnitX = toNextDx / toNextLen
                val nextUnitY = toNextDy / toNextLen

                val toPrevDx = prevCorner.x - corner.x
                val toPrevDy = prevCorner.y - corner.y
                val toPrevLen = kotlin.math.sqrt(toPrevDx * toPrevDx + toPrevDy * toPrevDy)
                val prevUnitX = toPrevDx / toPrevLen
                val prevUnitY = toPrevDy / toPrevLen

                val lPath = Path().apply {
                    moveTo(
                        corner.x + prevUnitX * cornerLength,
                        corner.y + prevUnitY * cornerLength
                    )
                    lineTo(
                        corner.x + prevUnitX * cornerRadius,
                        corner.y + prevUnitY * cornerRadius
                    )
                    // 둥근 코너
                    quadraticTo(
                        corner.x,
                        corner.y,
                        corner.x + nextUnitX * cornerRadius,
                        corner.y + nextUnitY * cornerRadius
                    )
                    lineTo(
                        corner.x + nextUnitX * cornerLength,
                        corner.y + nextUnitY * cornerLength
                    )
                }

                // 메인 라인
                drawPath(
                    path = lPath,
                    color = cornerColor.copy(alpha = overlayAlpha),
                    style = Stroke(
                        width = QrOverlayStyle.cornerStrokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }

            // 4개 코너 그리기
            animatedCorners.forEachIndexed { index, corner ->
                drawRoundedCornerL(corner, index)
            }

            // 성공 시 체크마크 (iOS 스타일)
            val checkProgress = if (shouldAnimateScale) animationProgress else 1f
            if (isSuccess && checkProgress > 0.5f) {
                val checkAlpha = ((checkProgress - 0.5f) * 2f).coerceIn(0f, 1f)
                val checkSize = qrWidth * 0.32f

                val checkPath = Path().apply {
                    moveTo(centerX - checkSize * 0.32f, centerY + checkSize * 0.05f)
                    lineTo(centerX - checkSize * 0.08f, centerY + checkSize * 0.28f)
                    lineTo(centerX + checkSize * 0.38f, centerY - checkSize * 0.22f)
                }

                // 체크마크
                drawPath(
                    path = checkPath,
                    color = QrOverlayStyle.successColor.copy(alpha = checkAlpha),
                    style = Stroke(
                        width = QrOverlayStyle.checkmarkStrokeWidth,
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
    isFlashEnabled: Boolean,
    onQrCodeDetected: (String, QrCodeBounds) -> Unit,
    onBoundsUpdate: (QrCodeBounds?) -> Unit,
    onFocusRequest: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    LaunchedEffect(isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val preview = Preview.Builder()
                .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
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
                    camera = cameraProvider.bindToLifecycle(
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

            // 터치로 포커스 맞추기
            previewView.setOnTouchListener { view, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    val x = event.x
                    val y = event.y

                    // UI에 애니메이션 표시를 위한 콜백
                    onFocusRequest(x, y)

                    // 실제 카메라 포커스 수행
                    camera?.let { cam ->
                        // PreviewView의 MeteringPointFactory 사용 (정확한 좌표 변환)
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(x, y)

                        // AF/AE 포인트 설정 및 자동 취소 비활성화
                        val action = FocusMeteringAction.Builder(point)
                            .disableAutoCancel()
                            .build()

                        cam.cameraControl.startFocusAndMetering(action)
                    }
                }
                true
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
                // 로또 QR 코드를 찾아서 처리
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { content ->
                            if (content.contains("?v=")) {
                                // 로또 QR 코드 발견
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
                                        // QR이 감지될 때만 bounds 업데이트
                                        onBoundsUpdate(bounds)
                                        onQrCodeDetected(content, bounds)
                                    }
                                }
                            }
                        }
                    }
                }
                // 중요: QR이 감지되지 않아도 onBoundsUpdate(null)을 호출하지 않음
                // 이렇게 하면 일시적인 인식 실패 시에도 박스가 유지되어 깜빡임 방지
            }
            .addOnFailureListener {
                it.printStackTrace()
                // onBoundsUpdate(null) 제거: 에러 시에도 박스 유지
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
