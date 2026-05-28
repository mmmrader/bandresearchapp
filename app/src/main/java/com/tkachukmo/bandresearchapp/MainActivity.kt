package com.tkachukmo.bandresearchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tkachukmo.bandresearchapp.core.navigation.BandResearchNavGraph
import com.tkachukmo.bandresearchapp.core.navigation.Routes
import com.tkachukmo.bandresearchapp.core.notifications.NotificationMonitor
import com.tkachukmo.bandresearchapp.core.ui.theme.BandResearchAppTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    @Inject
    lateinit var notificationMonitor: NotificationMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BandResearchAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    val sessionStatus by supabaseClient.auth.sessionStatus.collectAsState()

                    // ВАЖЛИВО: Зберігаємо початковий маршрут у пам'яті системи (rememberSaveable),
                    // щоб при виборі файлу і перезапуску програми він не скидався!
                    var startRoute by rememberSaveable { mutableStateOf<String?>(null) }

                    LaunchedEffect(sessionStatus) {
                        if (sessionStatus is SessionStatus.Authenticated) {
                            notificationMonitor.start()
                        } else if (sessionStatus is SessionStatus.NotAuthenticated) {
                            notificationMonitor.stop()
                        }

                        if (startRoute == null) {
                            // Встановлюємо маршрут лише при найпершому холодному старті
                            when (sessionStatus) {
                                is SessionStatus.Authenticated -> startRoute = Routes.MAIN
                                is SessionStatus.NotAuthenticated -> startRoute = Routes.LOGIN
                                else -> {} // Чекаємо
                            }
                        } else {
                            // Якщо користувач натиснув кнопку "Вийти" під час роботи додатку
                            if (sessionStatus is SessionStatus.NotAuthenticated) {
                                startRoute = Routes.LOGIN
                            }
                        }
                    }

                    if (startRoute == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Тепер NavGraph створюється миттєво після повернення з вибору файлу
                        // і система автоматично відновлює екран "Кабінет гурту"
                        BandResearchNavGraph(startDestination = startRoute!!)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        notificationMonitor.stop()
        super.onDestroy()
    }
}
