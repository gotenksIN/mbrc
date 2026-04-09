package com.kelsos.mbrc.screenshots

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.kelsos.mbrc.R
import com.kelsos.mbrc.core.common.state.ConnectionStatus
import com.kelsos.mbrc.core.ui.theme.RemoteTheme
import com.kelsos.mbrc.core.ui.theme.connection_status_connected
import com.kelsos.mbrc.core.ui.theme.connection_status_connecting
import com.kelsos.mbrc.core.ui.theme.connection_status_offline

private const val homeRoute = "home"
private const val queueRoute = "now_playing_list"
private const val libraryRoute = "library"
private const val playlistsRoute = "playlists"
private const val radioRoute = "radio"
private const val connectionManagerRoute = "connection_manager"
private const val settingsRoute = "settings"
private const val helpRoute = "help"

@PreviewTest
@Preview(name = "Drawer Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DrawerPreviewLight() {
  RemoteTheme(darkTheme = false) {
    NavigationDrawerPreviewContent(
      currentRoute = homeRoute,
      connectionStatus = ConnectionStatus.Connected,
      connectionName = "Living Room PC",
      versionName = "1.6.0"
    )
  }
}

@PreviewTest
@Preview(name = "Drawer Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun DrawerPreviewDark() {
  RemoteTheme(darkTheme = true) {
    NavigationDrawerPreviewContent(
      currentRoute = homeRoute,
      connectionStatus = ConnectionStatus.Connected,
      connectionName = "Living Room PC",
      versionName = "1.6.0"
    )
  }
}

@PreviewTest
@Preview(name = "Drawer Offline", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DrawerPreviewOffline() {
  RemoteTheme(darkTheme = false) {
    NavigationDrawerPreviewContent(
      currentRoute = homeRoute,
      connectionStatus = ConnectionStatus.Offline,
      connectionName = null,
      versionName = "1.6.0"
    )
  }
}

@PreviewTest
@Preview(name = "Drawer Queue Selected", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DrawerPreviewQueueSelected() {
  RemoteTheme(darkTheme = false) {
    NavigationDrawerPreviewContent(
      currentRoute = queueRoute,
      connectionStatus = ConnectionStatus.Connected,
      connectionName = "Desktop",
      versionName = "1.6.0"
    )
  }
}

@Composable
private fun NavigationDrawerPreviewContent(
  currentRoute: String,
  connectionStatus: ConnectionStatus,
  connectionName: String?,
  versionName: String,
  modifier: Modifier = Modifier
) {
  ModalDrawerSheet(
    modifier = modifier,
    drawerContainerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      PreviewDrawerHeader(
        connectionStatus = connectionStatus,
        connectionName = connectionName
      )

      PreviewDrawerNavigationItems(currentRoute = currentRoute)

      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = stringResource(R.string.drawer_version, versionName),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 28.dp, vertical = 16.dp)
      )
    }
  }
}

@Composable
private fun PreviewDrawerHeader(
  connectionStatus: ConnectionStatus,
  connectionName: String?
) {
  val isDarkTheme = isSystemInDarkTheme()
  val primary = MaterialTheme.colorScheme.primary
  val gradientColors = remember(isDarkTheme, primary) {
    if (isDarkTheme) {
      listOf(primary.copy(alpha = 0.3f), primary.copy(alpha = 0.1f))
    } else {
      listOf(primary, primary.copy(alpha = 0.85f))
    }
  }
  val onPrimary = if (isDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp)
      .background(Brush.verticalGradient(colors = gradientColors))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.Bottom
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = onPrimary.copy(alpha = 0.2f)
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
              )
            }
          }

          Column {
            Text(
              text = stringResource(R.string.application_name),
              style = MaterialTheme.typography.titleMedium,
              color = onPrimary,
              fontWeight = FontWeight.SemiBold
            )

            val statusText = when (connectionStatus) {
              is ConnectionStatus.Connected -> connectionName
              is ConnectionStatus.Connecting -> {
                val cycle = connectionStatus.cycle
                if (cycle != null) {
                  stringResource(
                    R.string.drawer_connection_connecting_cycle,
                    cycle,
                    connectionStatus.maxCycles
                  )
                } else {
                  stringResource(R.string.drawer_connection_connecting)
                }
              }

              is ConnectionStatus.Authenticating -> stringResource(R.string.drawer_connection_status_on)
              is ConnectionStatus.Offline -> stringResource(R.string.drawer_connection_not_connected)
            }

            if (statusText != null) {
              Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = onPrimary.copy(alpha = 0.7f)
              )
            }
          }
        }

        PreviewConnectionStatusIconButton(
          connectionStatus = connectionStatus,
          onPrimary = onPrimary
        )
      }
    }
  }
}

