#include "Simulator.h"
#include <list>

class RigidBodySystem{
public:
	//Constructor
	RigidBodySystem(int index, Vec3 center, Vec3 size, float mass);

	int index;
	Vec3 m_center;
	Vec3 m_size;
	float f_mass;

	// Variables for Rotation
	Quat rotation;
	Vec3 angularVelocity;
	Vec3 angularMomentum;
	Vec3 torque;
	Mat4 initialInertiaTensor;
	Mat4 currentInertiaTensor;

	//Variabels for Movement
	Vec3 linearVelocity;
	Vec3 Force;

	//variables for game
	boolean fixed;
	Vec3 color;

	void calculateLocalPosition();
	void calculateGlobalPosition();
	void calculateInitialInertiaTensor();
	void updateInertiaTensor();

	void applyForce(Vec3 loc, Vec3 force);
	void computeForce();
	void computeTorque();

	void clearForces();
	
private:
	int indexCounter;
	//structs
	struct Point
	{
		int index;
		Vec3 localPosition;
		Vec3 force;
		Vec3 velocity;
		Vec3 globalPosition;
	};
	//list
	std::list<Point*> m_lpoints;
};
