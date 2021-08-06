package model.challenges

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.Reward
import model.main_classes.objectivesRepo

class SolidHabit ():Challenge(
        "Solid habit",
        "Get at least one solid habit, complete it at least 90 times"
) {
    override val rewards: Reward = Reward(0, 15, null)

    override fun checkChallengeCondition(adventurer: Adventurer): Boolean {
        return objectivesRepo.read().filter {it.adventurerId == adventurer.id && it.type=="HABITS" }.firstOrNull{ it.completionCount!! >= 90} != null
    }
}

val solidHabit= SolidHabit()