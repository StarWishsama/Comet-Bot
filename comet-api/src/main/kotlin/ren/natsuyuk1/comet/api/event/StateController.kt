/*
 * Copyright (c) 2022- Sorapointa
 *
 * 此源代码的使用受 Apache-2.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the Apache-2.0 License which can be found through the following link.
 *
 * https://github.com/Sorapointa/Sorapointa/blob/master/LICENSE
 */

@file:Suppress("unused")

package ren.natsuyuk1.comet.api.event

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.util.concurrent.ConcurrentHashMap

/**
 * Interface for different state class
 *
 * You should implement it with your custom state interface
 *
 * ```
 * interface SomeClassWithState : WithState<SomeClassWithState.State> {
 *
 *  // This is a method you wanted to be differential implementation with different states
 *  fun foobar(): String
 *
 *  // An enum includes all your states
 *  enum class State {
 *      START,
 *      DOING,
 *      END
 *  }
 * }
 * ```
 * @property state indicated state of this class
 * @see StateController
 */
interface WithState<out T : Enum<*>> {

    val state: T

    /**
     * This method would be called when this state starts
     *
     * @see StateController.setState
     */
    suspend fun startState() {
    }

    /**
     * This method would be called when this state ends
     *
     * @see StateController.setState
     */
    suspend fun endState() {
    }
}

/**
 * A simple state controller
 *
 * Generic [TState] is an enum included all states of this controller,
 * [TInterfaceWithState] is your custom implementation of [WithState] interface,
 * [TClassWithState] is your parent state that would
 * be a receiver in state observer, and interceptor lambda.
 *
 * You should call [StateController.init] to make sure
 * your first state has been correctly start with [WithState.startState].
 *
 * You should use [StateController] like following example:
 *
 * ```
 * class SomeClassWithStateImpl {
 *
 *  val count = atomic(0)
 *
 *  val stateController = InitStateController(
 *      scope = ModuleScope("TestScopeWithState"),
 *      parentStateClass = this,
 *      Start(), Doing(), End(),
 *  )
 *
 *
 *  fun foobar(): String {
 *      return stateController.getStateInstance().foobar()
 *  }
 *
 *  inner class Start : SomeClassWithState {
 *      override val state: SomeClassWithState.State =
 *          SomeClassWithState.State.START
 *
 *      override fun foobar(): String {
 *          return "Start!"
 *      }
 *
 *      override suspend fun endState() {
 *          count.getAndIncrement()
 *          println("Start!")
 *      }
 *  }
 *
 *  inner class Doing : SomeClassWithState {
 *
 *      override val state: SomeClassWithState.State =
 *          SomeClassWithState.State.DOING
 *
 *      override fun foobar(): String {
 *          return "Doing!"
 *      }
 *
 *      override suspend fun startState() {
 *          count.getAndIncrement()
 *          println("Doing!")
 *      }
 *
 *      override suspend fun endState() {
 *          count.getAndIncrement()
 *          println("Done!")
 *      }
 *  }
 *
 *  inner class End : SomeClassWithState {
 *
 *      override val state: SomeClassWithState.State =
 *          SomeClassWithState.State.END
 *
 *      override fun foobar(): String {
 *          return "End!"
 *      }
 *
 *      override suspend fun startState() {
 *          count.getAndIncrement()
 *          println("End!")
 *      }
 *  }
 * }
 * ```
 *
 *
 * @param scope [ModuleScope] will provide a coroutine scope during the state transfering
 * @param parentStateClass is your parent state that would
 * be a receiver in state observer, and interceptor lambda
 * @see WithState
 */
