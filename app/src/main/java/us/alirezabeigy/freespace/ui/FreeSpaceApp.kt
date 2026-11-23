package us.alirezabeigy.freespace.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import us.alirezabeigy.freespace.R
import us.alirezabeigy.freespace.ui.screens.optimizer.FreeSpaceScreen
import us.alirezabeigy.freespace.ui.screens.optimizer.OptimizerQueueScreen
import us.alirezabeigy.freespace.ui.screens.optimizer.OptimizerReviewScreen
import us.alirezabeigy.freespace.ui.screens.settings.AboutScreen
import us.alirezabeigy.freespace.ui.screens.settings.LicensesScreen
import us.alirezabeigy.freespace.ui.screens.settings.SettingsScreen
import us.alirezabeigy.freespace.utils.ExpressiveSpatialSpring
import us.alirezabeigy.freespace.utils.mediaReadPermissions
import us.alirezabeigy.freespace.viewmodel.OptimizerViewModel
import kotlinx.coroutines.CancellationException

private enum class SettingsDestination {
    MAIN, ABOUT, LICENSES
}

private enum class FreeSpacePane {
    LIBRARY, QUEUE, REVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeSpaceApp(optimizerViewModel: OptimizerViewModel) {
    val optimizerState by optimizerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window

    var currentSettingsDestination by remember { mutableStateOf<SettingsDestination?>(null) }
    var freeSpacePane by remember { mutableStateOf(FreeSpacePane.LIBRARY) }

    DisposableEffect(Unit) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val isSettingsOpen = currentSettingsDestination != null
    val freeSpaceNested = freeSpacePane != FreeSpacePane.LIBRARY
    val canHandleBack = isSettingsOpen ||
        freeSpaceNested ||
        optimizerState.isProcessingQueue

    var backGestureProgress by remember { mutableFloatStateOf(0f) }
    var backGestureActive by remember { mutableStateOf(false) }
    var backSwipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    val predictiveBackProgress by animateFloatAsState(
        targetValue = backGestureProgress,
        animationSpec = if (backGestureActive) snap() else ExpressiveSpatialSpring,
        label = "predictiveBackProgress"
    )

    fun performBackNavigation() {
        if (isSettingsOpen) {
            currentSettingsDestination = when (currentSettingsDestination) {
                SettingsDestination.MAIN -> null
                SettingsDestination.LICENSES -> SettingsDestination.ABOUT
                else -> SettingsDestination.MAIN
            }
        } else {
            when (freeSpacePane) {
                FreeSpacePane.REVIEW -> freeSpacePane = FreeSpacePane.QUEUE
                FreeSpacePane.QUEUE -> freeSpacePane = FreeSpacePane.LIBRARY
                FreeSpacePane.LIBRARY -> {
                    if (optimizerState.isProcessingQueue) {
                        optimizerViewModel.cancelQueue()
                    }
                }
            }
        }
    }

    PredictiveBackHandler(enabled = canHandleBack) { progress ->
        backGestureActive = true
        try {
            progress.collect { backEvent ->
                backSwipeEdge = backEvent.swipeEdge
                backGestureProgress = backEvent.progress
            }
            backGestureProgress = 0f
            backGestureActive = false
            performBackNavigation()
        } catch (e: CancellationException) {
            backGestureActive = false
            backGestureProgress = 0f
            throw e
        }
    }

    val freeSpacePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        optimizerViewModel.onPermissionResult()
    }

