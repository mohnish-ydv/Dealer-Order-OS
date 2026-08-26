package com.apex.dms.ui.screens

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apex.dms.data.ActorRole
import com.apex.dms.data.AppStore
import com.apex.dms.ui.components.PrimaryAction
import com.apex.dms.ui.components.SecondaryAction
import com.apex.dms.ui.theme.ShoppeBackground
import com.apex.dms.ui.theme.ShoppeBlue
import com.apex.dms.ui.theme.ShoppeBlueSoft
import com.apex.dms.ui.theme.ShoppeInk
import com.apex.dms.ui.theme.ShoppeMint
import com.apex.dms.ui.theme.ShoppeMuted
import com.truecaller.android.sdk.TcOAuthCallback
import com.truecaller.android.sdk.TcOAuthData
import com.truecaller.android.sdk.TcOAuthError
import com.truecaller.android.sdk.TcSdk
import com.truecaller.android.sdk.TcSdkOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.SecureRandom
import java.security.MessageDigest
import java.util.Base64

@Composable
fun AccessScreen(
    store: AppStore,
    onAuthenticated: (ActorRole) -> Unit,
) {
    if (!store.onboardingCompleted) {
        IntroJourney(onFinished = store::completeOnboarding)
    } else {
        AuthJourney(store = store, onAuthenticated = onAuthenticated)
    }
}

private data class IntroPage(
    val icon: ImageVector,
    val eyebrow: String,
    val title: String,
    val description: String,
    val bullets: List<String>,
)

