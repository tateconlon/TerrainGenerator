package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
	
	private Vector3f position;	
	private float pitch;
	private float yaw;
	private float roll;
	
	public Camera() {
		position = new Vector3f(0, 0, 0);
		pitch = 0f;
		yaw = 0f;
		roll = 0f;
	}

	public Camera(Vector3f position, float pitch, float yaw, float roll) {
		this.position = position;
		this.pitch = pitch;
		this.yaw = yaw;
		this.roll = roll;
	}
	
	public void move() {
		int dRoll = Mouse.getDWheel();
		pitch += ((float)dRoll) / 20f;
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT))
		{
			yaw -= 0.5f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
		{
			yaw += 0.5f;
		}
		
		//yaw = 0;
		//System.out.println(yaw);
		Vector3f forward = new Vector3f((float)Math.sin(Math.toRadians(yaw)), (float)Math.sin(Math.toRadians(pitch)), (float)Math.cos(Math.toRadians(yaw))*(float)Math.cos(Math.toRadians(pitch)));
		forward.normalise();
		Vector3f right = Vector3f.cross(new Vector3f(0, 1, 0), forward, null);
		right.normalise();
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			position.translate(forward.x, -forward.y, -forward.z);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			position.translate(-right.x, right.y, right.z);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			position.translate(-forward.x, forward.y, forward.z);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			position.translate(right.x, -right.y, -right.z);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_UP))
		{
			position.y += 1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN))
		{
			position.y -= 1f;
		}
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	


}
