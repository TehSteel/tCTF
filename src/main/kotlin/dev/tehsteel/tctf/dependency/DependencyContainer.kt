package dev.tehsteel.tctf.dependency

import kotlin.properties.ReadOnlyProperty

/** DependencyContainer holds all dependencies **/
object DependencyContainer {
	/** Map that holds all the dependencies **/
	val dependencies: MutableMap<Any, Any> = mutableMapOf()

	/**
	 * Register managers with it
	 */
	inline fun <reified T : Any> register(dependency: T) {
		dependencies[T::class] = dependency
	}

	/**
	 * Grab the instance
	 */
	inline fun <reified T : Any> getInstance(): ReadOnlyProperty<Any, T> {
		return ReadOnlyProperty { _, _ ->
			dependencies[T::class] as T?
				?: throw IllegalStateException("Dependency not found for ${T::class.simpleName}")
		}
	}


}
