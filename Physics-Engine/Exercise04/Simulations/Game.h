#ifndef GAME_h
#define GAME_h
#include "Simulator.h"
#include "RigidBodySystem.h" 
#include "collisionDetect.h"
#include "vectorbase.h"
#include "pcgsolver.h"
#include <list>

//#define TESTCASEUSEDTORUNTEST 2

class Grid {
public:
	// Construtors
	Grid(int x, int y);

	float get(int x, int y);

	void set(int x, int y, float v);

	int getWidth();

	int getLength();

private:
	// Attributes
	vector<Real> grid;
	int w;
	int l;
};


class Game :public Simulator {
public:
	// Construtors
	Game();

	// Functions
	const char * getTestCasesStr();
	void initUI(DrawingUtilitiesClass * DUC);
	void reset();
	void drawFrame(ID3D11DeviceContext* pd3dImmediateContext);
	void notifyCaseChanged(int testCase);
	void externalForcesCalculations(float timeElapsed);
	void simulateTimestep(float timeStep);
	void onClick(int x, int y);
	void onMouse(int x, int y);

	// ExtraFunctions
	int getNumberOfRigidBodies();
	Vec3 getPositionOfRigidBody(int i);
	Vec3 getLinearVelocityOfRigidBody(int i);
	Vec3 getAngularVelocityOfRigidBody(int i);
	void addRigidBodiesToList();
	int addRigidBody_stationary(Vec3 position, Vec3 size, int mass);
	void applyForceOnBody(int i, Vec3 loc, Vec3 force);
	int addRigidBody(Vec3 position, Vec3 size, int mass);
	void setOrientationOf(int i, Quat orientation);
	void setVelocityOf(int i, Vec3 velocity);
	void setColorOf(int i, Vec3 col);
	void drawStuff();
	void checkCollisions();
	void computeImpact(RigidBodySystem* x, RigidBodySystem* y, CollisionInfo info);
	void calculateColors();
	Grid* diffuseTemperatureExplicit(float timeStep);
	void diffuseTemperatureImplicit(float timeStep);
	void setGravity(float gravity);
	RigidBodySystem* getRigidBody(int index);
	
	

private:
	// Attributes
	RigidBodySystem * m_pRigidBodySystem;
	Vec3 m_externalForce;
	Vec3 m_forcePosition;
	int index_counter;
	int index_counter_stationary;
	Vec3 torque;
	float m_fGravity;

	// UI Attributes
	Point2D m_mouse;
	Point2D m_trackmouse;
	Point2D m_oldtrackmouse;

	//lists
	std::list<RigidBodySystem*> m_lRigidbodies;
	std::list<RigidBodySystem*> m_lRigidbodies_stationary;


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
	void setFixed();
	void shoot_func();

	Vec3  m_vfMovableObjectPos;
	Vec3  m_vfMovableObjectFinalPos;
	Vec3  m_vfRotate;
	Grid *T; //save results of every time step

	int T_width; //width of grid
	int T_length;//length of grid
	float spacing;//space between points
	float alpha; //alpha value
	int num_sources; //number of temperature sources

	bool shoot;
	int shoot_counter;
};
#endif
