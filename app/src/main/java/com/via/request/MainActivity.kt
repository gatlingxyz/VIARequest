package com.via.request

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.via.request.ui.theme.ButtonOutline
import com.via.request.ui.theme.DarkGreen
import com.via.request.ui.theme.DarkerGreen
import com.via.request.ui.theme.LightBlue
import com.via.request.ui.theme.LightGreen
import com.via.request.ui.theme.LightRed
import com.via.request.ui.theme.VIARequestTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@Serializable sealed interface RequestDestination {
    @Serializable data object Home: RequestDestination
    @Serializable data class RequestDetails(
        val headline: String,
        val message: String,
    ): RequestDestination
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            }

            VIARequestTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        ViaSnackbarHost(
                            snackbarHostState = snackbarHostState,
                            requestState = requestState
                        )
                    },
                ) { innerPadding ->

                    val destination by viewModel.destinationFlow
                        .collectAsStateWithLifecycle()

                    LaunchedEffect(destination) {
                        navController.navigate(destination)
                    }

                    NavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = RequestDestination.Home
                    ) {
                        composable<RequestDestination.Home> {
                            HomeScreen(
                                createNewRequest = {
                                    viewModel.onEvent(RequestEvent.CreateNewRequest)
                                }
                            )
                        }

                        composable<RequestDestination.RequestDetails> {
                            val route = it.toRoute<RequestDestination.RequestDetails>()

                            val request = Request(
                                headline = route.headline,
                                message = route.message
                            )

                            RequestScreen(
                                headline = request.headline,
                                message = request.message,
                                approveRequest = {
                                    viewModel.onEvent(RequestEvent.ApproveRequest(request))
                                },
                                rejectRequest = {
                                    viewModel.onEvent(RequestEvent.RejectRequest(request))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SnackbarColors(
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

            }
        )
    }
}

@Preview(showBackground = true, device = Devices.PHONE, showSystemUi = true)
@Composable
fun RequestPreview() {
    VIARequestTheme() {
        RequestScreen(
            headline = "",
            message = "",
            approveRequest = {},
            rejectRequest = {}
        )
    }
}

@Composable
fun HomeScreen(
    createNewRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = LightBlue)
            .padding(16.dp)
        ,
    ) {
        Text("Home",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
        )
        Spacer(Modifier.size(48.dp))
        Image(
            painterResource(R.drawable.via_logo),
            contentDescription = "VIA Logo",
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(
                    color = Color.White,
                    shape = CircleShape,
                )
                .size(150.dp)
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
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}

@Composable
fun ViaElevatedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        onClick = onClick,
        border = BorderStroke(1.dp, ButtonOutline),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 3.dp
        ),
        content = content
    )
}

@Composable
fun RequestScreen(
    headline: String,
    message: String,
    approveRequest: () -> Unit,
    rejectRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkGreen)
            .padding(16.dp)
        ,
    ) {
        Text("New Request",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.size(48.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DarkerGreen
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(headline,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
            }
        }
        Row() {
            ViaElevatedButton(
                onClick = rejectRequest
            ) {
                Text("Reject")
            }
            ViaElevatedButton(
                onClick = approveRequest
            ) {
                Text("Slide to approve")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SlidingButton() {

}

/**
 *
 *     internal val ColorScheme.defaultButtonColors: ButtonColors
 *         get() {
 *             return defaultButtonColorsCached
 *                 ?: ButtonColors(
 *                         containerColor = fromToken(FilledButtonTokens.ContainerColor),
 *                         contentColor = fromToken(FilledButtonTokens.LabelTextColor),
 *                         disabledContainerColor =
 *                             fromToken(FilledButtonTokens.DisabledContainerColor)
 *                                 .copy(alpha = FilledButtonTokens.DisabledContainerOpacity),
 *                         disabledContentColor =
 *                             fromToken(FilledButtonTokens.DisabledLabelTextColor)
 *                                 .copy(alpha = FilledButtonTokens.DisabledLabelTextOpacity),
 *                     )
 *                     .also { defaultButtonColorsCached = it }
 *         }
 */