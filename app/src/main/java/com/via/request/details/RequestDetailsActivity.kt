@file:OptIn(ExperimentalMaterial3Api::class)

package com.via.request.details

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.via.request.R
import com.via.request.ui.composables.SliderButton
import com.via.request.ui.composables.ViaElevatedButton
import com.via.request.models.Request
import com.via.request.ui.theme.DarkBlue
import com.via.request.ui.theme.DarkerGreen
import com.via.request.ui.theme.GreenBlue
import com.via.request.ui.theme.LightBlue
import com.via.request.ui.theme.LightGreen
import com.via.request.ui.theme.LightRed
import com.via.request.ui.theme.VIARequestTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.serialization.Serializable

@Serializable sealed interface RequestDestination {
    @Serializable data object Home: RequestDestination
    @Serializable data object RequestDetails: RequestDestination
}

@AndroidEntryPoint
class RequestDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val viewModel: RequestDetailViewModel by viewModels()
            val navController = rememberNavController()

            val snackbarHostState = remember { SnackbarHostState() }

            val requestState by viewModel.requestState.collectAsStateWithLifecycle(null)

            LaunchedEffect(requestState) {
                requestState?.let {
                    when (it) {
                        is RequestState.Loading -> null
                        is RequestState.Approved -> it.message
                        is RequestState.Rejected -> it.reason
                        is RequestState.Error -> it.message
                    }?.let { message ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Indefinite,
                        )
                    }
                }
            }

            VIARequestTheme {

                FlowCollector(viewModel.destinationFlow) {
                    navController.navigate(it)
                }

                NavHost(
                    modifier = Modifier,
                    navController = navController,
                    startDestination = RequestDestination.Home
                ) {
                    composable<RequestDestination.Home> {
                        HomeScreen(
                            snackbarHost = {
                                ViaSnackbarHost(
                                    snackbarHostState = snackbarHostState,
                                    requestState = requestState
                                )
                            },
                            createNewRequest = {
                                viewModel.onEvent(RequestDetailsEvent.CreateNewRequest)
                            }
                        )
                    }

                    composable<RequestDestination.RequestDetails> {

                        LaunchedEffect(it) {
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }

                        RequestScreen(
                            onEvent = {
                                viewModel.onEvent(it)
                            }
                        )

                        if (requestState is RequestState.Loading) {
                            val state = requestState as RequestState.Loading
                            val message = if (state.approving) "Approving request..." else "Rejecting request..."

                            LoadingDialog(
                                loadingMessage = message
                            )
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun LoadingDialog(
    loadingMessage: String,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(loadingMessage, style = MaterialTheme.typography.headlineMedium)
                LinearProgressIndicator(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

private data class SnackbarColors(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
fun ViaSnackbarHost(
    snackbarHostState: SnackbarHostState,
    requestState: RequestState?,
) {

    val snackbarColors = remember(requestState) {
        when(requestState) {
            is RequestState.Approved -> SnackbarColors(LightGreen, DarkerGreen)
            is RequestState.Rejected -> SnackbarColors(LightRed, DarkerGreen)
            is RequestState.Error -> SnackbarColors(Color.Red, Color.White)
            else -> SnackbarColors(Color.Transparent, Color.Transparent)
        }
    }

    SnackbarHost(snackbarHostState) { snackbarData ->
        Snackbar(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            containerColor = snackbarColors.containerColor,
            contentColor = snackbarColors.contentColor,
            actionContentColor = snackbarColors.contentColor,
            dismissAction = {
                IconButton(
                    onClick = {
                        snackbarData.dismiss()
                    }
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close snackbar", tint = snackbarColors.contentColor)
                }
            },
            content = {
                Text(snackbarData.visuals.message)
            }
        )
    }
}

@Preview(showBackground = true, device = Devices.PHONE, showSystemUi = true)
@Composable
fun HomePreview() {
    VIARequestTheme() {
        HomeScreen(
            createNewRequest = {

            },
            snackbarHost = {}
        )
    }
}

@Preview(showBackground = true, device = Devices.PHONE, showSystemUi = true)
@Composable
fun RequestPreview() {
    VIARequestTheme() {
        Scaffold() {
            Box(Modifier.consumeWindowInsets(it)) {
                RequestScreen(
                    onEvent = {}
                )
            }
        }

    }
}

@Composable
fun HomeScreen(
    snackbarHost: @Composable () -> Unit,
    createNewRequest: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = snackbarHost,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = LightBlue)
                .padding(16.dp)
            ,
        ) {
            Text("Home",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
            )
            Spacer(Modifier.size(48.dp))
            Image(
                painterResource(R.drawable.via_logo),
                contentDescription = "VIA Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = Color.White,
                        shape = CircleShape,
                    )
                    .padding(64.dp)
                    .size(125.dp)
            )
            Spacer(Modifier.size(48.dp))
            ViaElevatedButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ,
                onClick = createNewRequest,
            ) {
                Text("Create new request",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }


    }

}

@Composable
fun RequestScreen(
    onEvent: (event: RequestDetailsEvent) -> Unit,
) {
    var loading by remember {
        mutableStateOf(false)
    }

    var request by remember {
        mutableStateOf(Request(
            headline = "",
            message = ""
        ))
    }

    val sliderEnabled = remember(loading, request.headline, request.message) {
        !loading && request.headline.isNotEmpty() && request.message.isNotEmpty()
    }

    Scaffold(
        containerColor = GreenBlue
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                "New Request",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.size(48.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4F),
                colors = CardDefaults.cardColors(
                    containerColor = DarkerGreen
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    TextField(
                        request.headline,
                        modifier = Modifier
                            .fillMaxWidth(),
                        onValueChange = {
                            request = request.copy(
                                headline = it
                            )
                        },
                        placeholder = {
                            Text(
                                "Title of request", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall
                            .copy(
                                fontWeight = FontWeight.Bold
                            ),
                        colors = requestTextFieldColors()
                    )
                    Spacer(Modifier.size(8.dp))
                    TextField(
                        request.message,
                        onValueChange = {
                            request = request.copy(
                                message = it
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F),
                        placeholder = {
                            Text("Please describe your request.")
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = requestTextFieldColors()
                    )
                }
            }
            Spacer(Modifier.size(48.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViaElevatedButton(
                    modifier = Modifier.weight(1F),
                    enabled = sliderEnabled,
                    onClick = {
                        loading = true
                        onEvent(RequestDetailsEvent.RejectRequest(request))
                    }
                ) {
                    Text("Reject")
                }
                SliderButton(
                    modifier = Modifier
                        .weight(3F)
                        .alpha(if (sliderEnabled) 1F else 0.2F),
                    enabled = sliderEnabled,
                    originalLabel = "Slide to approve",
                    finishedLabel = "Approved",
                    onFinished = {
                        loading = true
                        onEvent(RequestDetailsEvent.ApproveRequest(request))
                    }
                )

            }


        }
    }
}

@Composable
fun <T> FlowCollector(flow: Flow<T>, collector: FlowCollector<T>) {
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collector)
        }
    }
}

@Composable
fun requestTextFieldColors() = TextFieldDefaults.colors(
    unfocusedTextColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
    unfocusedIndicatorColor = Color.White
)