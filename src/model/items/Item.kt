package model.items

import kotlinx.serialization.Serializable
import me.mementomorri.model.main_classes.adventurerTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.DefaultIdTable

@Serializable
open class Item(
        open var quantity: Int,
        val name: String,
        val description: String,
        val price: Int,
        open val id: Int= -1
){

    open fun create(quantity: Int): Item?=Item(quantity, name, description, price)

    override fun toString(): String {
        return "Item with: id=${id}, name=${name}, quantity=${quantity}"
    }
}

fun Item.useCoffee(adventurerId: Int){
    Coffee(this.quantity).useItem(adventurerId)
}

fun Item.useDishDisasterScroll(adventurerId: Int){
    DishDisasterScroll(this.quantity).useItem(adventurerId)
}

fun Item.useDustRabbitsScroll(adventurerId: Int){
    DustRabbitsScroll(this.quantity).useItem(adventurerId)
}

fun Item.useGreenTea(adventurerId: Int){
    GreenTea(this.quantity).useItem(adventurerId)
}

fun Item.useHealingPotion(adventurerId: Int){
    HealingPotion(this.quantity).useItem(adventurerId)
}

fun Item.useMagicInBottleScroll(adventurerId: Int){
    MagicInBottleScroll(this.quantity).useItem(adventurerId)
}

class AdventurerItemFiller(
    val item_id: Int,
    val adventurer_id: Int,
    var quantity: Int= 1,
    val id: Int= -1
){
    override fun toString(): String {
        return "id=$id, item_id=$item_id, adventurer_id=$adventurer_id, quantity=$quantity"
    }
}

class ItemsTable: DefaultIdTable<Item>(){
    var quantity= integer("quantity")
    val name= varchar("name", 50)
    val description= varchar("description", 510)
    val price= integer("price")
    override fun fill(builder: UpdateBuilder<Int>, item: Item) {
        builder[quantity]= item.quantity
        builder[name]= item.name
        builder[description]= item.description
        builder[price]= item.price
    }

    override fun readResult(result: ResultRow): Item?=
            Item(
                    result[quantity],
                    result[name],
                    result[description],
                    result[price],
                    result[id].value
            )
}

val itemTable= ItemsTable()

class AdventurerItemTable: DefaultIdTable<AdventurerItemFiller>(){
    val item_id= reference("item_id", itemTable)
    val adventurer_id= reference("adventurer_id", adventurerTable)
    var quantity= integer("quantity")

    override fun fill(builder: UpdateBuilder<Int>, item: AdventurerItemFiller) {
        builder[item_id]= item.item_id
        builder[adventurer_id]= item.adventurer_id
        builder[quantity]= item.quantity
    }

    override fun readResult(result: ResultRow)=
            AdventurerItemFiller(
                    result[item_id].value,
                    result[adventurer_id].value,
                    result[quantity],
                    result[id].value
            )
}

val adventurerItemTable= AdventurerItemTable()