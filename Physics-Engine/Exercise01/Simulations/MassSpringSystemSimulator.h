#ifndef MASSSPRINGSYSTEMSIMULATOR_h
#define MASSSPRINGSYSTEMSIMULATOR_h
#include "Simulator.h"
#include <list>
#include <cmath>

// Do Not Change
#define EULER 0
#define LEAPFROG 1
#define MIDPOINT 2
// Do Not Change


class MassSpringSystemSimulator:public Simulator{
public:
	// Construtors
	MassSpringSystemSimulator();
	
	// UI Functions
	const char * getTestCasesStr();
	void initUI(DrawingUtilitiesClass * DUC);
	void reset();
	void drawFrame(ID3D11DeviceContext* pd3dImmediateContext);
	void notifyCaseChanged(int testCase);
	void externalForcesCalculations(float timeElapsed);
	void simulateTimestep(float timeStep);
	void onClick(int x, int y);
	void onMouse(int x, int y);

	// Specific Functions
	void setMass(float mass);
	void setStiffness(float stiffness);
	void setDampingFactor(float damping);
	int addMassPoint(Vec3 position, Vec3 velocity, bool isFixed);
	void addSpring(int masspoint1, int masspoint2, float initialLength);
	int getNumberOfMassPoints();
	int getNumberOfSprings();
	Vec3 getPositionOfMassPoint(int index);
	Vec3 getVelocityOfMassPoint(int index);
	void applyExternalForce(Vec3 force);
	float getInitialLength(int masspoint1, int masspoint2);

	// Do Not Change
	void setIntegrator(int integrator) {
		m_iIntegrator = integrator;
	}

private:
	// Data Attributes
	float m_fMass;
	float m_fStiffness;
	float m_fDamping;
	float m_fGravity;
	int m_iIntegrator;
	int m_iIDcounter;

	// structs
	struct massPoint
	{
		int index;
		Vec3 position;
		Vec3 velocity;
		Vec3 Force;
		bool isFixed;
	};
	struct spring
	{
		int masspoint1;
		int masspoint2;
		float initialLength;
	};

	//lists
	std::list<massPoint*> m_lMassPoints;
	std::list<spring*> m_lSprings;

	// UI Attributes
	Vec3 m_externalForce;
	Point2D m_mouse;
	Point2D m_trackmouse;
	Point2D m_oldtrackmouse;

	//exercise calculations
	void calculateEulerIntegration(float timeStep);
	void calculateMidpointIntegration(float timeStep);
	Vec3 computeElasticForces(spring* spring);
	massPoint* getMassPoint(int index);
	void clear();
	void setGravity(float gravity);
	void drawStuff();
};
#endif