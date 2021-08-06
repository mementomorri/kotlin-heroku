package model.items

import model.main_classes.adventurersRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class HealingPotion (
        override var quantity: Int
): Item(quantity,"Healing potion","This little warm potion restores little amount of your life energy, tastes like cough syrup", 12) {

    fun useItem(adventurerID: Int) {
        val character= adventurersRepo.read(adventurerID)
        if (character== null){
            return
        }else {
            if (quantity >= 1) {
                character.healthPoints.plus(15)
                if (character.healthPoints > character.maximumHP) character.healthPoints = character.maximumHP
                quantity--
                if (quantity <= 0) transaction {
                    adventurerItemTable.deleteWhere { (adventurerItemTable.adventurer_id eq adventurerID) and(adventurerItemTable.item_id eq this@HealingPotion.id)}
                }
            }
        }
    }

    override fun create(quantity: Int): Item {
        return HealingPotion(quantity)
    }
}