package model.items

import me.mementomorri.model.main_classes.Objective
import me.mementomorri.model.main_classes.objectiveTable
import me.mementomorri.nextObjectiveId
import model.main_classes.adventurersRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class MagicInBottleScroll(
        override var quantity: Int
): Item(quantity, "Magic in bottle scroll", "Some old scroll describing the story of Magic in bottle", 5) {

    fun useItem(adventurerID: Int) {
        val character= adventurersRepo.read(adventurerID)
        if (character== null){
            return
        }else {
            if (quantity >= 1) {
                transaction {
                    objectiveTable.insert {
                        fill(it, Objective(nextObjectiveId(),"Magic in a bottle","You've been at the beach, looking at the waves taking a walk before you're sleep, everything's as usual, but you accidentally saw bottle drifting at the beach. " +
                                "Once you have opened that bottle you found out that's there is a message in it, that message says:'You need to stretch every two hours when sit at a table for a week'," +
                                " once you read it magic sparks came out and you felt that there is a spell on you. This spell reminds you to always stretch every two hours and to get rid of this spell" +
                                " you need to just keep that habit for a week.", "QUEST", adventurerID, "HARD", null, null, null))
                    }
                }
                quantity--
                if (quantity <= 0) transaction {
                    adventurerItemTable.deleteWhere { (adventurerItemTable.adventurer_id eq adventurerID) and(adventurerItemTable.item_id eq this@MagicInBottleScroll.id)}
                }
            }
        }
    }

    override fun create(quantity: Int): Item {
        return MagicInBottleScroll(quantity)
    }
}