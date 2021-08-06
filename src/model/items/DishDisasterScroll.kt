package model.items

import me.mementomorri.model.main_classes.Objective
import me.mementomorri.model.main_classes.objectiveTable
import me.mementomorri.nextObjectiveId
import model.main_classes.adventurersRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class DishDisasterScroll (
        override var quantity: Int
): Item(quantity,"Dish disaster scroll","Some old scroll describing the story of Dish disaster",7) {

    fun useItem(adventurerId: Int){
        val character= adventurersRepo.read(adventurerId)
        if (character == null){
            return
        }else {
            if (quantity >= 1) {
                transaction {
                    objectiveTable.insert {
                        fill(it, Objective(nextObjectiveId(),"Dish disaster",
                            "You walk around Creaky-Clean Lake for some well-earned relaxation." +
                                    "But the lake is polluted with unwashed dishes! How did this happen? Well, you simply cannot allow the lake to be in such a state. " +
                                    "There is only one thing you can do: clean the dishes and save your vacation spot! Better find some soap to clean up this mess. A lot of soap...",
                        "QUEST",adventurerId,"HARD", null,null,null))
                    }
                }
                quantity--
                if (quantity <= 0) transaction {
                    adventurerItemTable.deleteWhere { (adventurerItemTable.adventurer_id eq adventurerId) and(adventurerItemTable.item_id eq this@DishDisasterScroll.id)}
                }
            }
        }
    }

    override fun create(quantity: Int): Item {
        return DishDisasterScroll(quantity)
    }
}