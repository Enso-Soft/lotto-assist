package com.enso.qrscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.enso.designsystem.theme.LottoTheme
import com.enso.designsystem.theme.getLottoBallColor
import com.enso.qrscan.parser.LottoTicketInfo
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val SCAN_VIBRATION_MS = 40L
private const val WINNING_VIBRATION_MS = 160L

private data class DrawScheduleInfo(
    val dateText: String,
    val days: Int,
    val hours: Int,
    val hoursOnly: Boolean
)



/**
 * QR 스캐너 오버레이 스타일 상수
 */
private object QrOverlayStyle {
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
    onScanSuccess: (LottoTicketInfo) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    // 리컴포지션 최적화: detectedBounds를 별도 StateFlow로 수집
    // 이렇게 하면 bounds 변경 시 전체 화면이 아닌 QrOverlay만 재컴포즈됨
    val detectedBounds by viewModel.detectedBounds.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 화면 진입 시 완전 초기화
    LaunchedEffect(Unit) {
        viewModel.onEvent(QrScanEvent.ResetScreen)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QrScanEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is QrScanEffect.ShowDuplicateMessage -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.qr_already_added))
                }
                is QrScanEffect.VibrateScan -> {
                    performVibration(context, SCAN_VIBRATION_MS)
                }
                is QrScanEffect.VibrateWinning -> {
                    performVibration(context, WINNING_VIBRATION_MS)
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

    // 결과 패널 높이를 측정하고 애니메이션
    val density = LocalDensity.current
    var measuredHeight by remember { mutableStateOf(0) }
    
    // 스캔 중이거나 저장된 티켓이 있으면 패널 표시
    val hasScannedResult = uiState.scannedResult != null
    val hasSavedTickets = uiState.savedTickets.isNotEmpty()
    val hasAnyTickets = hasScannedResult || hasSavedTickets
    
    // 카메라 축소 애니메이션
    val animatedHeight by animateDpAsState(
        targetValue = if (hasAnyTickets && measuredHeight > 0) measuredHeight.dp else 0.dp,
        animationSpec = tween(durationMillis = 400),
        label = "resultPanelHeight"
    )
    
    // 결과 패널 슬라이드업 애니메이션
    val animatedOffset by animateDpAsState(
        targetValue = if (hasAnyTickets) 0.dp else (if (measuredHeight > 0) measuredHeight.dp else 500.dp),
        animationSpec = tween(durationMillis = 400),
        label = "resultPanelOffset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 카메라 프리뷰 영역 (동적으로 축소됨)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = animatedHeight)
        ) {
            // 전체 화면 카메라 프리뷰
            CameraPreview(
                isFlashEnabled = uiState.isFlashEnabled,
                onQrCodeDetected = { content, bounds ->
                    // 스캔 중이고, 중복 확인도 없고, 현재 스캔된 결과도 없을 때만 QR 인식
                    if (uiState.isScanning && 
                        uiState.duplicateConfirmation == null && 
                        uiState.scannedResult == null) {
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

            // QR 코드 감지 오버레이 (bounds가 있으면 항상 표시)
            // 리컴포지션 최적화: 별도 StateFlow에서 수집한 bounds 사용
            detectedBounds?.let { bounds ->
                QrOverlay(
                    bounds = bounds,
                    isSuccess = uiState.isSuccess
                )
            }

            // 포커스 애니메이션
            if (uiState.isFocusing) {
                uiState.focusPoint?.let { point ->
                    FocusIndicator(point = point)
                }
            }

            // 상단 UI - 상태바 inset 적용
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(statusBarPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 뒤로가기, 안내 문구, 플래시 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 뒤로가기 버튼
                    CircularButton(
                        onClick = onBackClick,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.qr_back)
                    )

                    // 중앙 안내 문구
                    Text(
                        text = if (uiState.isSaving) {
                            stringResource(R.string.qr_saving)
                        } else if (uiState.savedTickets.isNotEmpty()) {
                            stringResource(R.string.qr_scan_next_guide)
                        } else {
                            stringResource(R.string.qr_scan_guide)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // 플래시 버튼
                    CircularButton(
                        onClick = { viewModel.onEvent(QrScanEvent.ToggleFlash) },
                        icon = if (uiState.isFlashEnabled) {
                            Icons.Default.FlashOn
                        } else {
                            Icons.Default.FlashOff
                        },
                        contentDescription = stringResource(
                            if (uiState.isFlashEnabled) {
                                R.string.qr_flash_on
                            } else {
                                R.string.qr_flash_off
                            }
                        )
                    )
                }
            }

            // 스낵바 호스트 (카메라 영역 내)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }

        // 하단 결과 패널 (애니메이션으로 나타남)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset { IntOffset(0, with(density) { animatedOffset.toPx().toInt() }) }
        ) {
            if (hasAnyTickets) {
                    SavedTicketsPager(
                    scannedResult = uiState.scannedResult,
                    winningDetail = uiState.currentWinningDetail,
                    tickets = uiState.savedTickets,
                    currentRound = uiState.currentRound,
                    duplicateConfirmation = uiState.duplicateConfirmation,
                    onConfirmDuplicate = { viewModel.onEvent(QrScanEvent.ConfirmDuplicateSave) },
                    onCancelDuplicate = { viewModel.onEvent(QrScanEvent.CancelDuplicateSave) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .padding(vertical = 16.dp)
                        .onSizeChanged { size ->
                            val height = with(density) { size.height.toDp().value.toInt() }
                            if (measuredHeight != height) {
                                measuredHeight = height
                            }
                        }
                )
            }
        }
    }
}

private fun performVibration(
    context: Context,
    durationMillis: Long
) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator?.hasVibrator() != true) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMillis)
    }
}

