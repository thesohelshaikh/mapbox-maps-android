package com.mapbox.maps.extension.androidauto

import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Session
import androidx.lifecycle.Lifecycle
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental

/**
 * This is the main entry point for controlling the Mapbox car map surface.
 *
 * You can add the [MapboxCarMap] to your entire [androidx.car.app.Session] using the [Lifecycle].
 * And then any [androidx.car.app.Screen] that is using a supported template will automatically
 * show the Mapbox map. You can also, specify a [MapboxCarMap] for each individual
 * [androidx.car.app.Screen] by constructing the [MapboxCarMap] with the screen Lifecycle.
 *
 * Supported templates include:
 *  - [androidx.car.app.navigation.model.NavigationTemplate]
 *  - [androidx.car.app.navigation.model.RoutePreviewNavigationTemplate]
 *  - [androidx.car.app.navigation.model.PlaceListNavigationTemplate]
 *
 * Customize your [MapboxCarMap] with your own implementations of [MapboxCarMapObserver].
 * Use the [registerObserver] and [unregisterObserver] functions to load and detach the observers.
 *
 * The internals of this class use [AppManager.setSurfaceCallback], which assumes there is a single
 * surface callback. Do not use setSurfaceCallback, and do not create multiple instances of
 * [MapboxCarMap].
 */
@MapboxExperimental
class MapboxCarMap {
  private val carMapSurfaceOwner = CarMapSurfaceOwner()

  /**
   * The initial options used to [setup] the map.
   */
  val mapInitOptions: MapInitOptions by lazy {
    check(carMapSurfaceOwner.isSetup()) {
      "You must call MapboxCarMap.setup before you can access the MapboxCarMap.mapInitOptions"
    }
    carMapSurfaceOwner.mapInitOptions
  }

  /**
   * Accessor for the carContext provided to the MapInitOptions. This makes it easier to create
   * screens with the MapboxCarMap in the constructor.
   *
   * For example:
   *  class YourMapScreen(val mapboxCarMap: MapboxCarMap) : Screen(mapboxCarMap.carContext) {
   *
   * The carContext can also be found in the [MapboxCarMapObserver] callbacks. Make sure to
   * call [MapboxCarMap.clearObservers] when your car session is destroyed.
   */
  val carContext: CarContext by lazy {
    check(carMapSurfaceOwner.isSetup()) {
      "You must call MapboxCarMap.setup before you can access the MapboxCarMap.carContext"
    }
    carMapSurfaceOwner.carContext
  }

  /**
   * Calls [AppManager.setSurfaceCallback] to give Mapbox access to render the map onto Android
   * Auto head units. You should only call this once per [CarContext] in a [Session].
   *
   * @param carContext Android Auto CarContext
   * @param mapInitOptions parameter to initialize the head unit map
   */
  fun setup(
    carContext: CarContext,
    mapInitOptions: MapInitOptions,
  ) = apply {
    check(mapInitOptions.context is CarContext) {
      "You must setup the MapboxCarMap MapInitOptions with a CarContext"
    }
    carMapSurfaceOwner.setup(carContext, mapInitOptions)
    carContext.getCarService(AppManager::class.java).setSurfaceCallback(carMapSurfaceOwner)
  }

  /**
   * Returns the current [MapboxCarMapSurface]. It is recommended to use [registerObserver] and
   * [MapboxCarMapObserver] to attach and detach your customizations.
   */
  val carMapSurface: MapboxCarMapSurface?
    get() { return carMapSurfaceOwner.mapboxCarMapSurface }

  /**
   * Accessor to the visible area calculated by the car library. It is recommended to
   * use the values returned by [MapboxCarMapObserver.onVisibleAreaChanged].
   */
  val visibleArea: Rect?
    get() { return carMapSurfaceOwner.visibleArea }

  /**
   * Accessor to the edgeInsets calculated by the car library. It is recommended to
   * use the values returned by [MapboxCarMapObserver.onVisibleAreaChanged].
   */
  val edgeInsets: EdgeInsets?
    get() { return carMapSurfaceOwner.edgeInsets }

  /**
   * @param mapboxCarMapObserver implements the desired mapbox car experiences
   */
  fun registerObserver(mapboxCarMapObserver: MapboxCarMapObserver) = apply {
    carMapSurfaceOwner.registerObserver(mapboxCarMapObserver)
  }

  /**
   * @param mapboxCarMapObserver the instance used in [registerObserver]
   */
  fun unregisterObserver(mapboxCarMapObserver: MapboxCarMapObserver) {
    carMapSurfaceOwner.unregisterObserver(mapboxCarMapObserver)
  }

  /**
   * Optional function to clear all observers registered through [registerObserver]
   */
  fun clearObservers() {
    carMapSurfaceOwner.clearObservers()
  }

  /**
   * Override the car gestures with your own implementation. If you would like to build a custom
   * experience for handling gestures, you can implement the [MapboxCarMapGestureHandler]
   * interface, or override the [DefaultMapboxCarMapGestureHandler], or set to null to disable
   * gesture handling.
   */
  fun setGestureHandler(gestureHandler: MapboxCarMapGestureHandler?) {
    carMapSurfaceOwner.gestureHandler = gestureHandler
  }
}