package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.adventurersRepo

class HuntersFocus (): Ability(
        "Hunter's focus",
        "ARCHER",
        "Archer breathes deeply and focuses on theirs tasks, that makes them confident and raise up the spirit",
        20,
        3
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.energyPoints>= energyRequired){
            adventurer.experiencePoints+=(adventurer.level*2.5).toInt()
            adventurer.checkExperience()
            adventurer.experiencePoints.minus(energyRequired)
            adventurersRepo.update(adventurer.id, adventurer)
            true
        } else false
    }
}

val huntersFocus= HuntersFocus()