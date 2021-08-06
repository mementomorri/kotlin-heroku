package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.adventurersRepo

class BurstsOfFlame(): Ability(
        "Bursts of flame",
        "MAGICIAN",
        "Magician focuses at they're target so there is little sparks around it",
        20,
        3
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.energyPoints >= energyRequired){
            adventurer.experiencePoints+=(adventurer.level*3)
            adventurer.checkExperience()
            adventurer.energyPoints.minus(energyRequired)
            adventurersRepo.update(adventurer.id, adventurer)
            true
        } else false
    }
}

val burstsOfFlame = BurstsOfFlame()