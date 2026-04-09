package com.via.request

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.via.request.ui.theme.DarkGreen
import com.via.request.ui.theme.LightBlue
import com.via.request.ui.theme.VIARequestTheme
import kotlinx.serialization.Serializable

@Serializable sealed interface Destination {
    @Serializable data object Home: Destination
    @Serializable data object Request: Destination
    @Serializable data object RequestResult: Destination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()

            VIARequestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = Destination.Home
                    ) {
                        composable<Destination.Home> {
                            HomeScreen()
                        }

                        composable<Destination.Request> {

                        }

                        composable<Destination.RequestResult> {

                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PHONE, showSystemUi = true)
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = LightBlue)
            .padding(16.dp)
        ,
    ) {
        Text("Home",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
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
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ,
            onClick = {

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGreen
            )
        ) {
            Text("Create new request",
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}

@Preview(showBackground = true, device = Devices.PHONE, showSystemUi = true)
@Composable
fun RequestScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkGreen)
            .padding(16.dp)
        ,
    ) {
        Text("New Request",
            style = MaterialTheme.typography.titleLarge
            )
    }
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