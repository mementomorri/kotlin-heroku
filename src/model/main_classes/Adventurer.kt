package me.mementomorri.model.main_classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.abilities.Ability
import model.abilities.AdventurerAbilityFiller
import model.abilities.adventurerAbilityTable
import model.items.*
import model.main_classes.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import repo.DefaultIdTable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
class Adventurer (
    val name: String,
    val adventurerClass: String,
    var id: Int=-1,
    @SerialName("maximumHP")
    var maximumHP_:Int? =null,
    @SerialName("healthPoints")
    var healthPoints_: Int? = null,
    var energyPoints: Int = 100,
    var experiencePoints: Int = 0,
    var coins: Int = 0,
    var level: Int = 1
){
    @kotlinx.serialization.Transient
    @SerialName("maximumHP_public")
    var maximumHP = maximumHP_ ?: when (AdventurerClass.valueOf(adventurerClass.uppercase())) {
        AdventurerClass.MAGICIAN -> 30
        AdventurerClass.ARCHER -> 40
        AdventurerClass.WARRIOR -> 50
    }

    @kotlinx.serialization.Transient
    @SerialName("healthPoints_public")
    var healthPoints = healthPoints_ ?: maximumHP

    val abilities: List<Ability>
        get() = getAbilitiesList()

    private fun getAbilitiesList(): List<Ability> {
        //reads abilities table and returns abilities this character learned
        val abilitiesIdList = transaction {
            adventurerAbilityTable.selectAll().mapNotNull { adventurerAbilityTable.readResult(it) }
        }.filter { it.adventurer_id == this.id }
        val result = mutableListOf<Ability>()
        abilitiesIdList.forEach {
            result.add(abilitiesRepo.read(it.ability_id)!!)
        }
        return result.toList()
    }

    val buffs: List<Buff>
        get() = getBuffList()

    private fun getBuffList(): List<Buff> {
        //reads buffs table and returns buffs this character affected by
        return transaction {
            buffTable.selectAll().mapNotNull { buffTable.readResult(it) }
        }.filter { it.adventurer_id == this.id }
    }

    val inventory: List<Item>
        get() = getInventoryList()

    private fun getInventoryList(): List<Item> {
        //reads characterItemTable and returns list of items this character owns
        val result = mutableListOf<Item>()
        transaction {
            adventurerItemTable.selectAll().mapNotNull { adventurerItemTable.readResult(it) }
        }.filter { it.adventurer_id == this.id }.forEach { t ->
            val item = shopRepo.read(t.item_id)!!
            result.add(Item(t.quantity, item.name, item.description, item.price, item.id))
        }
        return result.toList()
    }

    private val levelMap: List<Int>
        get() = calculateLevelMap()

    fun learnAbility(abilityId: Int): Boolean {
        //reads abilities repository, checks whether character can learn ability or not and makes changes respectively
        //return true if ability learned successfully, false otherwise
        val ability = abilitiesRepo.read(abilityId)
        return if (ability == null) {
            false
        } else {
            if (this.abilities.contains(ability)) {
                true
            } else {
                if (this.adventurerClass == ability.adventurerClass
                    && this.level >= ability.levelRequired) transaction {
                    adventurerAbilityTable.insertAndGetIdItem(AdventurerAbilityFiller(abilityId, this@Adventurer.id)).value
                    true
                }
                true
            }
        }
    }

    private fun calculateLevelMap(): List<Int> {
        //calculates threshold of each level sequentially and returns it as a list of integers
        val n = 4181
        var t1 = 8
        var t2 = 13
        val result = mutableListOf<Int>()

        while (t1 <= n) {
            result.add(t1)
            val sum = t1 + t2
            t1 = t2
            t2 = sum
        }
        return result.toList()
    }

    fun checkExperience() {
        //checks whether character has enough experience points, levels character up respectively
        val i = levelMap.indexOf(levelMap.firstOrNull { it <= this.experiencePoints })
        if (this.level < (i + 2)) {
            when (AdventurerClass.valueOf(this.adventurerClass.uppercase())) {
                AdventurerClass.MAGICIAN -> this.maximumHP += ((i + 2) - this.level) * 3
                AdventurerClass.ARCHER -> this.maximumHP += ((i + 2) - this.level) * 4
                AdventurerClass.WARRIOR -> this.maximumHP += ((i + 2) - this.level) * 5
            }
            this.level = i + 2
            this.energyPoints = 100
        } else return
    }

    fun buyItem(itemId: Int, quantity: Int) {
        //reads shop repository, checks whether character has enough coins and makes changes respectively
        val itemToBuy = shopRepo.read().firstOrNull { it.id == itemId }
        if (itemToBuy != null && itemToBuy.quantity >= quantity) {
            if (this.coins >= (itemToBuy.price * quantity)) {
                val item = this.inventory.find { it.name == itemToBuy.name }
                if (item != null) {
                    item.quantity += quantity
                    val itemToUptdate = transaction {
                        adventurerItemTable.selectAll().mapNotNull { adventurerItemTable.readResult(it) }
                    }.find { it.item_id == item.id && it.adventurer_id == this.id }
                    transaction {
                        adventurerItemTable.updateItem(itemToUptdate!!.id, AdventurerItemFiller(itemToUptdate.item_id, itemToUptdate.adventurer_id, item.quantity, itemToUptdate.id))
                    }
                } else transaction {
                    adventurerItemTable.insertAndGetIdItem(AdventurerItemFiller(itemToBuy.id, this@Adventurer.id, quantity))
                    true
                }
            }
        } else return
    }

    fun addItemToInventory(item: Item): Boolean {
        //adds item possession to characterItemTable, returns true if added successfully, false otherwise
        val itemFromInventory = this.inventory.firstOrNull { it.name == item.name }
        return if (itemFromInventory != null) {
            itemFromInventory.quantity += item.quantity
            val t = transaction {
                adventurerItemTable.selectAll().mapNotNull { adventurerItemTable.readResult(it) }
            }.firstOrNull { it.adventurer_id == this.id && it.item_id == itemFromInventory.id }
            transaction {
                adventurerItemTable.updateItem(t!!.id, AdventurerItemFiller(t.item_id, t.adventurer_id, itemFromInventory.quantity, t.id)) > 0
            }
            true
        } else {
            val neededId= transaction {
                itemTable.selectAll().mapNotNull { itemTable.readResult(it) }
            }.firstOrNull{ it.name == item.name }
            transaction {
                if (neededId != null) {
                    adventurerItemTable.insertAndGetIdItem(AdventurerItemFiller(neededId.id, this@Adventurer.id, item.quantity)).value
                }
                true
            }
        }
    }

    fun removeItemFromInventory(itemId: Int): Boolean {
        //removes item possession from characterItemTable, returns true if removed successfully, false otherwise
        val itemFromInventory = this.inventory.firstOrNull { it.id == itemId }
        return if (itemFromInventory != null) {
            transaction {
                adventurerItemTable.deleteWhere { (adventurerItemTable.adventurer_id eq itemId) and (adventurerItemTable.item_id eq this@Adventurer.id) } > 0
            }
        } else true
    }
}

class AdventurerTable: DefaultIdTable<Adventurer>(){
    val name = varchar("name", 50)
    val adventurerClass= varchar("adventurerClass", 50)
    var level= integer("leve")
    var maximumHP= integer("maximumHP")
    var healthPoints= integer("healthPoints")
    var energyPoints= integer("energyPoints")
    var experiencePoints= integer("experiencePoints")
    var coins= integer("coins")

    override fun fill(builder: UpdateBuilder<Int>, item: Adventurer) {
        builder[name] = item.name
        builder[adventurerClass] = item.adventurerClass
        builder[level] = item.level
        builder[maximumHP] = item.maximumHP
        builder[healthPoints] = item.healthPoints
        builder[energyPoints] = item.energyPoints
        builder[experiencePoints] = item.experiencePoints
        builder[coins] = item.coins
    }

    override fun readResult(result: ResultRow): Adventurer? =
        Adventurer(
            result[name],
            result[adventurerClass],
            result[id].value,
            result[maximumHP],
            result[healthPoints],
            result[energyPoints],
            result[experiencePoints],
            result[coins],
            result[level]
        )
}

val adventurerTable= AdventurerTable()