package com.example.wchat.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class TipoSegmento(val nomeExibicao: String, val icone: ImageVector) {
    ED("ED", Icons.Default.School),
    IT("IT", Icons.Default.Computer),
    RETAIL_FINANCIAL("RETAIL & FINANCIAL", Icons.Default.Storefront),
    GRC("GRC", Icons.Default.Policy),
    HR("HR", Icons.Default.People),
    SMART_SPENDS("SMART SPENDS", Icons.Default.ShoppingCart),
    HEALTH("HEALTH", Icons.Default.LocalHospital),
    CSC("CSC", Icons.Default.HeadsetMic),
    FIELD_MARKETING("FIELD MARKETING", Icons.Default.Campaign),
    FINANCE("FINANCE", Icons.Default.AttachMoney),
    ESG("ESG", Icons.Default.Eco),
    CX("CX", Icons.Default.ThumbUp);

    companion object {
        fun todos() = values().toList()
    }
}