@Composable
private fun CircularButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun FocusIndicator(point: Offset) {
    var animationStarted by remember { mutableStateOf(false) }
    val highlightColor = MaterialTheme.colorScheme.onPrimary

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
            color = highlightColor.copy(alpha = currentAlpha * 0.8f),
            radius = currentRadius,
            center = point,
            style = Stroke(width = 3f)
        )

        // 내부 원 (더 작은 원)
        drawCircle(
            color = highlightColor.copy(alpha = currentAlpha * 0.4f),
            radius = currentRadius * 0.7f,
            center = point,
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun QrOverlay(
    bounds: QrCodeBounds,
    isSuccess: Boolean
) {
    // 박스가 처음 나타날 때를 추적 (스케일 애니메이션용)
    var isFirstAppearance by remember { mutableStateOf(true) }
    var animationStarted by remember { mutableStateOf(false) }
    
    // 소스 이미지 좌표계에서 corner point를 부드럽게 이동시키기 위한 state
    val animatedSourceCorners = remember { mutableStateListOf<Offset>() }
    
    // bounds의 고유 식별자 (새로운 QR 감지 판별용)
    var lastBoundsId by remember { mutableStateOf<String?>(null) }
    val currentBoundsId = "${bounds.cornerPoints.firstOrNull()?.x}_${bounds.cornerPoints.firstOrNull()?.y}"

    LaunchedEffect(bounds) {
        val targetSourceCorners = bounds.cornerPoints.take(4).map { Offset(it.x, it.y) }
        
        // 새로운 QR 코드 감지 판별
        val isNewQrDetection = lastBoundsId == null || 
            (lastBoundsId != currentBoundsId && animatedSourceCorners.isEmpty())
        
        if (isNewQrDetection) {
            // 새 QR 감지: 스케일 애니메이션 활성화
            isFirstAppearance = true
            animatedSourceCorners.clear()
            animatedSourceCorners.addAll(targetSourceCorners)
            lastBoundsId = currentBoundsId
            
            // 스케일 애니메이션 시작
            animationStarted = false
            kotlinx.coroutines.delay(50)
            animationStarted = true
        } else {
            // 기존 QR 추적: 위치만 부드럽게 이동
            isFirstAppearance = false
            animationStarted = true
            
            if (animatedSourceCorners.size != 4) {
                animatedSourceCorners.clear()
                animatedSourceCorners.addAll(targetSourceCorners)
            } else {
                // 위치를 부드럽게 이동 (250ms)
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
        }
    }

    // 스케일 애니메이션 (첫 등장 시에만)
    val animationProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = if (isFirstAppearance) {
            tween(
                durationMillis = QrOverlayStyle.animationDuration,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "qr_box_scale_animation"
    )

    val qrPrimary = MaterialTheme.colorScheme.primary
    val successColor = qrPrimary
    val cornerColor = if (isSuccess) successColor else qrPrimary.copy(alpha = 0.8f)
    val overlayAlpha = if (isFirstAppearance) animationProgress else 1f

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

            // 스케일 애니메이션 계산 (첫 등장 시에만)
            val initialScale = QrOverlayStyle.initialScaleRatio
            val currentScale = if (isFirstAppearance) {
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
            val checkProgress = if (isFirstAppearance) animationProgress else 1f
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
                    color = successColor.copy(alpha = checkAlpha),
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
            // 카메라 리소스 정리 (UI 깨짐/렉 방지)
            try {
                cameraProviderFuture.get()?.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            executor.shutdown()
            barcodeScanner.close()
        }
    }

    LaunchedEffect(isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                // COMPATIBLE 모드(TextureView) 사용: Compose 애니메이션과 동기화
                // PERFORMANCE 모드(SurfaceView)는 별도 하드웨어 레이어에서 렌더링되어
                // slideOutVertically 같은 Compose 애니메이션이 적용되지 않음
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val preview = Preview.Builder()
                .setResolutionSelector(
                    androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                        .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .build()
                )
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                        .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy, onQrCodeDetected, onBoundsUpdate)
                    }
                }

            try {
                cameraProviderFuture.get().let { provider ->
                    provider.unbindAll()
                    camera = provider.bindToLifecycle(
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
                // 로또 QR 코드를 찾아서 수집
                val lottoQrCodes = barcodes
                    .filter { it.format == Barcode.FORMAT_QR_CODE }
                    .mapNotNull { barcode ->
                        val content = barcode.rawValue ?: return@mapNotNull null
                        if (!content.contains("?v=")) return@mapNotNull null
                        
                        val points = barcode.cornerPoints ?: return@mapNotNull null
                        if (points.size < 4) return@mapNotNull null
                        
                        val cornerPoints = points.map { PointF(it.x.toFloat(), it.y.toFloat()) }
                        
                        // QR 코드의 면적 계산 (카메라와의 거리 추정용)
                        val minX = cornerPoints.minOf { it.x }
                        val maxX = cornerPoints.maxOf { it.x }
                        val minY = cornerPoints.minOf { it.y }
                        val maxY = cornerPoints.maxOf { it.y }
                        val area = (maxX - minX) * (maxY - minY)
                        
                        // 화면 중앙과의 거리 계산
                        val centerX = (minX + maxX) / 2f
                        val centerY = (minY + maxY) / 2f
                        val imageCenterX = imageProxy.width / 2f
                        val imageCenterY = imageProxy.height / 2f
                        val distanceFromCenter = kotlin.math.sqrt(
                            (centerX - imageCenterX) * (centerX - imageCenterX) +
                            (centerY - imageCenterY) * (centerY - imageCenterY)
                        )
                        
                        Triple(
                            content,
                            QrCodeBounds(
                                cornerPoints = cornerPoints,
                                sourceImageWidth = imageProxy.width,
                                sourceImageHeight = imageProxy.height,
                                rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            ),
                            Pair(area, distanceFromCenter)
                        )
                    }
                
                // 여러 QR이 감지된 경우: 면적이 크고(가까운) + 중앙에 가까운 QR 선택
                lottoQrCodes.maxByOrNull { (_, _, metrics) ->
                    val (area, distanceFromCenter) = metrics
                    // 면적에 높은 가중치, 중앙 거리에 낮은 패널티
                    area - (distanceFromCenter * 0.3f)
                }?.let { (content, bounds, _) ->
                    // 가장 적합한 QR만 처리
                    onBoundsUpdate(bounds)
                    onQrCodeDetected(content, bounds)
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

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun SavedTicketsPager(
    scannedResult: com.enso.qrscan.parser.LottoTicketInfo?,
    winningDetail: TicketWinningDetail?,
    tickets: List<SavedTicketSummary>,
    currentRound: Int,
    duplicateConfirmation: DuplicateConfirmation?,
    onConfirmDuplicate: () -> Unit,
    onCancelDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val cardWidth = 300.dp
    
    // 스캔된 결과를 임시 아이템으로 변환
    val scannedTicketSummary = remember(scannedResult, winningDetail, currentRound) {
        scannedResult?.let { ticketInfo ->
            SavedTicketSummary(
                round = ticketInfo.round,
                gameCount = ticketInfo.games.size,
                games = ticketInfo.games.mapIndexed { index, gameInfo ->
                    ScannedGameSummary(
                        gameLabel = ('A' + index).toString(),
                        numbers = gameInfo.numbers,
                        isAuto = gameInfo.isAuto
                    )
                },
                timestamp = System.currentTimeMillis(),
                winningNumbers = winningDetail?.winningNumbers,
                bonusNumber = winningDetail?.bonusNumber,
                winningResults = winningDetail?.gameResults,
                winningCheckFailed = winningDetail == null && ticketInfo.round <= currentRound,
                firstPrizeAmount = winningDetail?.firstPrizeAmount,
                drawDate = winningDetail?.drawDate
            )
        }
    }
    
    // 스캔된 아이템을 맨 앞에 추가한 전체 리스트
    val allItems = remember(scannedTicketSummary, tickets) {
        if (scannedTicketSummary != null) {
            listOf(scannedTicketSummary) + tickets
        } else {
            tickets
        }
    }
    
    // 이전 티켓 개수를 추적하여 새 아이템 추가 감지
    var previousSize by remember { mutableStateOf(0) }
    
    LaunchedEffect(allItems.size) {
        if (allItems.isNotEmpty()) {
            // 첫 아이템은 애니메이션 없이 즉시 이동 (패널 슬라이드업과 동시)
            if (previousSize == 0) {
                listState.scrollToItem(0)
            } 
            // 이후 추가되는 아이템은 부드럽게 애니메이션 스크롤
            else if (allItems.size > previousSize) {
                listState.animateScrollToItem(0)
            }
            previousSize = allItems.size
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        // constraints를 사용하여 컨테이너 너비 기반 중앙 정렬 패딩 계산
        // 린터가 constraints 사용을 감지하지 못하지만, 실제로는 사용 중
        val containerWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        val sidePadding = ((containerWidth - cardWidth) / 2).coerceAtLeast(0.dp)
        val snapLayoutInfoProvider = remember(listState) {
            object : SnapLayoutInfoProvider by SnapLayoutInfoProvider(
                listState,
                SnapPosition.Center
            ) {
                override fun calculateApproachOffset(
                    velocity: Float,
                    decayOffset: Float
                ): Float = 0f
            }
        }
        val decayAnimationSpec = rememberSplineBasedDecay<Float>()
        val snapFlingBehavior = remember(snapLayoutInfoProvider, decayAnimationSpec) {
            snapFlingBehavior(
                snapLayoutInfoProvider = snapLayoutInfoProvider,
                decayAnimationSpec = decayAnimationSpec,
                snapAnimationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        LazyRow(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(horizontal = sidePadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items(
                items = allItems,
                key = { it.timestamp }
            ) { ticket ->
                val isScannedItem = scannedTicketSummary != null && 
                                   ticket.timestamp == scannedTicketSummary.timestamp
                QrTicketCard(
                    ticket = ticket,
                    currentRound = currentRound,
                    duplicateConfirmation = if (isScannedItem) duplicateConfirmation else null,
                    onConfirmDuplicate = onConfirmDuplicate,
                    onCancelDuplicate = onCancelDuplicate,
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 300),
                            placementSpec = androidx.compose.animation.core.spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .width(cardWidth)
                )
            }
        }
    }
}

@Composable
private fun QrTicketCard(
    ticket: SavedTicketSummary,
    currentRound: Int,
    duplicateConfirmation: DuplicateConfirmation?,
    onConfirmDuplicate: () -> Unit,
    onCancelDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winningNumbers = remember(ticket.winningNumbers) { ticket.winningNumbers.orEmpty() }
    val isDrawComplete = remember(currentRound, ticket.round) { currentRound >= ticket.round }
    val isPrizeExpired = remember(ticket.drawDate) { 
        ticket.drawDate?.let { isPrizeExpired(it) } ?: false 
    }
    val drawScheduleInfo = remember(isDrawComplete, ticket.drawDate) {
        if (!isDrawComplete) {
            ticket.drawDate?.let { calculateDrawSchedule(it) }
        } else {
            null
        }
    }
    val highlightShape = MaterialTheme.shapes.small
    val qrPrimary = MaterialTheme.colorScheme.primary
    val qrPrimaryContainer = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 중복 확인 배너 (빠른 애니메이션으로 출렁거림 최소화)
            AnimatedVisibility(
                visible = duplicateConfirmation != null,
                enter = androidx.compose.animation.expandVertically(
                    animationSpec = tween(durationMillis = 200, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 150)),
                exit = androidx.compose.animation.shrinkVertically(
                    animationSpec = tween(durationMillis = 200, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                if (duplicateConfirmation != null) {
                    DuplicateConfirmationBanner(
                        round = duplicateConfirmation.existingRound,
                        onConfirm = onConfirmDuplicate,
                        onSkip = onCancelDuplicate
                    )
                }
            }
            
            // 티켓 내용
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.qr_saved_ticket_round, ticket.round),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (drawScheduleInfo != null) {
                    val scheduleDateText = stringResource(
                        R.string.qr_draw_schedule_date_format,
                        drawScheduleInfo.dateText
                    )
                    val scheduleRemainingText = if (drawScheduleInfo.hoursOnly) {
                        stringResource(
                            R.string.qr_draw_schedule_hours_only_format,
                            drawScheduleInfo.hours
                        )
                    } else {
                        stringResource(
                            R.string.qr_draw_schedule_remaining_format,
                            drawScheduleInfo.days,
                            drawScheduleInfo.hours
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = scheduleDateText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = scheduleRemainingText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (!isDrawComplete) {
                    Text(
                        text = stringResource(R.string.qr_result_not_drawn),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (winningNumbers.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        winningNumbers.forEach { number ->
                            QrHighlightedSmallLottoBall(
                                number = number,
                                isMatched = true
                            )
                        }
                    }
                }
            }

            ticket.games.forEachIndexed { index, game ->
                val gameResult = ticket.winningResults?.firstOrNull {
                    it.gameLabel == game.gameLabel
                }
                val resultText = gameResultText(
                    isDrawComplete = isDrawComplete,
                    isPrizeExpired = isPrizeExpired,
                    winningCheckFailed = ticket.winningCheckFailed,
                    gameResult = gameResult,
                    firstPrizeAmount = ticket.firstPrizeAmount
                )
                val isWinning = gameResult?.rank in 1..5 && !isPrizeExpired

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .background(
                            color = if (isWinning) {
                                qrPrimaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            },
                            shape = highlightShape
                        )
                        .border(
                            width = if (isWinning) 1.dp else 0.dp,
                            color = if (isWinning) {
                                qrPrimary.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            },
                            shape = highlightShape
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = game.gameLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = qrPrimary,
                        modifier = Modifier.width(16.dp)
                    )

                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        game.numbers.forEach { number ->
                            QrHighlightedSmallLottoBall(
                                number = number,
                                isMatched = winningNumbers.contains(number)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resultText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isWinning) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isWinning) {
                                MaterialTheme.typography.labelMedium.fontSize * 1.1f
                            } else {
                                MaterialTheme.typography.labelMedium.fontSize
                            },
                            color = if (isWinning) {
                                qrPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.widthIn(min = 64.dp)
                        )
                    }
                }

            }
            }
        }
    }
}

@Composable
private fun DuplicateConfirmationBanner(
    round: Int,
    onConfirm: () -> Unit,
    onSkip: () -> Unit
) {
    val lottoColors = LottoTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(lottoColors.warningContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.qr_duplicate_prompt_title, round),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = lottoColors.onWarningContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            text = stringResource(R.string.qr_duplicate_prompt_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.qr_duplicate_action_skip),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            androidx.compose.material3.Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.qr_duplicate_action_save),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun gameResultText(
    isDrawComplete: Boolean,
    isPrizeExpired: Boolean,
    winningCheckFailed: Boolean,
    gameResult: GameWinningInfo?,
    firstPrizeAmount: Long?
): String {
    return when {
        isPrizeExpired -> stringResource(R.string.qr_result_expired)
        !isDrawComplete -> stringResource(R.string.qr_result_not_drawn)
        winningCheckFailed || gameResult == null -> stringResource(R.string.qr_result_unavailable)
        gameResult.rank == 1 -> firstPrizeAmount?.let { formatPrizeAmount(it) }
            ?: stringResource(R.string.qr_result_rank_format, 1)
        gameResult.rank == 2 || gameResult.rank == 3 -> {
            stringResource(R.string.qr_result_rank_format, gameResult.rank)
        }
        gameResult.rank == 4 -> stringResource(R.string.qr_result_prize_4th)
        gameResult.rank == 5 -> stringResource(R.string.qr_result_prize_5th)
        else -> stringResource(R.string.qr_result_lose)
    }
}

@Composable
private fun QrHighlightedSmallLottoBall(
    number: Int,
    isMatched: Boolean,
    modifier: Modifier = Modifier
) {
    val lottoColors = LottoTheme.colors
    val backgroundColor = if (isMatched) {
        qrLottoBallColor(number)
    } else {
        lottoColors.chipBackground
    }
    val contentColor = if (isMatched) {
        lottoColors.ballTextColor
    } else {
        lottoColors.textSecondary
    }
    val borderColor = if (isMatched) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = modifier
            .size(26.dp)
            .background(backgroundColor, shape = CircleShape)
            .border(width = 1.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun qrLottoBallColor(number: Int): Color {
    return getLottoBallColor(number, LottoTheme.colors)
}

private fun isPrizeExpired(drawDate: Date): Boolean {
    val calendar = Calendar.getInstance().apply { time = drawDate }
    calendar.add(Calendar.YEAR, 1)
    return Date().after(calendar.time)
}

private fun formatPrizeAmount(amount: Long): String {
    val billions = amount / 100_000_000
    val remainder = amount % 100_000_000
    val tenThousands = remainder / 10_000

    return if (billions > 0) {
        if (tenThousands > 0) {
            val formatter = NumberFormat.getInstance(Locale.KOREA)
            "${billions}억 ${formatter.format(tenThousands)}만원"
        } else {
            "${billions}억원"
        }
    } else if (tenThousands > 0) {
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        "${formatter.format(tenThousands)}만원"
    } else {
        "${amount}원"
    }
}

private fun calculateDrawSchedule(drawDate: Date): DrawScheduleInfo {
    val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    val now = Date()
    val remainingMillis = (drawDate.time - now.time).coerceAtLeast(0L)
    val totalHours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val days = (totalHours / 24).toInt()
    val hours = (totalHours % 24).toInt()
    val hoursOnly = days <= 0
    return DrawScheduleInfo(
        dateText = formatter.format(drawDate),
        days = days,
        hours = hours,
        hoursOnly = hoursOnly
    )
}

@Composable
private fun MinimizedSavedList(
    count: Int,
    isExpanded: Boolean,
    savedTickets: List<SavedTicketSummary>,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth(0.9f),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val overlayTextColor = MaterialTheme.colorScheme.inverseOnSurface

            // 요약 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.qr_tickets_saved_count, count),
                    style = MaterialTheme.typography.bodyLarge,
                    color = overlayTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowUp
                    },
                    contentDescription = stringResource(
                        if (isExpanded) {
                            R.string.qr_saved_list_collapse
                        } else {
                            R.string.qr_saved_list_expand
                        }
                    ),
                    tint = overlayTextColor
                )
            }

            // 확장된 목록
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                savedTickets.forEach { ticket ->
                    androidx.compose.material3.HorizontalDivider(
                        color = overlayTextColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.qr_ticket_summary_format, ticket.round, ticket.gameCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = overlayTextColor.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
