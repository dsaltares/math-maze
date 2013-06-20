package com.siondream.core;

public class Chrono {
	private long time;
	private long initial;
	private boolean running;
	
	public Chrono() {
		reset();
	}
	
	public void reset() {
		time = 0;
		initial = System.currentTimeMillis();
		running = true;
	}
	
	public void start() {
		if (running) return;
		setInitial();
		running = true;
	}
	
	public void pause() {
		if (!running) return;
		time += getCount();
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public float getSeconds() {
		if (running) {
			time += getCount();
			setInitial();
		}
		
		return time / 1000.0f;
	}
	
	public String getTime() {
		float totalSeconds = getSeconds();
		int minutes = ((int)totalSeconds / 60) % 100;
		int seconds = (int)totalSeconds % 60;
		
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	private void setInitial() {
		initial = System.currentTimeMillis();
	}
	
	private long getCount() {
		return System.currentTimeMillis() - initial;
	}
}
