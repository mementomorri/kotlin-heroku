package model.items

import model.main_classes.adventurersRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class Coffee (
        override var quantity: Int,
        override val id: Int = -1
): Item(quantity,"Coffee", "Nice and warm cup of coffee to raise an adventurer spirit", 20, id = id ) {

    fun useItem(adventurerID: Int) {
        val adventurer= adventurersRepo.read(adventurerID)
         if (adventurer == null){
            return
        }else {
            if (quantity >= 1) {
                adventurer.energyPoints.plus(30)
                if (adventurer.energyPoints > 100) adventurer.energyPoints = 100
                quantity--
                if (quantity <= 0) transaction {
                 adventurerItemTable.deleteWhere { adventurerItemTable.adventurer_id eq adventurerID and(adventurerItemTable.item_id eq this@Coffee.id)}
                }
            }
        }
    }

    override fun create(quantity: Int): Item {
        return Coffee(quantity)
    }
}