package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.Buff
import model.main_classes.adventurersRepo
import model.main_classes.buffTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class FriendlyProtection (): Ability(
        "Friendly protection",
        "WARRIOR",
        "Warrior stands up for they're nearest friend and protects them from suffer of fail or protects themself with a shield",
        40,
        6
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.energyPoints>= energyRequired){
            transaction {
                buffTable.insert { fill(it, Buff("Friendly protection", adventurer_id = adventurer.id)) }
            }
            adventurer.energyPoints.minus(energyRequired)
            adventurersRepo.update(adventurer.id, adventurer)
            true
        } else false
    }

    fun protectOtherPerson(caster: Adventurer, personToProtect: Adventurer):Boolean{
        return if (caster.energyPoints>= energyRequired){
            transaction {
                buffTable.insert { fill(it, Buff("Friendly protection", adventurer_id = personToProtect.id)) }
            }
            caster.energyPoints.minus(energyRequired)
            adventurersRepo.update(caster.id, caster)
            adventurersRepo.update(personToProtect.id, personToProtect)
            true
        } else false
    }
}

val friendlyProtection= FriendlyProtection()