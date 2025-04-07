package com.example.pbl5.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        modifier = Modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                onClick = { onTabSelected("Home") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = if (selectedTab == "Home") Color(0xFF1E90FF) else Color.Gray
                )
            }
            IconButton(
                onClick = { onTabSelected("Fish") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusStrong,
                    contentDescription = "Fish",
                    tint = if (selectedTab == "Fish") Color(0xFF1E90FF) else Color.Gray
                )
            }
            IconButton(
                onClick = { onTabSelected("Monitor") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = "Monitor",
                    tint = if (selectedTab == "Monitor") Color(0xFF1E90FF) else Color.Gray
                )
            }
            IconButton(
                onClick = { onTabSelected("Settings") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = if (selectedTab == "Settings") Color(0xFF1E90FF) else Color.Gray
                )
            }
        }
    }
}