open class StateController<TState : Enum<*>, TInterfaceWithState : WithState<TState>, TClassWithState>(
    protected var scope: ModuleScope,
    protected var parentStateClass: TClassWithState,
    firstState: TInterfaceWithState
) {

    protected val currentState = atomic(firstState)

    private var observers = ConcurrentHashMap<suspend TClassWithState.(TState, TState) -> Unit, ListenerState>()
    private var interceptors = ConcurrentHashMap<suspend TClassWithState.(TState, TState) -> Boolean, ListenerState>()

    /**
     * Init state controller, to call the first state [WithState.startState]
     */
    suspend fun init() {
        currentState.value.startState()
    }

    /**
     * Get current enum state
     */
    fun getCurrentState(): TState =
        currentState.value.state

    /**
     * Get current instance state
     */
    fun getStateInstance(): TInterfaceWithState {
        return currentState.value
    }

    /**
     * Transfer state from current state to specfied state
     *
     * It will call all observers in parallel, all interceptors in serial during transfering,
     * if there is an intercetpor with [ListenerState.BEFORE_UPDATE] priority,
     * it could intercept and cancel this transfering.
     *
     * @see ListenerState
     * @see observeStateChange
     * @see interceptStateChange
     * @param after transfer to this state, instance type [TInterfaceWithState]
     */
    suspend fun setState(after: TInterfaceWithState): TInterfaceWithState {
        val before = currentState.value
        val beforeState = before.state
        val afterState = after.state
        if (invokeChange(beforeState, afterState, ListenerState.BEFORE_UPDATE, parentStateClass)) return before
        before.endState()
        currentState.update { after }
        after.startState()
        invokeChange(beforeState, afterState, ListenerState.AFTER_UPDATE, parentStateClass)
        return before
    }

    private suspend fun invokeChange(
        beforeState: TState,
        afterState: TState,
        listenerState: ListenerState,
        listenerCaller: TClassWithState
    ): Boolean {
        var isIntercepted by atomic(false)
        observers
            .asSequence()
            .filter { it.value == listenerState }
            .forEach { (observer, _) ->
                scope.launch {
                    listenerCaller.observer(beforeState, afterState)
                }
            }
        interceptors
            .asSequence()
            .asFlow()
            .filter { it.value == listenerState }
            .map { (interceptor, _) ->
                scope.launch {
                    isIntercepted = listenerCaller.interceptor(beforeState, afterState) || isIntercepted
                }
            }
            .collect { it.join() }
        return isIntercepted
    }

    /**
     * Observe a state change
     *
     * [observer] will be called in parallel
     *
     * @param listenerState a [ListenerState] to indicate invocation priority
     * @param observer observation lambda block with [TClassWithState] context
     * @see observe
     * @see setState
     */
    fun observeStateChange(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        observer: suspend TClassWithState.(TState, TState) -> Unit
    ) {
        observers[observer] = listenerState
    }

    /**
     * Intercept a state change
     *
     * [interceptor] will be called in serial
     *
     * @param listenerState a [ListenerState] to indicate invocation priority
     * @param interceptor interception lambda block with [TClassWithState] context
     * @see block
     * @see intercept
     * @see setState
     */
    fun interceptStateChange(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        interceptor: suspend TClassWithState.(TState, TState) -> Boolean
    ) {
        interceptors[interceptor] = listenerState
    }

    fun cleanAllObserver() {
        observers.clear()
    }

    fun cleanAllInterceptor() {
        interceptors.clear()
    }

    /**
     * Observer or interceptor's priority
     *
     * [setState] will call all observers in parallel, all interceptors in serial during transfering,
     * if there is an intercetpor with [ListenerState.BEFORE_UPDATE] priority,
     * it could intercept and cancel the transfering.
     *
     * @see setState
     */
    enum class ListenerState {
        BEFORE_UPDATE,
        AFTER_UPDATE
    }
}

/**
 * @param stateInstances all instances of your different state classes
 */
class InitStateController<TState : Enum<*>, TInterfaceWithState : WithState<TState>, TClassWithState>(
    scope: ModuleScope,
    parentStateClass: TClassWithState,
    vararg stateInstances: TInterfaceWithState
) : StateController<TState, TInterfaceWithState, TClassWithState>(scope, parentStateClass, stateInstances.first()) {

    private val states = listOf(*stateInstances)

    /**
     * Transfer state from current state to specfied state
     *
     * It will call all observers in parallel, all interceptors in serial during transfering,
     * if there is an intercetpor with [ListenerState.BEFORE_UPDATE] priority,
     * it could intercept and cancel this transfering.
     *
     * @see ListenerState
     * @see observeStateChange
     * @see interceptStateChange
     * @param afterState transfer to this state, enum type [TState]
     */
    suspend fun setState(afterState: TState): TInterfaceWithState =
        setState(states.first { it.state == afterState })
}

/**
 * Quick way of [StateController.observeStateChange]
 *
 * @param listenerState a [ListenerState] to indicate invocation priority
 * @param observer observation lambda block with [TClassWithState] context,
 * and without any input parameter
 * @see StateController.observeStateChange
 * @see StateController.setState
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <TState : Enum<*>, TInterfaceWithState : WithState<TState>, TClassWithState>
    StateController<TState, TInterfaceWithState, TClassWithState>.observe(
    listenerState: StateController.ListenerState = StateController.ListenerState.BEFORE_UPDATE,
    noinline observer: suspend TClassWithState.() -> Unit
) = observeStateChange(listenerState) { _, _ -> this.observer() }

/**
 * Quick way of [StateController.interceptStateChange]
 *
 * @param listenerState a [ListenerState] to indicate invocation priority
 * @param interceptor interception lambda block with [TClassWithState] context
 * and without any input parameter
 * @see StateController.interceptStateChange
 * @see StateController.setState
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <TState : Enum<*>, TInterfaceWithState : WithState<TState>, TClassWithState>
    StateController<TState, TInterfaceWithState, TClassWithState>.intercept(
    listenerState: StateController.ListenerState = StateController.ListenerState.BEFORE_UPDATE,
    noinline interceptor: suspend TClassWithState.() -> Boolean
) = interceptStateChange(listenerState) { _, _ -> this.interceptor() }

/**
 * Quick way of [StateController.interceptStateChange]
 *
 * Observe changes and call [block] in serial
 *
 * @param listenerState a [ListenerState] to indicate invocation priority
 * @param block lambda block with [TClassWithState] context
 * and without any input parameter, and final return
 * @see StateController.interceptStateChange
 * @see StateController.setState
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <TState : Enum<*>, TInterfaceWithState : WithState<TState>, TClassWithState>
    StateController<TState, TInterfaceWithState, TClassWithState>.block(
    listenerState: StateController.ListenerState = StateController.ListenerState.BEFORE_UPDATE,
    noinline block: suspend TClassWithState.() -> Unit
) = interceptStateChange(listenerState) { _, _ -> this.block(); false }
