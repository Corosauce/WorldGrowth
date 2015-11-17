package vectortree.simulation.tree;

/**
 * This can define the trunk, or branches, etc
 * 
 * what defines a node?
 * 
 * profiled and runtime values of:
 * - thickness
 * - length
 * - ideal direction
 * - 
 * 
 * just profiled:
 * - growth rate
 * 
 * random ideas:
 * - perhaps an algorithm can help define how it all functions
 * - eg: how many child branches can grow depending on what level deep we are in the tree node hierarchy
 * 
 * 
 * adjusting from old code to new:
 * - adjust branch growth patterns to allow for random variance
 * -- only do a new node if its a legit child branch
 * -- otherwise keep growing with random variance but maintaining an overall direction
 * 
 * - spreading via fruit dropping with seeds and growing new tree
 * -- simulate dont make actual items
 * 
 * ideas about profiling:
 * ======================
 * - define on a per branch level basis, since most trees dont go beyond 3-4 levels
 * 
 * branch options:
 * ---------------
 * 
 * - up/down ratio where 1 is even, 2 is up, 0 is down
 * - inherit direction from previous branch or fully use above option
 * -- amount to inherit, sliding value? 0-1?
 * - do leaf producing module
 * 
 * - 
 * 
 * @author Corosus
 *
 */
public class GrowthNodeNew {

}
