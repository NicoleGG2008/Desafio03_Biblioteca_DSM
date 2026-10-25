package com.example.desafio03_biblioteca

import android.app.Application

class AplicacionBiblioteca : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            AyudanteFirebase.bd().setPersistenceEnabled(true)
        } catch (e: Exception) {
        }
    }
}