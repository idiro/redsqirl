package com.redsqirl.workflow.server.enumeration;

/**
 * Path Type, describes what kind of operations are available for a path:
 * - REAL: a real path describes a string on which a dataset can be saved
 * - TEMPLATE: a path that includes variables that has to change at running time
 * - MATERIALIZED: a path with variables, for which every variables needed for using it as input is set.
 * 
 * @author etienne
 *
 */
public enum PathType {
	/**
	 * Real Path
	 */
	REAL,
	/**
	 * Template Path
	 */
	TEMPLATE,
	/**
	 * Materialized Path (Template path with variables set)
	 */
	MATERIALIZED
}
