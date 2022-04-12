#ifndef RIGIDBODYSYSTEMSIMULATOR_h
#define RIGIDBODYSYSTEMSIMULATOR_h
#include "Simulator.h"
#include "RigidBodySystem.h" 
#include "collisionDetect.h"
#include <list>

#define TESTCASEUSEDTORUNTEST 2

class RigidBodySystemSimulator:public Simulator{
public:
	// Construtors
	RigidBodySystemSimulator();
	
	// Functions
	const char * getTestCasesStr();
	void initUI(DrawingUtilitiesClass * DUC);
	void reset();
	void drawFrame(ID3D11DeviceContext* pd3dImmediateContext);
	void notifyCaseChanged(int testCase);
	void externalForcesCalculations(float timeElapsed);
	void arrowKeys(int index);
	void simulateTimestep(float timeStep);
	void onClick(int x, int y);
	void onMouse(int x, int y);

	// ExtraFunctions
	int getNumberOfRigidBodies();
	Vec3 getPositionOfRigidBody(int i);
	Vec3 getLinearVelocityOfRigidBody(int i);
	Vec3 getAngularVelocityOfRigidBody(int i);
	void applyForceOnBody(int i, Vec3 loc, Vec3 force);
	void addRigidBody(Vec3 position, Vec3 size, int mass);
	void setOrientationOf(int i,Quat orientation);
	void setVelocityOf(int i, Vec3 velocity);
	void clear();
	void drawStuff();
	void checkCollisions();
	void computeImpact(RigidBodySystem* x, RigidBodySystem* y, CollisionInfo info);

	void setGravity(float gravity);

private:
	// Attributes
	RigidBodySystem * m_pRigidBodySystem; 
	Vec3 m_externalForce;
	Vec3 m_forcePosition;
	int index_counter;
	Vec3 torque;
	float m_fGravity;

	// UI Attributes
	Point2D m_mouse;
	Point2D m_trackmouse;
	Point2D m_oldtrackmouse;

	//lists
	std::list<RigidBodySystem*> m_lRigidbodies;


	void integrateRotation(int i, float timeStep);
	void integrateAngularMomentum(int i, float timeStep);
	void computeTorque(int i);
	void computeForce(int i);
	void precomputeInertiaTensor(int i);
	void updateInertiaTensor(int i);
	void updateAngularVelocity(int i);
	void updateWorldSpace(int i);
	void clearForces(int i);
	void calculateEulerIntegration(int i, float timeStep);
	void applyExternalForce(Vec3 force);
	};
#endif