package utils

inline fun Boolean.onFalse(block: () -> Unit) = also { if (!this) block() }
