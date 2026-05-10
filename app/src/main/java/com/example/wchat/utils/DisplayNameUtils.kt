package com.example.wchat.utils

import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento

/**
 * Centraliza a conversão de IDs técnicos do backend para nomes amigáveis na UI.
 * Ex.: RETAIL_FINANCIAL -> RETAIL & FINANCIAL.
 */
object DisplayNameUtils {

    fun grupoNomeExibicao(idOuNome: String?): String {
        val base = limparPrefixo(idOuNome, "Grupo")
        if (base.isBlank()) return "Grupo"

        val tipo = TipoGrupo.values().find { it.name.equals(base, ignoreCase = true) }
        return tipo?.nomeExibicao ?: humanizarId(base)
    }

    fun segmentoNomeExibicao(idOuNome: String?): String {
        val base = limparPrefixo(idOuNome, "Segmento")
        if (base.isBlank()) return "Segmento"

        val tipo = TipoSegmento.values().find { it.name.equals(base, ignoreCase = true) }
        return tipo?.nomeExibicao ?: humanizarId(base)
    }

    fun grupoComPrefixo(idOuNome: String?): String = "Grupo ${grupoNomeExibicao(idOuNome)}"

    fun segmentoComPrefixo(idOuNome: String?): String = "Segmento ${segmentoNomeExibicao(idOuNome)}"

    private fun limparPrefixo(valor: String?, prefixo: String): String {
        return valor
            .orEmpty()
            .trim()
            .removePrefix("$prefixo ")
            .trim()
    }

    private fun humanizarId(id: String): String {
        return id
            .replace('_', ' ')
            .lowercase()
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { palavra -> palavra.replaceFirstChar { it.uppercase() } }
    }
}
