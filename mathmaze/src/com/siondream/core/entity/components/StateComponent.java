package com.siondream.core.entity.components;

import com.badlogic.gdx.utils.ObjectMap;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class StateComponent extends Component implements Poolable {
	
	public static final int STATE_INVALID = 0;
	
	public int id = STATE_INVALID;
	
	private static ObjectMap<String, Integer> stateIDs = new ObjectMap<String, Integer>();
	private static ObjectMap<Integer, String> stateNames = new ObjectMap<Integer, String>();
	private static int nextState = 1;
	
	public static int getID(String name) {
		Integer stateID = stateIDs.get(name, null);
		
		if (stateID != null) {
			return stateID;
		}
		
		stateID = nextState;
		stateIDs.put(name, nextState++);
		stateNames.put(stateID, name);
		return stateID;
	}
	
	public static String getName(int id) {
		return stateNames.get(id, null);
	}
	
	@Override
	public void reset() {
		id = STATE_INVALID;
	}
}
