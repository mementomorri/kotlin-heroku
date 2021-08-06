package model.items

import me.mementomorri.model.main_classes.Objective
import me.mementomorri.model.main_classes.objectiveTable
import me.mementomorri.nextObjectiveId
import model.main_classes.adventurersRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class DustRabbitsScroll (
        override var quantity: Int
): Item(quantity,"Dust rabbits scroll", "Some old scroll describing the story about Dust rabbits disaster", 6) {

    fun useItem(adventurerId: Int) {
        val character= adventurersRepo.read(adventurerId)
        if (character == null){
            return
        } else {
            if (quantity >= 1) {
                transaction {
                    objectiveTable.insert {
                        fill(it, Objective(nextObjectiveId(), "Dust rabbits","Word on the streets: there is dust rabbits at the desert near our town, they're eating dust, reproduce themself and spread dust around " +
                                "to populate the area, your mission is to clean up every corner at your home, that should make them stop and leave our great town of productive folks.",
                        "QUEST",adventurerId,"VERYHARD",null,null,null))
                    }
                }
                transaction {
                    adventurerItemTable.updateItem(this@DustRabbitsScroll.id, AdventurerItemFiller(this@DustRabbitsScroll.id, adventurerId, this@DustRabbitsScroll.quantity--, this@DustRabbitsScroll.id))
                }
                if (quantity-- <= 0) transaction {
                    adventurerItemTable.deleteWhere { (adventurerItemTable.adventurer_id eq adventurerId) and(adventurerItemTable.item_id eq this@DustRabbitsScroll.id)}
                }
            }
        }
    }

    override fun create(quantity: Int): Item {
        return DustRabbitsScroll(quantity)
    }
}