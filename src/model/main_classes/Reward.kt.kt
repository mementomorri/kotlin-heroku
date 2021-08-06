package model.main_classes

import kotlinx.serialization.Serializable
import me.mementomorri.model.main_classes.Adventurer
import model.items.AdventurerItemFiller
import model.items.Item
import model.items.adventurerItemTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
class Reward(
    val experiencePoints: Int,
    val coins: Int,
    val items: List<Item>?
){
    fun getReward(adventurer: Adventurer){
        adventurer.experiencePoints+=experiencePoints
        adventurer.checkExperience()
        adventurer.coins+=coins
        items?.forEach { item ->
            val itemFromInventory = adventurer.inventory.firstOrNull { it.name == item.name }
            if (itemFromInventory != null) {
                itemFromInventory.quantity += item.quantity
                val t = transaction {
                    adventurerItemTable.selectAll().mapNotNull { adventurerItemTable.readResult(it) }
                }.firstOrNull { it.adventurer_id == adventurer.id && it.item_id == itemFromInventory.id }
                transaction {
                    adventurerItemTable.updateItem(t!!.id, AdventurerItemFiller(t.item_id, t.adventurer_id, itemFromInventory.quantity, t.id)) > 0
                }
            } else {
                val t = shopRepo.read().firstOrNull{ it.name == item.name }
                if (t != null) {
                    transaction {
                        adventurerItemTable.insertAndGetIdItem(AdventurerItemFiller(t.id, adventurer.id, item.quantity)).value
                        true
                    }
                } else {
                    shopRepo.create(item)
                    val v = shopRepo.read().firstOrNull { it.name == item.name }
                    transaction {
                        adventurerItemTable.insertAndGetIdItem(AdventurerItemFiller(v?.id!!, adventurer.id, item.quantity)).value
                        true
                    }
                }
            }
        }
        adventurersRepo.update(adventurer.id, adventurer)
    }
    fun getGreedyReward(adventurer: Adventurer){
        adventurer.experiencePoints+=(experiencePoints*1.5).toInt()
        adventurer.checkExperience()
        adventurer.coins+= (coins*1.5).toInt()
        items?.forEach { item ->
            val itemFromInventory = adventurer.inventory.firstOrNull { it.name ==  item.name}
            if (itemFromInventory != null) {
                itemFromInventory.quantity+=item.quantity
                val t = transaction {
                    adventurerItemTable.selectAll().mapNotNull { adventurerItemTable.readResult(it) }
                }.firstOrNull { it.adventurer_id == adventurer.id && it.item_id == itemFromInventory.id}
                transaction {
                    adventurerItemTable.updateItem(t!!.id, AdventurerItemFiller(t.item_id, t.adventurer_id, itemFromInventory.quantity, t.id)) >0
                }
            }else transaction {
                adventurerItemTable.insertAndGetIdItem(AdventurerItemFiller(item.id,adventurer.id, item.quantity)).value
                true
            }
        }
        adventurersRepo.update(adventurer.id, adventurer)
    }
}