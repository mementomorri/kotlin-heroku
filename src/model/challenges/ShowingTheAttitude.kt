package model.challenges

import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.Reward
import model.main_classes.objectivesRepo

class ShowingTheAttitude ():Challenge(
        "Showing the attitude",
        "Complete very hard daily at least 5 times"
) {
    override val rewards: Reward = Reward(0, 20, null)

    override fun checkChallengeCondition(adventurer: Adventurer): Boolean {
        return objectivesRepo.read().filter {it.adventurerId == adventurer.id && it.type=="DAILY" }.firstOrNull{it.completionCount!!>= 5} != null
    }
}

val showingTheAttitude= ShowingTheAttitude()