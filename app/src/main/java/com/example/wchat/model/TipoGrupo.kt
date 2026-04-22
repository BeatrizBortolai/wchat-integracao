package com.example.wchat.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

enum class TipoGrupo(val nomeExibicao: String, val icone: ImageVector) {
    CMO("CMOs", Icons.Default.Groups),
    CFO("CFOs", Icons.Default.CurrencyBitcoin),
    CEO("CEOs", Icons.Default.CorporateFare),
    CPO("CPOs", Icons.Default.BusinessCenter),
    CHRO("CHROs", Icons.Default.ManageAccounts),
    ESG("ESG", Icons.Default.Recycling),
    CSO("CSOs", Icons.Default.Security),
    CIO("CIOs", Icons.Default.Lan);

    companion object {
        fun todos() = values().toList()
    }
}