@file:Suppress("SameParameterValue", "SameParameterValue", "SameParameterValue",
    "SameParameterValue"
)

package com.example.heavenesports.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heavenesports.activities.AuthActivity
import com.example.heavenesports.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

@Suppress("DEPRECATION", "SameParameterValue", "SameParameterValue")
class HomeFragment : Fragment() {

    private lateinit var homeView : View

    private lateinit var btnSalir : Button
    private lateinit var btnGetPuuid : Button

    lateinit var txtapi : TextView
    private lateinit var txtapi2 : TextView
    private lateinit var username : String

    var muestronombre : String = "TextView"
    var muestrojuego : String = "TextView"

    private val riotAPIKey = "RGAPI-04f42e82-9c44-47a5-ae88-5ca2eed7ef77"

    // URL para obtener datos del usuario
    private var url =
        "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%C3%9Fanana/LAS?api_key=$riotAPIKey"

    private var request = Request.Builder().url(url).build()

    private val client = OkHttpClient()

    private val database = FirebaseFirestore.getInstance()

    lateinit var userpuuid : Userpuuid

    // URL para obtener datos del juego
    private val url2 =
        "https://americas.api.riotgames.com/riot/account/v1/active-shards/by-game/val/by-puuid/GZHSqDMa_KCJKGDZTh2JBQgNjpZHcd7HAXoQRc83J-N_spl7QuJriZtfVTMwURauWSxKyS2eJHg5rw?api_key=$riotAPIKey"
    //val url2 = "https://la2.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/GZHSqDMa_KCJKGDZTh2JBQgNjpZHcd7HAXoQRc83J-N_spl7QuJriZtfVTMwURauWSxKyS2eJHg5rw?api_key=RGAPI-d15036af-0314-401f-b4a2-584379886091"

    private val request2 = Request.Builder().url(url2).build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeView = inflater.inflate(R.layout.fragment_home, container, false)

        btnSalir = homeView.findViewById(R.id.btn_logout)
        btnGetPuuid = homeView.findViewById(R.id.btn_getpuuid)

        txtapi = homeView.findViewById(R.id.txt_api)
        txtapi2 = homeView.findViewById(R.id.txt_api2)

        //https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%C3%9Fanana/LAS
        //https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/ELPERROPORTUGUES/663

        // Inflar el layout de este fragment
        return homeView
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        PreferenceManager.getDefaultSharedPreferences(requireContext())

        val parentJob = Job()

        val handler = CoroutineExceptionHandler { _, _ ->
            txtapi.text = "ERROR"
        }
        val scope = CoroutineScope(Dispatchers.IO + parentJob + handler)

        val useruid = FirebaseAuth.getInstance().currentUser?.uid

        // Obtener el nombre de usuario desde Firestore
        database.collection("usuarios").document("$useruid").get()
            .addOnSuccessListener { dataSnapshot  ->
                if (dataSnapshot  != null){
                    Log.d("Test", "DocumentSnapshot data: ${dataSnapshot["username"].toString()}")
                    username = dataSnapshot["username"].toString()
                    // Construir la URL actualizada con el nombre de usuario
                    val updatedUrl = "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/$username/LAS?api_key=$riotAPIKey"

                    // Actualizar la variable 'request' con la nueva URL
                    request = Request.Builder().url(updatedUrl).build()

                    // Actualizar los textos de los TextView
                    txtapi.text = muestronombre
                    txtapi2.text = muestrojuego

                }
            }
        scope.launch {
            // Se ejecuta en un contexto de CoroutineScope

            // Llama a la función cargodatosusuario para cargar los datos del usuario
            cargodatosusuario()

            // Ejecuta la función task1 en segundo plano
            task1()

            // Ejecuta la función task2 en segundo plano
            task2()
        }

        // Asigna un click listener al botón "Salir"
        btnSalir.setOnClickListener{
            showDialog(homeView)
        }

