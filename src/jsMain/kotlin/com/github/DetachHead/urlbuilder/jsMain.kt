package com.github.DetachHead.urlbuilder

public external fun encodeURIComponent(uri: String): String

internal actual fun encodeURLsegment(segment: String): String = encodeURIComponent(segment)
