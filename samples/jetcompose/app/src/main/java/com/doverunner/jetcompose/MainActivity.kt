package com.doverunner.jetcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.doverunner.widevine.model.ContentData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    androidx.compose.material3.MaterialTheme {
        androidx.compose.material3.Scaffold(
            topBar = {
                androidx.compose.material3.CenterAlignedTopAppBar(
                    title = { androidx.compose.material3.Text(titleFor(navController)) }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable(
                    route = "player/{DrContentData}",
                    arguments = listOf(navArgument("DrContentData") { type = AssetParamType() })
                ) { entry ->
                    entry.arguments?.getParcelable<ContentData>("DrContentData")?.let { data ->
                        PlayerScreen(data, navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun titleFor(navController: androidx.navigation.NavHostController): String {
    val route = navController.currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry).value?.destination?.route
    return when {
        route == "home" -> "JetCompose"
        route?.startsWith("player/") == true -> "Player"
        else -> ""
    }
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp()
}