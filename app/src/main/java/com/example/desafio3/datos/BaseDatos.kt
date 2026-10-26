package com.example.desafio3.datos

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object BaseDatos {

    // URL de la Realtime Database del proyecto desafio3-6698f (ubicación us-central1)
    private const val URL_BASE = "https://desafio3-6698f-default-rtdb.firebaseio.com"

    // La persistencia guarda una copia local, así los datos siguen aunque no haya internet
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(URL_BASE).apply { setPersistenceEnabled(true) }
    }

    // Cada usuario guarda sus datos bajo su propio uid: usuarios/{uid}/...
    private fun refUsuario(uid: String): DatabaseReference =
        database.getReference("usuarios").child(uid)

    fun refLibros(uid: String): DatabaseReference = refUsuario(uid).child("libros")

    fun refMeta(uid: String, anio: Int): DatabaseReference =
        refUsuario(uid).child("metas").child(anio.toString())
}
