package com.example.rinconsur.ui.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.example.rinconsur.ui.theme.Dimens
@Composable
fun RinconSurCampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMensaje: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.alturaCampo),
        label = {
            Text(text = label)
        },
        placeholder = placeholder?.let { placeholderText ->
            { Text(text = placeholderText) }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        isError = errorMensaje != null,
        supportingText = errorMensaje?.let { mensaje ->
            { Text(text = mensaje) }
        },
        shape = RoundedCornerShape(Dimens.radioCampo),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor =
                MaterialTheme.colorScheme.surface,
            unfocusedContainerColor =
                MaterialTheme.colorScheme.surface,

            errorContainerColor =
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),

            focusedBorderColor =
                MaterialTheme.colorScheme.primary,
            unfocusedBorderColor =
                MaterialTheme.colorScheme.outline,
            errorBorderColor =
                MaterialTheme.colorScheme.error,

            focusedLabelColor =
                MaterialTheme.colorScheme.primary,
            unfocusedLabelColor =
                MaterialTheme.colorScheme.onSurfaceVariant,
            errorLabelColor =
                MaterialTheme.colorScheme.error,

            cursorColor =
                MaterialTheme.colorScheme.primary,
            focusedTextColor =
                MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor =
                MaterialTheme.colorScheme.onSurface,

            focusedPlaceholderColor =
                MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}