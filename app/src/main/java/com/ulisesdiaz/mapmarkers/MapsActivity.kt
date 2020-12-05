package com.ulisesdiaz.mapmarkers

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap

    // Variables de los permisos que se van autilizar haciendo referencia a los permisos que se van autilizar
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    // Variable que permite identificar el permiso mediante un nummero
    private val CODIGO_SOLICITUD_PERMISO = 100

    // Variable que permite obtener los datos de la ubicacion a traves de google play service Location
    var fusedLocationClient: FusedLocationProviderClient? = null

    // Variable que permitira hacer el traking de ubicacion
    var locationRequest: LocationRequest? = null

    // Se almacenara todos los marcadores que generara el usuario
    private var listaMarcadores: ArrayList<Marker>? = null

    // Marcadores de mapa
    private var marcadorGolden: Marker? = null
    private var marcadorPiramides: Marker? = null
    private var marcadorTorre: Marker? = null
    private var marcadorMiPosicion: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
    }

    /**
     * Ciclo de Vida onstart
     * Se activa cada vez que se inicie la aplicacion, cambie o la app pase a segundo plano
     * Condicinal donde si hay permisos obtiene la ubicion, caso contrario solicita los permisos
     */
    override fun onStart() {
        super.onStart()

        if (validarPermisosUbicacion()){
            //obtenerUbicacion()
        }else{
            pedirPermisos()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //mMap.mapType = GoogleMap.MAP_TYPE_HYBRID //Estilos por defecto de google maps
        val exitoCambioMapa = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.estilo_mapa))

        if (!exitoCambioMapa){
            // Mencionar que ocurrio un problema al cambiar el tipo de mapa
        }

        val goldenGate = LatLng(37.8199286, -122.4782551)
        val piramides = LatLng(29.9772962, 31.1324955)
        val torrePisa = LatLng(43.722952, 10.396597)
        val misCoordenadas = LatLng(19.546112, -96.91136)

        marcadorGolden = mMap.addMarker(MarkerOptions()
                .position(goldenGate)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Esta es Golden")
                .alpha(0.3F)
                .title("Golden gate"))
        marcadorGolden?.tag = 0

        marcadorPiramides = mMap.addMarker(MarkerOptions()
                .position(piramides)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .snippet("Esta son piramides")
                .alpha(0.6F)
                .title("Piramides de Giza"))
        marcadorPiramides?.tag = 0

        marcadorTorre = mMap.addMarker(MarkerOptions()
                .position(torrePisa)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .snippet("Esta es Torre")
                .alpha(0.9F)
                .title("Torre de pisa"))
        marcadorTorre?.tag = 0

        marcadorMiPosicion = mMap.addMarker(MarkerOptions()
                .position(misCoordenadas)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                .snippet("Esta es mi ciudad")
                .title("Mi ciudad"))
        marcadorMiPosicion?.tag = 0

        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        prepararMarcadores()
    }

    /**
     * Se activa cuando detecta el mapa que se arrastrara el maarcador siempre y cuando este tenga el permiso
     */
    override fun onMarkerDragStart(marcador: Marker?) {
        Toast.makeText(this, "Se empezo a moveer el marcador", Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR INICIAL", marcador?.position?.latitude.toString())
        val index = listaMarcadores?.indexOf(marcador!!) // Regresa un indice para saber que elemento de la lista se esta utilizando
        Log.d("MARCADOR INICIAL", listaMarcadores?.get(index!!)!!.position?.latitude.toString())
    }

    /**
     * Se activa cuando se esta arrastrando el marcador
     */
    override fun onMarkerDrag(marcador: Marker?) {
        title = marcador?.position?.latitude.toString() + " -- " +
                marcador?.position?.longitude.toString()
    }

    /**
     * Se activa cuando el marker es soltado y a su ves el ArrayList de marcadores se actualiza sus coordenas
     */
    override fun onMarkerDragEnd(marcador: Marker?) {
        Toast.makeText(this, "Termino el evento drag & drop", Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR FINAL", marcador?.position?.latitude.toString())
        val index = listaMarcadores?.indexOf(marcador!!) // Regresa un indice para saber que elemento de la lista se esta utilizando
        Log.d("MARCADOR FINAL", listaMarcadores?.get(index!!)!!.position?.latitude.toString())
    }

    /**
     * Añade marcadores cuando detecta un clcik prolongado y setea las coordenadas de donde presiono
     * el usuario
     */
    private fun prepararMarcadores(){
        listaMarcadores = ArrayList()

        mMap.setOnMapLongClickListener {
            location: LatLng ->

            listaMarcadores?.add(mMap.addMarker(MarkerOptions()
                    .position(location!!)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                    .snippet("Esta es mi ciudad")
                    .title("Mi ciudad")))
            // Obtiene el ultimo marcador que esta actualmente configurando el usuario
            listaMarcadores?.last()!!.isDraggable = true
        }
    }

    /**
     * Permite mapear los marcadores
     */
    override fun onMarkerClick(marcador: Marker?): Boolean {
        var numeroClicks = marcador?.tag as? Int
        if (numeroClicks != null){
            numeroClicks++
            marcador?.tag = numeroClicks

            Toast.makeText(this, "Se han dado "+ numeroClicks.toString() +
                    " clicks", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    /**
     * Esta funcion permite mapear si el usuario otorgo permisos, es llamada una vez que se otorgaron los permisos o se denegaron
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_PERMISO ->{
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Obtener la ubicacion
                    //obtenerUbicacion()
                }else{
                    Toast.makeText(this, "No se otorgaron permisos para la ubicacion",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Funcion que valida si el usuario ya tiene permisos o se necesita pedir
     * Se compara el permiso que deseo usar con los que se declararon en el manifest
     * Regresa verdadero si estan los permisos otorgados
     */
    private fun  validarPermisosUbicacion(): Boolean{
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(
                this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria =  ActivityCompat.checkSelfPermission(
                this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    /**
     * Si el usuario no tiene permisos o son negados entra a esta funcion para solicitarlos
     * la variable proverContexto recibe un boleano de si se otorgo el permiso o no (true o false)
     * Solo se pide el permiso de Fine Location
     */
    private fun pedirPermisos(){
        val proveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(
                this , permisoFineLocation)
        if (proveerContexto){
            // Mandar mensaje con explicacion adicional
            solicitudPermiso()
        }else{
            solicitudPermiso()
        }
    }

    /**
     * Se llama a requesPermissions para ingresar todos los permisos que funcionaran en la actividad.
     * Los permisos se ingresan por medio de un arreglo
     */
    private fun solicitudPermiso(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
        }
    }

}