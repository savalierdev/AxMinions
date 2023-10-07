package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.entity.PacketEntityFactory
import com.artillexstudios.axapi.entity.impl.PacketArmorStand
import com.artillexstudios.axapi.entity.impl.PacketEntity
import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axapi.utils.EquipmentSlot
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.RotationType
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.warnings.Warning
import com.artillexstudios.axminions.api.warnings.Warnings
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import java.util.UUID
import kotlin.math.roundToInt

class Minion(
    private val location: Location,
    private val ownerUUID: UUID,
    private val owner: OfflinePlayer,
    private val type: MinionType,
    private var level: Int,
    private var tool: ItemStack?,
    private var linkedChest: Location?,
    private var direction: Direction,
    private var actions: Long,
    private var storage: Double,
    private val locationID: Int,
    private var chestLocationId: Int
) : Minion {
    private lateinit var entity: PacketArmorStand
    private var nextAction = 0
    private var range = 0.0
    private var dirty = true
    private var armTick = 2.0
    private var warning: Warning? = null
    private var hologram: Hologram? = null
    private val extraData = hashMapOf<String, String>()
    private var linkedInventory: Inventory? = null

    init {
        spawn()
        Minions.load(this)
        linkedInventory = (linkedChest?.block?.blockData as? Container)?.inventory
    }

    override fun getType(): MinionType {
        return this.type
    }

    override fun spawn() {
        entity = PacketEntityFactory.get().spawnEntity(location, EntityType.ARMOR_STAND) as PacketArmorStand
        entity.setHasBasePlate(false)
        entity.setSmall(true)
        updateArmour()
        entity.onClick { event ->
            if (event.isAttack) {
                println("LEFT CLICKED!")
            } else {
                println("RIGHT CLICKED!")
            }
        }
    }

    override fun tick() {
        if (dirty) {
            dirty = false
            range = type.getDouble("range", level)
            val efficiency = 1.0 - (getTool()?.getEnchantmentLevel(Enchantment.DIG_SPEED)?.div(10.0) ?: 0.1)
            nextAction = (type.getLong("speed", level) * efficiency).roundToInt()
        }

        type.tick(this)
        animate()
    }

    override fun getLocation(): Location {
        return this.location
    }

    override fun updateInventory(inventory: Inventory) {

    }

    override fun openInventory(player: Player) {

    }

    override fun getAsItem(): ItemStack {
        return type.getItem(level)
    }

    override fun getLevel(): Int {
        return this.level
    }

    override fun setActions(actions: Long) {
        this.actions = actions
    }

    override fun setStorage(storage: Double) {
        this.storage = storage
    }

    override fun setWarning(warning: Warning?) {
        this.warning = warning
    }

    override fun getWarning(): Warning? {
        return this.warning
    }

    override fun setWarningHologram(hologram: Hologram?) {
        this.hologram = hologram
    }

    override fun getWarningHologram(): Hologram? {
        return this.hologram
    }

    override fun getOwner(): OfflinePlayer {
        return this.owner
    }

    override fun getOwnerUUID(): UUID {
        return this.ownerUUID
    }

    override fun setTool(tool: ItemStack) {
        this.tool = tool
    }

    override fun getTool(): ItemStack? {
        return this.tool
    }

    override fun getEntity(): PacketEntity {
        return this.entity
    }

    override fun setLevel(level: Int) {
        this.level = level
    }

    override fun getData(key: String): String? {
        return extraData[key]
    }

    override fun hasData(key: String): Boolean {
        return extraData.containsKey(key)
    }

    override fun getNextAction(): Int {
        return this.nextAction
    }

    override fun getActionAmount(): Long {
        return this.actions
    }

    override fun getStorage(): Double {
        return this.storage
    }

    override fun getRange(): Double {
        return this.range
    }

    override fun resetAnimation() {
        armTick = 0.0
    }

    override fun animate() {
        if (armTick >= 2) return

        entity.setRotation(RotationType.RIGHT_ARM, EulerAngle(-2 + armTick, 0.0, 0.0))
        armTick += 0.2
    }

    override fun setLinkedChest(location: Location?) {
        this.linkedChest = location?.clone()
        linkedInventory = (linkedChest?.block?.blockData as? Container)?.inventory
    }

    override fun getLinkedChest(): Location? {
        return this.linkedChest
    }

    override fun serializeExtraData(): String {
        val builder = StringBuilder()
        for (data in extraData) {
            builder.append(data.key)
            builder.append("=")
            builder.append(data.value)
            builder.append("|")
        }

        return builder.toString()
    }

    override fun setDirection(direction: Direction) {
        this.direction = direction
        location.yaw = direction.yaw
        entity.teleport(location)
    }

    override fun getDirection(): Direction {
        return this.direction
    }

    override fun remove() {
        Warnings.remove(this)
        Minions.remove(this)
        entity.remove()
    }

    override fun getLinkedInventory(): Inventory? {
        return linkedInventory
    }

    override fun addToContainerOrDrop(itemStack: ItemStack) {
        val remaining = linkedInventory?.addItem(itemStack)

        remaining?.forEach { (_, u) ->
            location.world?.dropItem(location, u)
        }
    }

    override fun addToContainerOrDrop(itemStack: Iterable<ItemStack>) {
        itemStack.forEach {
            addToContainerOrDrop(it)
        }
    }

    override fun updateArmour() {
        for (entry in EquipmentSlot.entries) {
            entity.setItem(entry, ItemStack(Material.AIR))
        }

        type.getSection("items.helmet", level)?.let {
            println("helmet!")
            entity.setItem(EquipmentSlot.HELMET, ItemBuilder(it).get())
        }

        type.getSection("items.chestplate", level)?.let {
            println("CP!")
            entity.setItem(EquipmentSlot.CHEST_PLATE, ItemBuilder(it).get())
        }

        type.getSection("items.leggings", level)?.let {
            println("legs!")
            entity.setItem(EquipmentSlot.LEGGINGS, ItemBuilder(it).get())
        }

        type.getSection("items.boots", level)?.let {
            println("boots!")
            entity.setItem(EquipmentSlot.BOOTS, ItemBuilder(it).get())
        }
    }

    override fun getLocationId(): Int {
        return this.locationID
    }

    override fun getChestLocationId(): Int {
        return this.chestLocationId
    }
}