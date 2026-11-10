package dev.tint_wym.novora_hrms_android.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Approval
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import dev.tint_wym.novora_hrms_android.core.model.SessionUser
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraCard
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import dev.tint_wym.novora_hrms_android.feature.approvals.ApprovalsScreen
import dev.tint_wym.novora_hrms_android.feature.attendance.AttendanceScreen
import dev.tint_wym.novora_hrms_android.feature.claims.ClaimsScreen
import dev.tint_wym.novora_hrms_android.feature.helpdesk.HelpdeskScreen
import dev.tint_wym.novora_hrms_android.feature.home.HomeScreen
import dev.tint_wym.novora_hrms_android.feature.leave.LeaveScreen
import dev.tint_wym.novora_hrms_android.feature.notifications.NotificationsScreen
import dev.tint_wym.novora_hrms_android.feature.payroll.PayrollScreen
import dev.tint_wym.novora_hrms_android.feature.profile.ProfileScreen

private data class TabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val overlayRoutes = setOf("notifications", "claims", "helpdesk")

@Composable
fun MainShell(
    user: SessionUser,
    onLogout: () -> Unit,
) {
    val tabs = buildList {
        add(TabItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home))
        add(TabItem("attendance", "Attendance", Icons.Filled.Schedule, Icons.Outlined.Schedule))
        add(TabItem("leave", "Leave", Icons.Filled.BeachAccess, Icons.Outlined.BeachAccess))
        add(TabItem("payroll", "Payroll", Icons.Filled.Payments, Icons.Outlined.Payments))
        if (user.canApprove) {
            add(TabItem("approvals", "Approvals", Icons.Filled.Approval, Icons.Outlined.Approval))
        }
        add(TabItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person))
    }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    fun navigateTo(route: String) {
        if (route in overlayRoutes) {
            navController.navigate(route) {
                launchSingleTop = true
            }
            return
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showBottomBar = currentRoute !in overlayRoutes

    Scaffold(
        containerColor = NovoraSurface,
        bottomBar = {
            if (showBottomBar) {
                PolishBottomBar(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    onTabSelected = ::navigateTo,
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") {
                HomeScreen(
                    user = user,
                    onNavigate = { route ->
                        if (route in overlayRoutes || tabs.any { it.route == route }) {
                            navigateTo(route)
                        }
                    },
                )
            }
            composable("attendance") { AttendanceScreen() }
            composable("leave") { LeaveScreen() }
            composable("payroll") { PayrollScreen() }
            if (user.canApprove) {
                composable("approvals") { ApprovalsScreen(user = user) }
            }
            composable("profile") { ProfileScreen(user = user, onLogout = onLogout) }
            composable("notifications") {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable("claims") {
                ClaimsScreen(onBack = { navController.popBackStack() })
            }
            composable("helpdesk") {
                HelpdeskScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun PolishBottomBar(
    tabs: List<TabItem>,
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    Surface(
        color = NovoraCard,
        border = BorderStroke(1.dp, NovoraBorder),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab.route) }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (tabs.size > 5) 44.dp else 52.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (selected) NovoraPrimaryContainer else androidx.compose.ui.graphics.Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (selected) NovoraBlue else NovoraSlate,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = if (tabs.size > 5) 10.sp else 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) NovoraBlue else NovoraSlate,
                    )
                }
            }
        }
    }
}
