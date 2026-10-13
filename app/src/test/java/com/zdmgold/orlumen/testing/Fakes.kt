package com.zdmgold.orlumen.testing

import android.app.Activity
import com.zdmgold.orlumen.core.database.ConfigDao
import com.zdmgold.orlumen.core.database.PaletteDao
import com.zdmgold.orlumen.core.database.PaletteEntity
import com.zdmgold.orlumen.core.database.PresetEntity
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.platform.monetization.BillingGateway
import com.zdmgold.orlumen.platform.monetization.Ownership
import com.zdmgold.orlumen.platform.monetization.ProProductInfo
import com.zdmgold.orlumen.platform.monetization.PurchaseEvent
import com.zdmgold.orlumen.platform.monetization.PurchaseLaunchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeConfigDao : ConfigDao {
    private val rows = MutableStateFlow<List<PresetEntity>>(emptyList())
    private var nextId = 1L

    override fun observePresets(kind: DesignKind): Flow<List<PresetEntity>> = rows.map { list ->
        list.filter { it.kind == kind }
    }

    override suspend fun getPreset(id: Long): PresetEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun insert(preset: PresetEntity): Long {
        val id = nextId++
        rows.value = rows.value + preset.copy(id = id)
        return id
    }

    override suspend fun update(preset: PresetEntity) {
        rows.value = rows.value.map { if (it.id == preset.id) preset else it }
    }

    override suspend fun delete(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }
}

class FakePaletteDao : PaletteDao {
    private val rows = MutableStateFlow<List<PaletteEntity>>(emptyList())

    override fun observeAll(): Flow<List<PaletteEntity>> = rows

    override suspend fun getById(id: String): PaletteEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun upsert(palette: PaletteEntity) {
        rows.value = rows.value.filterNot { it.id == palette.id } + palette
    }

    override suspend fun delete(id: String) {
        rows.value = rows.value.filterNot { it.id == id }
    }
}

class FakeGateway : BillingGateway {
    override val ownership = MutableStateFlow(Ownership.UNKNOWN)
    override val product = MutableStateFlow<ProProductInfo?>(null)
    override val events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 1)
    var connectCalls = 0
    var refreshCalls = 0

    override fun connect() {
        connectCalls++
    }

    override fun refresh() {
        refreshCalls++
    }

    override fun launchPurchase(activity: Activity): PurchaseLaunchResult = PurchaseLaunchResult.LAUNCHED
}