        // Asigna un click listener al botón "GetPuuid"
        btnGetPuuid.setOnClickListener{
            txtapi.text = muestronombre
            txtapi2.text = muestrojuego
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val prefs = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isDialogShown = prefs?.getBoolean("isDialogShown", false) ?: false

        if (!isDialogShown) {
            showConfirmationDialog("¡Bienvenido!", "Has iniciado sesión correctamente.")

            // Guarda el estado del diálogo mostrado en las preferencias compartidas
            val editor = prefs?.edit()
            editor?.putBoolean("isDialogShown", true)
            editor?.apply()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_config -> {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToSettingsActivity()
                homeView.findNavController().navigate(action)
            }

            R.id.action_user_info -> {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToUserInfoFragment()
                homeView.findNavController().navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showConfirmationDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun showDialog (view : View) {
        // Muestra un diálogo de confirmación para cerrar sesión
        val builder : AlertDialog.Builder = AlertDialog.Builder(view.context)
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Seguro desea cerrar sesión?")

        builder.setPositiveButton("Sí") { dialog, _ ->
            // Restablece el estado del diálogo mostrado en las preferencias compartidas
            val prefs2 = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = prefs2?.edit()
            editor?.remove("isDialogShown")
            editor?.apply()
            // Se confirma la opción de cerrar sesión
            dialog.dismiss()
            FirebaseAuth.getInstance().signOut()

            val prefs =
                this.activity?.getSharedPreferences(
                    getString(R.string.login_pref),
                    Context.MODE_PRIVATE
                )
                    ?.edit()

            if (prefs != null) {
                prefs.clear()
                prefs.apply()
            }

            val authIntent = Intent(view.context, AuthActivity::class.java)
            startActivity(authIntent)
            activity?.finish()
        }

        // Se cancela la opción de cerrar sesión
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun task1 (){
        // Realiza una llamada HTTP para obtener datos del usuario
        client.newCall(request).enqueue(object : Callback{
            @SuppressLint("SetTextI18n")
            override fun onFailure(call: Call, e: IOException) {
                // La llamada falló, muestra un mensaje de error
                txtapi.text = "No se encuentra el usuario"
                println("No se encuentra el usuario")
            }

            override fun onResponse(call: Call, response: Response) {
                // La llamada fue exitosa, procesa la respuesta
                val body = response.body?.string()
                println(body)

                val gson = GsonBuilder().create()

                userpuuid = gson.fromJson(body, Userpuuid::class.java)

                // Actualiza el valor de 'muestronombre' con el nombre y tag del usuario
                muestronombre = userpuuid.gameName + "#" + userpuuid.tagLine

            }
        })
    }

    private fun task2 (){
        // Realiza una llamada HTTP para obtener datos del juego
        client.newCall(request2).enqueue(object : Callback{
            @SuppressLint("SetTextI18n")
            override fun onFailure(call: Call, e: IOException) {
                // La llamada falló, muestra un mensaje de error
                txtapi.text = "No se encuentra el usuario"
                println("No se encuentra el usuario")
            }

            override fun onResponse(call: Call, response: Response) {
                // La llamada fue exitosa, procesa la respuesta
                val body = response.body?.string()
                println(body)

                val gson = GsonBuilder().create()

                val gamepuuid = gson.fromJson(body, Gamepuuid::class.java)

                // Actualiza el valor de 'muestrojuego' con el nombre del juego
                muestrojuego = gamepuuid.game + " en " + gamepuuid.activeShard
            }
        })
    }

    private fun cargodatosusuario(){
        val useruid = FirebaseAuth.getInstance().currentUser?.uid
        val prefs =
            activity?.getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE)
                ?.edit()

        val fotousercache : ImageView = homeView.findViewById(R.id.foto_user_cache)
        database.collection("usuarios").document("$useruid").get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot != null) {
                    Glide.with(this)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .load(dataSnapshot.getString("URL_foto_perfil"))
                        .placeholder(R.drawable.foto_user)
                        .into(fotousercache)
                    prefs?.putString("foto_perfil", "Si")
                    prefs?.apply()
                }
            }
            .addOnFailureListener {
                prefs?.putString("foto_perfil", "No")
                prefs?.apply()
            }
    }
    class Userpuuid(val gameName: String, val tagLine: String)
    class Gamepuuid(val game: String, val activeShard: String)
}

