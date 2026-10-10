package com.zdmgold.orlumen.core.model

import androidx.annotation.StringRes

sealed interface ParamSpec {
    val key: String
    val labelRes: Int

    data class Slider(
        override val key: String,
        @StringRes override val labelRes: Int,
        val min: Float,
        val max: Float,
        val defaultValue: Float
    ) : ParamSpec {
        init {
            require(min < max) { "min must be below max for $key" }
            require(defaultValue in min..max) { "default outside range for $key" }
        }
    }

    data class Option(val id: String, @StringRes val labelRes: Int)

    data class Choice(
        override val key: String,
        @StringRes override val labelRes: Int,
        val options: List<Option>,
        val defaultOptionId: String
    ) : ParamSpec {
        init {
            require(options.isNotEmpty()) { "choice $key needs at least one option" }
            require(options.any { it.id == defaultOptionId }) { "default option missing for $key" }
        }
    }

    data class Toggle(override val key: String, @StringRes override val labelRes: Int, val defaultValue: Boolean) :
        ParamSpec
}
