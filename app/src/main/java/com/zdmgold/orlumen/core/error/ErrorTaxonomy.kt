package com.zdmgold.orlumen.core.error

sealed class ErrorTaxonomy(val code: String, val cause: Throwable?) {
    class Recoverable(code: String, cause: Throwable? = null) : ErrorTaxonomy(code, cause)

    class Fatal(code: String, cause: Throwable? = null) : ErrorTaxonomy(code, cause)

    class Network(code: String, cause: Throwable? = null) : ErrorTaxonomy(code, cause)

    class UserCaused(code: String, cause: Throwable? = null) : ErrorTaxonomy(code, cause)
}
