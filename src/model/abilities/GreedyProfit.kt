package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.Buff
import model.main_classes.adventurersRepo
import model.main_classes.buffTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class GreedyProfit (): Ability(
        "Greedy profit",
        "ARCHER",
        "Archer tries shoot every target possible as quickly as they're swiftness let them",
        40,
        6
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.energyPoints>=energyRequired){
            transaction {
                buffTable.insert { fill(it, Buff("Greedy", adventurer_id = adventurer.id)) }
            }
            adventurer.energyPoints.minus(40)
            adventurersRepo.update(adventurer.id, adventurer)
            true
        } else false
    }
}

val greedyProfit= GreedyProfit()