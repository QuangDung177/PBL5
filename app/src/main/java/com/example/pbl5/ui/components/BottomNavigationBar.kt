package com.example.pbl5.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
        backgroundColor = Color.White,
        elevation = 8.dp,
        modifier = Modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Trang chủ
            IconButton(
                onClick = { onTabSelected("Home") },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == "Home") Color(0xFF1E90FF) else Color.Gray
                    )
                    if (selectedTab == "Home") {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color(0xFF1E90FF))
                        )
                    }
                }
            }

            // Cá
            IconButton(
                onClick = { onTabSelected("Fish") },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CenterFocusStrong,
                        contentDescription = "Fish",
                        tint = if (selectedTab == "Fish") Color(0xFF1E90FF) else Color.Gray
                    )
                    if (selectedTab == "Fish") {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color(0xFF1E90FF))
                        )
                    }
                }
            }

            // Giám sát
            IconButton(
                onClick = { onTabSelected("Monitor") },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sensors,
                        contentDescription = "Monitor",
                        tint = if (selectedTab == "Monitor") Color(0xFF1E90FF) else Color.Gray
                    )
                    if (selectedTab == "Monitor") {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color(0xFF1E90FF))
                        )
                    }
                }
            }

            // Cài đặt
            IconButton(
                onClick = { onTabSelected("Settings") },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = if (selectedTab == "Settings") Color(0xFF1E90FF) else Color.Gray
                    )
                    if (selectedTab == "Settings") {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color(0xFF1E90FF))
                        )
                    }
                }
            }
        }
    }
}