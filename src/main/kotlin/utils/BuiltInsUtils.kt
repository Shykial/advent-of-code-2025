package utils

inline fun Boolean.onTrue(block: () -> Unit) = also { if (this) block() }

inline fun Boolean.onFalse(block: () -> Unit) = also { if (!this) block() }
