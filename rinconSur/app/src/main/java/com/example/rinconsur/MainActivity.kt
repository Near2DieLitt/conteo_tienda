package com.example.rinconsur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.rinconsur.navigation.AppNavigation
import com.example.rinconsur.ui.theme.RinconSurTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var modoOscuro by rememberSaveable {
                mutableStateOf(false)
            }

            RinconSurTheme(
                darkTheme = modoOscuro
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        modoOscuro = modoOscuro,
                        onCambiarTema = {
                            modoOscuro = !modoOscuro
                        }
                    )
                }
            }
        }
    }
}