package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import java.io.File
import java.io.InputStream

class Messages(file: File, stream: InputStream) {
    companion object {
        @JvmStatic
        fun PREFIX() = AxMinionsAPI.INSTANCE.getMessages().get<String>("prefix")
        @JvmStatic
        fun NO_CONTAINER_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-container")
        @JvmStatic
        fun NO_TOOL_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-tool")
        @JvmStatic
        fun NO_WATER_NEARBY_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-water-nearby")
        @JvmStatic
        fun CONTAINER_FULL_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.container-full")
        @JvmStatic
        fun RELOAD_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("reload")
        @JvmStatic
        fun PLACE_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.success")
        @JvmStatic
        fun PLACE_LIMIT_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.limit-reached")
        @JvmStatic
        fun PLACE_MINION_AT_LOCATION() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.minion-at-location")
    }

    private val config = Config(
        file,
        stream,
        GeneralSettings.builder().setUseDefaults(false).build(),
        LoaderSettings.builder().setAutoUpdate(true).build(),
        DumperSettings.DEFAULT,
        UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
    )

    fun <T> get(route: String?): T {
        return this.config.get(route)
    }

    fun getFormatted(route: String?): String {
        return StringUtils.formatToString(this.config.get(route))
    }

    fun reload() {
        config.reload()
    }
}