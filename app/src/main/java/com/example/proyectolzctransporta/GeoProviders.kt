package com.example.proyectolzctransporta

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore

class GeoProviders {
    val collection =FirebaseFirestore.getInstance().collection("Locations")

    val geoFirestore = GeoFirestore(collection)


    fun saveLocation(idUsers: String, position: LatLng){
        geoFirestore.setLocation(idUsers, GeoPoint(position.latitude, position.longitude))
    }

    fun removeLocation(idUsers: String){
        collection.document(idUsers).delete()

    }

    fun getLocation(idUsers: String): Task<DocumentSnapshot> {
        return collection.document(idUsers).get().addOnFailureListener { exception ->
            Log.d("FIREBASE","ERROR: ${exception.toString()}")


        }
    }


}