package com.zdmgold.orlumen.core.model

import androidx.annotation.StringRes

enum class DesignKind {
    VISUALIZER,
    AOD
}

data class DesignDescriptor(
    val id: String,
    val kind: DesignKind,
    @StringRes val nameRes: Int,
    val isPro: Boolean,
    val params: List<ParamSpec>
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(params.map { it.key }.toSet().size == params.size) { "duplicate param keys in $id" }
    }
}
