package io.foliage.utils

import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE

inline fun <reified T : Any> formatToString(obj: T, style: ToStringStyle = JSON_STYLE): String =
        ToStringBuilder.reflectionToString(obj, style)