    val optimizerDeleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            optimizerViewModel.onOriginalDeletedForReview()
        } else {
            optimizerViewModel.cancelPendingOriginalReplace()
        }
    }

    LaunchedEffect(Unit) {
        optimizerViewModel.refreshPermissionStatus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = predictiveBackProgress
                if (p > 0f) {
                    val scale = 1f - (0.08f * p)
                    scaleX = scale
                    scaleY = scale
                    val direction = if (backSwipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
                    translationX = direction * size.width * 0.08f * p
                    alpha = 1f - (0.12f * p)
                    shadowElevation = 8f * p
                    shape = RoundedCornerShape(28.dp * p)
                    clip = p > 0.01f
                }
            }
    ) {
        AnimatedContent(
            targetState = if (currentSettingsDestination != null) "settings" else "main",
            transitionSpec = {
                if (targetState == "settings") {
                    slideInVertically { h -> h } + fadeIn() togetherWith fadeOut()
                } else {
                    fadeIn() togetherWith slideOutVertically { h -> h }
                }
            },
            label = "TopLevelContent"
        ) { destination ->
            when (destination) {
                "settings" -> {
                    AnimatedContent(
                        targetState = currentSettingsDestination,
                        transitionSpec = {
                            if (targetState != SettingsDestination.MAIN) {
                                slideInHorizontally { w -> w } + fadeIn() togetherWith
                                    slideOutHorizontally { w -> -w } + fadeOut()
                            } else {
                                slideInHorizontally { w -> -w } + fadeIn() togetherWith
                                    slideOutHorizontally { w -> w } + fadeOut()
                            }
                        },
                        label = "SettingsFlow"
                    ) { settingsDest ->
                        when (settingsDest) {
                            SettingsDestination.MAIN -> SettingsScreen(
                                onBack = { currentSettingsDestination = null },
                                onNavigateToAbout = {
                                    currentSettingsDestination = SettingsDestination.ABOUT
                                }
                            )
                            SettingsDestination.ABOUT -> AboutScreen(
                                onBack = { currentSettingsDestination = SettingsDestination.MAIN },
                                onOpenLicenses = {
                                    currentSettingsDestination = SettingsDestination.LICENSES
                                }
                            )
                            SettingsDestination.LICENSES -> LicensesScreen(
                                onBack = { currentSettingsDestination = SettingsDestination.ABOUT }
                            )
                            null -> {}
                        }
                    }
                }
                else -> {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                ),
                                actions = {
                                    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
                                    IconButton(onClick = {
                                        haptics.performHapticFeedback(
                                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                                        )
                                        currentSettingsDestination = SettingsDestination.MAIN
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = stringResource(R.string.settings_content_desc),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (freeSpacePane) {
                                FreeSpacePane.LIBRARY -> FreeSpaceScreen(
                                    state = optimizerState,
                                    onRequestPermission = {
                                        val perms = mediaReadPermissions()
                                        if (perms.isEmpty()) {
                                            optimizerViewModel.onPermissionResult()
                                        } else {
                                            freeSpacePermissionLauncher.launch(perms)
                                        }
                                    },
                                    onRefresh = { optimizerViewModel.scanLibrary() },
                                    onToggle = { optimizerViewModel.toggleSelection(it) },
                                    onSelectAll = { optimizerViewModel.selectAllVisible() },
                                    onClearSelection = { optimizerViewModel.clearSelection() },
                                    onQualityModeSelected = {
                                        optimizerViewModel.applyQualityMode(it)
                                    },
                                    onCompressSelected = {
                                        optimizerViewModel.enqueueSelected()
                                        freeSpacePane = FreeSpacePane.QUEUE
                                    },
                                    onOpenQueue = { freeSpacePane = FreeSpacePane.QUEUE },
                                    onExcludeFile = { optimizerViewModel.excludeFile(it) },
                                    onExcludeFolder = { optimizerViewModel.excludeFolder(it) },
                                    onClearExclusions = { optimizerViewModel.clearExclusions() }
                                )
                                FreeSpacePane.QUEUE -> OptimizerQueueScreen(
                                    state = optimizerState,
                                    onBack = { freeSpacePane = FreeSpacePane.LIBRARY },
                                    onCancel = { optimizerViewModel.cancelQueue() },
                                    onClear = {
                                        optimizerViewModel.clearQueue()
                                        freeSpacePane = FreeSpacePane.LIBRARY
                                    },
                                    onReview = { id ->
                                        optimizerViewModel.openReview(id)
                                        freeSpacePane = FreeSpacePane.REVIEW
                                    },
                                    onRetry = { id -> optimizerViewModel.retryFailed(id) }
                                )
                                FreeSpacePane.REVIEW -> {
                                    val reviewItem = optimizerState.reviewingItem
                                    if (reviewItem != null) {
                                        OptimizerReviewScreen(
                                            item = reviewItem,
                                            onBack = {
                                                optimizerViewModel.closeReview()
                                                freeSpacePane = FreeSpacePane.QUEUE
                                            },
                                            onKeepBoth = {
                                                optimizerViewModel.keepBothForReview()
                                                freeSpacePane = FreeSpacePane.QUEUE
                                            },
                                            onDeleteOriginal = {
                                                optimizerViewModel.deleteOriginalForReview(context) { sender ->
                                                    optimizerDeleteLauncher.launch(
                                                        IntentSenderRequest.Builder(sender).build()
                                                    )
                                                }
                                            },
                                            onDeleteCompressed = {
                                                optimizerViewModel.deleteCompressedForReview()
                                                freeSpacePane = FreeSpacePane.QUEUE
                                            }
                                        )
                                    } else {
                                        freeSpacePane = FreeSpacePane.QUEUE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
