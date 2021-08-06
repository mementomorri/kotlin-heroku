package model.abilities

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.objectivesRepo
import java.time.format.DateTimeFormatter
import java.time.LocalDate

class FrostChill(): Ability(
        "Frost chill",
        "MAGICIAN",
        "Magician spins around them and makes air around become cold, time freezes and stops for a while",
        40,
        9
) {
    fun useAbility(adventurer: Adventurer): Boolean {
        return if (adventurer.energyPoints >= energyRequired){
            objectivesRepo.read().filter { it.adventurerId == adventurer.id && it.type == "DAILY" }.forEach {
                LocalDate.parse(it.deadline.toString(), DateTimeFormatter.ISO_DATE)?.plusDays(1)
            }
            objectivesRepo.read().filter { it.adventurerId == adventurer.id && it.type == "TODO" }.forEach {
                LocalDate.parse(it.deadline.toString(), DateTimeFormatter.ISO_DATE)?.plusDays(1)
            }
            adventurer.energyPoints.minus(energyRequired)
            true
        } else false
    }
}

val frostChill= FrostChill()