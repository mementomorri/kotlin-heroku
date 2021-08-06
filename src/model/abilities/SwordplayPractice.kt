package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.adventurersRepo

class SwordplayPractice(): Ability(
        "Swordplay practice",
        "WARRIOR",
        "Warrior focuses on they're swordplay skill while swinging it at the dummy ",
        20,
        3
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.energyPoints>= energyRequired){
            adventurer.experiencePoints+=(adventurer.level*2)
            adventurer.checkExperience()
            adventurer.energyPoints.minus(energyRequired)
            adventurersRepo.update(adventurer.id, adventurer)
            true
        }else false
    }
}

val swordplayPractice= SwordplayPractice()