@Composable
private fun IntroJourney(onFinished: () -> Unit) {
    val pages = remember {
        listOf(
            IntroPage(
                Icons.Rounded.Business,
                "APEX DMS",
                "Dealer commerce, without the chaos.",
                "Give dealers one polished place to discover products, request commercial terms and follow every order.",
                listOf("Searchable live catalogue", "Structured RFQs instead of chat threads", "Quote and order visibility"),
            ),
            IntroPage(
                Icons.Rounded.AutoAwesome,
                "BUSINESS AUTOMATION OS",
                "Automations handle the busy work.",
                "RFQs, approvals, follow-ups, payment events and inventory signals move through one traceable workflow.",
                listOf("Approval rules for high-value quotes", "Automation activity and exception handling", "Tally, payments and messaging ready"),
            ),
            IntroPage(
                Icons.Rounded.Speed,
                "ONE OPERATIONS VIEW",
                "Know what needs attention now.",
                "Sales teams act on the exceptions while owners get a concise digest of orders, collections and risk.",
                listOf("Role-aware staff workspace", "Owner digest and automation health", "Offline-first experience with backend sync"),
            ),
        )
    }
    var page by remember { mutableIntStateOf(0) }
    val current = pages[page]

    Column(
        Modifier.fillMaxSize().background(ShoppeBackground).padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.padding(top = 60.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.indices.forEach { index ->
                    Surface(
                        modifier = Modifier.height(5.dp).weight(if (index == page) 2f else 1f),
                        shape = CircleShape,
                        color = if (index <= page) ShoppeBlue else Color(0xFFDCE3EC),
                    ) {}
                }
            }
            Spacer(Modifier.height(48.dp))
            Surface(shape = RoundedCornerShape(24.dp), color = if (page == 1) ShoppeMint else ShoppeBlueSoft, modifier = Modifier.size(76.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(current.icon, null, tint = ShoppeBlue, modifier = Modifier.size(36.dp)) }
            }
            Spacer(Modifier.height(26.dp))
            Text(current.eyebrow, style = MaterialTheme.typography.labelLarge, color = ShoppeBlue)
            Text(current.title, style = MaterialTheme.typography.displaySmall, color = ShoppeInk, modifier = Modifier.padding(top = 8.dp))
            Text(current.description, style = MaterialTheme.typography.bodyLarge, color = ShoppeMuted, modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.height(26.dp))
            current.bullets.forEach { bullet ->
                Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = ShoppeBlue, modifier = Modifier.size(21.dp))
                    Text(bullet, style = MaterialTheme.typography.bodyMedium, color = ShoppeInk, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }

        Column(Modifier.padding(bottom = 30.dp)) {
            PrimaryAction(
                text = if (page == pages.lastIndex) "Get started" else "Continue",
                onClick = {
                    if (page == pages.lastIndex) onFinished() else page += 1
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (page < pages.lastIndex) {
                Text(
                    "Skip introduction",
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onFinished).padding(14.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = ShoppeMuted,
                )
            }
        }
    }
}

@Composable
private fun AuthJourney(store: AppStore, onAuthenticated: (ActorRole) -> Unit) {
    val activity = LocalContext.current as? ComponentActivity
    var mode by remember { mutableStateOf("login") }
    var tcUsable by remember { mutableStateOf<Boolean?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var requestedState by remember { mutableStateOf("") }
    var codeVerifier by remember { mutableStateOf("") }

    val callback = remember(activity) {
        object : TcOAuthCallback {
            override fun onSuccess(tcOAuthData: TcOAuthData) {
                val returnedState = tcOAuthData.state.orEmpty()
                if (requestedState.isBlank() || returnedState != requestedState) {
                    localError = "Security check failed. Please try Truecaller again."
                    return
                }
                store.signInWithTruecaller(
                    authorizationCode = tcOAuthData.authorizationCode.orEmpty(),
                    codeVerifier = codeVerifier,
                    state = returnedState,
                ) { success, role ->
                    if (success) onAuthenticated(role)
                }
            }

            override fun onFailure(tcOAuthError: TcOAuthError) {
                localError = tcOAuthError.errorMessage ?: "Truecaller sign-in was not completed."
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        activity?.let { host ->
            runCatching { TcSdk.getInstance().onActivityResultObtained(host, result.resultCode, result.data) }
                .onFailure { localError = it.message ?: "Could not complete Truecaller authorization." }
        }
    }

    LaunchedEffect(activity) {
        val host = activity ?: return@LaunchedEffect
        tcUsable = runCatching {
            val options = TcSdkOptions.Builder(host, callback)
                .buttonColor(AndroidColor.rgb(23, 104, 221))
                .buttonTextColor(AndroidColor.WHITE)
                .sdkOptions(TcSdkOptions.OPTION_VERIFY_ONLY_TC_USERS)
                .build()
            withContext(Dispatchers.IO) { TcSdk.init(options) }
            TcSdk.getInstance().isOAuthFlowUsable
        }.getOrElse {
            localError = "Truecaller is unavailable on this device. Demo access remains available."
            false
        }
    }

    fun launchTruecaller() {
        val host = activity ?: run { localError = "Unable to open Truecaller from this screen."; return }
        if (tcUsable != true) {
            localError = "Truecaller app is not available. Use the demo access below for QA."
            return
        }
        runCatching {
            requestedState = BigInteger(130, SecureRandom()).toString(32)
            codeVerifier = generatePkceVerifier()
            val challenge = generatePkceChallenge(codeVerifier)
            TcSdk.getInstance().apply {
                setOAuthState(requestedState)
                setOAuthScopes(arrayOf("profile", "phone", "openid", "email", "address"))
                setCodeChallenge(challenge)
                getAuthorizationCode(host, launcher)
            }
        }.onFailure { localError = it.message ?: "Could not start Truecaller." }
    }

    Column(
        Modifier.fillMaxSize().background(ShoppeBackground).padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.padding(top = 58.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = ShoppeBlue, modifier = Modifier.size(62.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Business, null, tint = Color.White, modifier = Modifier.size(30.dp)) }
            }
            Spacer(Modifier.height(24.dp))
            Text(if (mode == "login") "Welcome back" else "Create your dealer account", style = MaterialTheme.typography.displaySmall, color = ShoppeInk)
            Text(
                if (mode == "login") "Use your verified Truecaller profile for a faster, safer sign-in." else "Your verified number creates a secure dealer profile. Business details can be completed afterwards.",
                style = MaterialTheme.typography.bodyLarge,
                color = ShoppeMuted,
                modifier = Modifier.padding(top = 10.dp),
            )
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthTab("Log in", mode == "login", Modifier.weight(1f)) { mode = "login" }
                AuthTab("Register", mode == "register", Modifier.weight(1f)) { mode = "register" }
            }
            Spacer(Modifier.height(28.dp))

            PrimaryAction(
                text = when {
                    store.authLoading -> "Verifying…"
                    tcUsable == null -> "Preparing Truecaller…"
                    else -> "Continue with Truecaller"
                },
                onClick = ::launchTruecaller,
                enabled = !store.authLoading && tcUsable != null,
                modifier = Modifier.fillMaxWidth(),
            )
            if (store.authLoading) {
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Creating secure session…", modifier = Modifier.padding(start = 9.dp), style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                }
            }

            val error = store.authError ?: localError
            if (!error.isNullOrBlank()) {
                Surface(Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(15.dp), color = Color(0xFFFFF0F0)) {
                    Text(error, modifier = Modifier.padding(13.dp), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9B2C2C))
                }
            }

            Row(Modifier.padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lock, null, tint = ShoppeMuted, modifier = Modifier.size(18.dp))
                Text("Truecaller verifies identity; Apex DMS stores the app session securely through the backend.", modifier = Modifier.padding(start = 9.dp), style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
            }
        }

        Column(Modifier.padding(bottom = 28.dp)) {
            Text("Demo / QA access", style = MaterialTheme.typography.labelLarge, color = ShoppeMuted)
            Text("Use only when testing without a configured Truecaller account.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp, bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DemoAccess(Icons.Rounded.Person, "Dealer", Modifier.weight(1f)) {
                    store.signIn(ActorRole.DEALER); onAuthenticated(ActorRole.DEALER)
                }
                DemoAccess(Icons.Rounded.Security, "Staff", Modifier.weight(1f)) {
                    store.signIn(ActorRole.ADMIN); onAuthenticated(ActorRole.ADMIN)
                }
            }
        }
    }
}

private fun generatePkceVerifier(): String {
    val bytes = ByteArray(48)
    SecureRandom().nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

private fun generatePkceChallenge(verifier: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
}

@Composable
private fun AuthTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) ShoppeBlueSoft else Color.Transparent,
    ) {
        Text(
            text,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) ShoppeBlue else ShoppeMuted,
        )
    }
}

@Composable
private fun DemoAccess(icon: ImageVector, text: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = Color.White) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = ShoppeBlue, modifier = Modifier.size(20.dp))
            Text(text, modifier = Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.labelLarge, color = ShoppeInk)
            Icon(Icons.Rounded.ChevronRight, null, tint = ShoppeMuted, modifier = Modifier.size(18.dp))
        }
    }
}
