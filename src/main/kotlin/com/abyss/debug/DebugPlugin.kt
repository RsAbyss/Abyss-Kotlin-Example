package com.abyss.debug

import abyss.plugin.api.*
import abyss.plugin.api.Client.WOODCUTTING
import abyss.plugin.api.actions.MenuAction
import abyss.plugin.api.game.actionbar.ActionBar
import abyss.plugin.api.world.WorldTile.Companion.expand

class DebugPlugin : Plugin() {

    private var isRunning = true
    private val area = Vector3i(3174, 3297, 0).expand(8)
    private val ivyArea = Vector3i(2417, 3060, 0).expand(8)
    private val castleWars = Vector3i(2443, 3088, 0).expand(8)
    private var hasInteracted = false
    private var lastTickSinceInteraction = 0L
    private var logs = intArrayOf(1511, 1521)
    private lateinit var ctx: PluginContext

    private val ivy = Ivy.values()

    override fun onLoaded(pluginContext: PluginContext): Boolean {
        ctx = pluginContext
        pluginContext.name = "Woodcutting and Fletching"
        pluginContext.category = "DrJavatar"
        load(pluginContext)
        //isRunning = getBoolean("running")
        return true
    }

    override fun onServerTick(self: Player, tickCount: Long): Int {
        if(!isRunning) return 0
        val stat = Client.getStatById(WOODCUTTING)

        if(Inventory.isFull() || Widgets.isOpen(1251)) {
            Debug.log("Fletching Logs")
            return fletchLogs()
        }

        if(!self.isMoving && !self.isAnimationPlaying && hasInteracted && (tickCount - lastTickSinceInteraction) >= Rng.i32(2, 5)) {
            hasInteracted = false
        }

        if(stat.current >= 68) {
            if(self.isMoving || self.isAnimationPlaying) {
                return 0
            }
            if(castleWars.contains(self)) {
                Move.to(ivyArea.random())
                return 0
            }
            return chopIvy(self, tickCount)
        }
        /*val tile = area.random()
        if(self.globalPosition.distance(tile) > 35 && !area.contains(self) && !hasInteracted) {
            Debug.log("Walking to Location")
            return walkToLocation(self, tickCount, tile)
        }*/
        return 0
    }

    private fun chopIvy(self: Player, tickCount: Long) : Int {
        val ivys = SceneObjects.all { !it.hidden() && (it.id == 46320 || it.id == 46322 || it.id == 46324 || it.id == 46318) }
        val ivy = ivys.minBy { it.globalPosition.distance(self.globalPosition) }
        if(ivy != null) {
            ivy.interact(Actions.MENU_EXECUTE_OBJECT1)
            hasInteracted = true
            lastTickSinceInteraction = tickCount
        } else {
            Debug.log("No ivy found.")
        }
        return 0
    }

    private val blackList = mutableListOf<Int>()

    private fun chopOakLogs(self: Player, tickCount: Long) : Int {
        if(self.isAnimationPlaying || self.isMoving) {
            Debug.log("Waiting to finish chopping...")
            return Rng.i32(0, 2)
        }
        val stat = Client.getStatById(Client.FLETCHING)
        val tree = SceneObjects.closest { it.isReachable && it.id !in blackList && it.name.startsWith(if(stat.current >= 15) "oak" else "tree", true) && !it.hidden() }
        if(tree == null) {
            return 0
        }
        Debug.log("Interacting With Tree ${tree.interactId}")
        if(!tree.interact("chop down")) {
            Debug.log("Adding to black list ${tree.id}")
            blackList.add(tree.id)
            return Rng.i32(1, 3)
        }
        hasInteracted = true
        lastTickSinceInteraction = tickCount
        return Rng.i32(0, 2)
    }

    private fun fletchLogs() : Int {
        if(Widgets.isOpen(1251)) {
            return Rng.i32(0, 3)
        } else if(Widgets.isOpen(1179)) {
            Actions.menu(Actions.MENU_EXECUTE_DIALOGUE, 0, -1, 77266961, 1)
            return Rng.i32(1, 3)
        } else if(Widgets.isOpen(1370)) {
            Actions.menu(Actions.MENU_EXECUTE_DIALOGUE, 0, -1, 89784350, Rng.i32(0, Int.MAX_VALUE))
            return Rng.i32(1, 3)
        }
        val log = Inventory.first { it.id in logs }
        if(log == null) return Rng.i32(0, 5)
        val actionSlot = ActionBar.forItem(log.id)

        if(actionSlot != null) {
            actionSlot.interact(MenuAction.WIDGET)
        } else if(!log.interact(1)) {
            return Rng.i32(1, 2)
        }
        return 0
    }

    private fun walkToLocation(self: Player, tickCount: Long, tile: Vector3i): Int {
        //Lodestones.LUMBRIDGE.interact()
        if(self.isMoving) return 0
        Move.to(area.random())
        return Rng.i32(0, 5)
    }

    override fun onPaint() {
        if(isRunning) {
            if(ImGui.button("Stop")) {
                isRunning = false
                setAttribute("running", false)
                save(ctx)
            }
        } else {
            if(ImGui.button("Start")) {
                isRunning = true
                setAttribute("running", false)
                save(ctx)
            }
        }
    }
}