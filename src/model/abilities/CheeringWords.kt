package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.adventurersRepo

class CheeringWords(): Ability(
        "Cheering words",
        "MAGICIAN",
        "Magician says some inspiring words to other person that heals they're heart or inspires themself by thinking about good things",
        40,
        6
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.maximumHP == adventurer.healthPoints || adventurer.energyPoints< energyRequired){
            false
        } else {
            adventurer.healthPoints.plus((adventurer.level*4))
            if (adventurer.healthPoints>adventurer.maximumHP) adventurer.healthPoints=adventurer.maximumHP
            adventurer.energyPoints.minus(energyRequired)
            adventurersRepo.update(adventurer.id, adventurer)
            true
        }
    }

    fun healOtherPerson(caster: Adventurer, personToHeal: Adventurer): Boolean{
        return if (personToHeal.maximumHP == personToHeal.healthPoints || caster.energyPoints< energyRequired){
            false
        } else {
            personToHeal.healthPoints.plus(caster.level*4)
            if (personToHeal.healthPoints> personToHeal.maximumHP) personToHeal.healthPoints=(personToHeal.maximumHP)
            caster.energyPoints.minus(energyRequired)
            adventurersRepo.update(caster.id, caster)
            adventurersRepo.update(personToHeal.id, personToHeal)
            true
        }
    }
}

val cheeringWords= CheeringWords()