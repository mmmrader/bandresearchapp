package com.tkachukmo.bandresearchapp.feature.auth.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val AuthBg = Color(0xFF090B0D)
internal val AuthPanel = Color(0xFF1A171F)
internal val AuthInput = Color(0xFF0B0C0D)
internal val AuthStroke = Color(0xFF34303C)
internal val AuthPrimary = Color(0xFFD6B3FF)
internal val AuthPrimaryStrong = Color(0xFFB579FF)
internal val AuthMuted = Color(0xFF9D94A8)
internal val AuthText = Color(0xFFF6F0FF)

@Composable
internal fun AuthScene(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuthBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x663E155E), Color.Transparent),
                    center = center.copy(y = size.height * 0.18f),
                    radius = size.width * 0.9f
                )
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0x99051110)),
                    startY = size.height * 0.45f,
                    endY = size.height
                )
            )
        }
        content()
    }
}

@Composable
internal fun BandMatchHeader(
    subtitle: String,
    compact: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!compact) {
            EqualizerLogo()
            Spacer(Modifier.height(18.dp))
        }
        Text(
            text = "BandMatch",
            color = AuthPrimary,
            fontSize = if (compact) 38.sp else 44.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            color = AuthText.copy(alpha = 0.78f),
            fontSize = if (compact) 13.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        )
    }
}

@Composable
internal fun EqualizerLogo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "logo")
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(92.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF3A2B4D).copy(alpha = 0.88f))
            .border(1.dp, AuthPrimary.copy(alpha = 0.28f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heights = listOf(22.dp, 42.dp, 64.dp, 42.dp, 22.dp)
            heights.forEachIndexed { index, height ->
                Box(
                    modifier = Modifier
                        .size(width = 7.dp, height = height * (if (index == 2) pulse else 0.86f + pulse * 0.14f))
                        .clip(RoundedCornerShape(6.dp))
                        .background(AuthPrimary)
                )
            }
        }
    }
}

@Composable
internal fun AuthCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AuthStroke, RoundedCornerShape(24.dp)),
        color = AuthPanel.copy(alpha = 0.94f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = AuthText.copy(alpha = 0.82f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled,
            isError = isError,
            visualTransformation = visualTransformation,
            leadingIcon = icon?.let { { Icon(it, null, tint = AuthMuted) } },
            trailingIcon = trailingIcon,
            textStyle = LocalTextStyle.current.copy(color = AuthText, fontSize = 18.sp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AuthInput,
                unfocusedContainerColor = AuthInput,
                disabledContainerColor = AuthInput.copy(alpha = 0.7f),
                focusedBorderColor = AuthPrimary,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color(0xFFFF6B8A),
                cursorColor = AuthPrimary,
                focusedTextColor = AuthText,
                unfocusedTextColor = AuthText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(if (minLines > 1) 118.dp else 64.dp)
        )
        supportingText?.let {
            Text(
                text = it,
                color = if (isError) Color(0xFFFF8FA5) else AuthMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 6.dp, top = 6.dp)
            )
        }
    }
}

@Composable
internal fun PasswordVisibilityButton(
    visible: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = null,
            tint = AuthMuted
        )
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimary,
            contentColor = Color(0xFF35125A),
            disabledContainerColor = AuthPrimary.copy(alpha = 0.35f),
            disabledContentColor = Color(0xFF35125A).copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color(0xFF35125A),
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun StepDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == current) 26.dp else 8.dp, height = 8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (index == current) AuthPrimary else AuthStroke)
            )
        }
    }
}

@Composable
internal fun SmallMutedText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = AuthMuted,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