@Composable
private fun PreviewConnectionStatusIconButton(
  connectionStatus: ConnectionStatus,
  onPrimary: Color
) {
  val (statusColor, statusIcon) = when (connectionStatus) {
    ConnectionStatus.Connected -> connection_status_connected to Icons.Default.Wifi
    is ConnectionStatus.Connecting -> connection_status_connecting to Icons.Default.Wifi
    ConnectionStatus.Authenticating -> connection_status_connecting to Icons.Default.Wifi
    ConnectionStatus.Offline -> connection_status_offline to Icons.Default.WifiOff
  }
  val isConnecting = connectionStatus is ConnectionStatus.Connecting ||
    connectionStatus is ConnectionStatus.Authenticating

  Box(
    modifier = Modifier
      .size(48.dp)
      .clip(CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      shape = CircleShape,
      color = onPrimary.copy(alpha = 0.2f)
    ) {}

    if (isConnecting) {
      CircularProgressIndicator(
        modifier = Modifier.size(46.dp),
        color = statusColor,
        strokeWidth = 2.dp
      )
    }

    if (connectionStatus is ConnectionStatus.Connecting) {
      val cycle = connectionStatus.cycle
      if (cycle != null) {
        CircularProgressIndicator(
          progress = { cycle.toFloat() / connectionStatus.maxCycles.toFloat() },
          modifier = Modifier.size(36.dp),
          color = statusColor.copy(alpha = 0.7f),
          strokeWidth = 3.dp,
          trackColor = statusColor.copy(alpha = 0.2f)
        )
      }
    }

    Icon(
      imageVector = statusIcon,
      contentDescription = null,
      tint = statusColor,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
private fun PreviewDrawerNavigationItems(currentRoute: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    PreviewSectionLabel(R.string.drawer_section_music)

    previewPrimaryNavigationItems.forEach { item ->
      PreviewDrawerNavigationItem(item = item, currentRoute = currentRoute)
    }

    Spacer(modifier = Modifier.height(16.dp))

    PreviewSectionLabel(R.string.common_settings)

    previewSecondaryNavigationItems.forEach { item ->
      PreviewDrawerNavigationItem(item = item, currentRoute = currentRoute)
    }
  }
}

@Composable
private fun PreviewDrawerNavigationItem(
  item: PreviewDrawerItem,
  currentRoute: String,
  colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
    selectedContainerColor = Color.Transparent,
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.onSurface,
    unselectedContainerColor = Color.Transparent,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
  )
) {
  val isSelected = currentRoute == item.route

  NavigationDrawerItem(
    icon = {
      Icon(
        imageVector = item.icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
    },
    label = {
      Text(
        text = stringResource(item.titleRes),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
      )
    },
    selected = isSelected,
    onClick = {},
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 2.dp),
    shape = RoundedCornerShape(28.dp),
    colors = colors
  )
}

@Composable
private fun PreviewSectionLabel(textRes: Int) {
  Text(
    text = stringResource(textRes).uppercase(),
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight = FontWeight.Medium,
    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
  )
}

private data class PreviewDrawerItem(
  val route: String,
  val icon: ImageVector,
  val titleRes: Int
)

private val previewPrimaryNavigationItems = listOf(
  PreviewDrawerItem(homeRoute, Icons.Default.Home, R.string.nav_now_playing),
  PreviewDrawerItem(queueRoute, Icons.AutoMirrored.Filled.QueueMusic, R.string.nav_queue),
  PreviewDrawerItem(libraryRoute, Icons.Default.LibraryMusic, R.string.common_library),
  PreviewDrawerItem(playlistsRoute, Icons.AutoMirrored.Filled.PlaylistPlay, R.string.nav_playlists),
  PreviewDrawerItem(radioRoute, Icons.Default.Radio, R.string.nav_radio)
)

private val previewSecondaryNavigationItems = listOf(
  PreviewDrawerItem(connectionManagerRoute, Icons.Default.DesktopWindows, R.string.nav_connections),
  PreviewDrawerItem(settingsRoute, Icons.Default.Settings, R.string.common_settings),
  PreviewDrawerItem(helpRoute, Icons.AutoMirrored.Filled.Help, R.string.nav_help)
)
