#include "MassSpringSystemSimulator.h"

MassSpringSystemSimulator::MassSpringSystemSimulator() {
	// Data Attributes (values given from exercise sheet 1)
	m_fMass = 10.0f;
	m_fStiffness = 40.0f;
	m_fDamping = 1.0f;
	m_iIntegrator = 0;
	m_iIDcounter = 0;
	m_fGravity = 0.5f;

	//lists
	std::list<massPoint> m_lMassPoints;
	std::list<spring> m_lSprings;

	// UI Attributes
	m_externalForce = Vec3();
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
}

//UI-functions
const char * MassSpringSystemSimulator::getTestCasesStr() {
	return "Demo 1,Demo 2,Demo 3,Demo 4";
}

void MassSpringSystemSimulator::reset() {
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
}

void MassSpringSystemSimulator::initUI(DrawingUtilitiesClass * DUC)
{
	this->DUC = DUC;
	switch (m_iTestCase)
	{
	case 0:break;
		//euler function here
	case 1:break;
		//leapfrog function here
	case 2:break;
		//midpoint function here
	case 3:
		TwAddVarRW(DUC->g_pTweakBar, "Integreator, Euler=0; Midpoint=1", TW_TYPE_INT32, &m_iIntegrator, "min=0");
		TwAddVarRW(DUC->g_pTweakBar, "Gravity", TW_TYPE_FLOAT, &m_fGravity, "min=0.00 step=0.01");
		TwAddVarRW(DUC->g_pTweakBar, "Stiffness", TW_TYPE_FLOAT, &m_fStiffness, "min=0.00 step=0.01");
		TwAddVarRW(DUC->g_pTweakBar, "Mass", TW_TYPE_FLOAT, &m_fMass, "min=0.00 step=0.01");
		break;
	default:break;
	}
}

void MassSpringSystemSimulator::clear() {
	m_iIDcounter = 0;
	m_lSprings.clear();
	m_lMassPoints.clear();
};

void MassSpringSystemSimulator::notifyCaseChanged(int testCase)
{
	m_iTestCase = testCase;
	clear();
	switch (m_iTestCase)
	{
	case 0: 
		{
			cout << "------- Demo 1! -------\n";
			//initializing Euler-test case with values from exercise sheet 1)
			Vec3 pos0 = { 0, 0, 0 };
			Vec3 vel0 = { -1, 0, 0 };
			Vec3 pos1 = { 0, 2, 0 };
			Vec3 vel1 = { 1, 0, 0 };

			//adding two masspoints
			int euler_index0 = addMassPoint(pos0, vel0, false);
			int euler_index1 = addMassPoint(pos1, vel1, false);

			//adding the spring
			addSpring(euler_index0, euler_index1, 1.0f);

			//simulate one Euler Step
			calculateEulerIntegration(0.1f);
			cout << "[Euler] new Position of masspoint " << getMassPoint(euler_index0)->index << " is: " << getMassPoint(euler_index0)->position << endl;
			cout << "[Euler] new Position of masspoint " << getMassPoint(euler_index1)->index << " is: " << getMassPoint(euler_index1)->position << endl;
			cout << "[Euler] new Velocity of masspoint " << getMassPoint(euler_index0)->index << " is: " << getMassPoint(euler_index0)->velocity << endl;
			cout << "[Euler] new Velocity of masspoint " << getMassPoint(euler_index1)->index << " is: " << getMassPoint(euler_index1)->velocity << endl;

			//adding two masspoints
			int midpoint_index0 = addMassPoint(pos0, vel0, false);
			int midpoint_index1 = addMassPoint(pos1, vel1, false);

			//adding the spring
			addSpring(midpoint_index0, midpoint_index1, 1.0f);

			//simulate one Midpoint Step
			calculateMidpointIntegration(0.1f);
			cout << "[Midpoint] new Position of masspoint " << getMassPoint(midpoint_index0)->index << " is: " << getMassPoint(midpoint_index0)->position << endl;
			cout << "[Midpoint] new Position of masspoint " << getMassPoint(midpoint_index1)->index << " is: " << getMassPoint(midpoint_index1)->position << endl;
			cout << "[Midpoint] new Velocity of masspoint " << getMassPoint(midpoint_index0)->index << " is: " << getMassPoint(midpoint_index0)->velocity << endl;
			cout << "[Midpoint] new Velocity of masspoint " << getMassPoint(midpoint_index1)->index << " is: " << getMassPoint(midpoint_index1)->velocity << endl;
		}
		break;
	case 1: {
		cout << "------- Demo 2! -------\n\n";
		m_iIntegrator = 0;
		Vec3 pos0 = { 0, 0, 0 };
		Vec3 vel0 = { -1, 0, 0 };
		Vec3 pos1 = { 0, 2, 0 };
		Vec3 vel1 = { 1, 0, 0 };
		int euler_index0 = addMassPoint(pos0, vel0, false);
		int euler_index1 = addMassPoint(pos1, vel1, false);
		addSpring(euler_index0, euler_index1, 1.0f);
		}
		break;
	case 2: {
		cout << "------- Demo 3! -------\n\n";
		m_iIntegrator = 2;
		Vec3 pos0 = { 0, 0, 0 };
		Vec3 vel0 = { -1, 0, 0 };
		Vec3 pos1 = { 0, 2, 0 };
		Vec3 vel1 = { 1, 0, 0 };
		int midPoint_index0 = addMassPoint(pos0, vel0, false);
		int midPoint_index1 = addMassPoint(pos1, vel1, false);
		addSpring(midPoint_index0, midPoint_index1, 1.0f);
		}
		break;
	case 3: {
		cout << "------- Demo 4! -------\n\n";
		Vec3 pos0 = { -0.5, 0, 0 };
		Vec3 vel0 = { 0, 0, 0 };

		Vec3 pos1 = { -0.25, -0.5, 0 };
		Vec3 vel1 = { 0, 0, 0 };

		Vec3 pos2 = { -0.25, 0, 0 };
		Vec3 vel2 = { 0, 0, 0 };

		Vec3 pos3 = { -0.25, 0.5, 0 };
		Vec3 vel3 = { 0, 0, 0 };

		Vec3 pos4 = { 0, 0.5, 0 };
		Vec3 vel4 = { 0, 0, 0 };

		Vec3 pos5 = { 0, 0, 0 };
		Vec3 vel5 = { 0, 0, 0 };

		Vec3 pos6 = { 0.25, 0, 0 };
		Vec3 vel6 = { 0, 0, 0 };

		Vec3 pos7 = { 0.25, 0.5, 0 };
		Vec3 vel7 = { 0, 0, 0 };

		Vec3 pos8 = { 0.5, 0, 0 };
		Vec3 vel8 = { 0, 0, 0 };

		Vec3 pos9 = { 0.25, -0.5, 0 };
		Vec3 vel9 = { 0, 0, 0 };

		int index0 = addMassPoint(pos0, vel0, false);
		int index1 = addMassPoint(pos1, vel1, false);
		int index2 = addMassPoint(pos2, vel2, false);
		int index3 = addMassPoint(pos3, vel3, false);
		int index4 = addMassPoint(pos4, vel4, false);
		int index5 = addMassPoint(pos5, vel5, false);
		int index6 = addMassPoint(pos6, vel6, false);
		int index7 = addMassPoint(pos7, vel7, false);
		int index8 = addMassPoint(pos8, vel8, false);
		int index9 = addMassPoint(pos9, vel9, false);

		addSpring(index0, index1, 0.56f);
		addSpring(index0, index2, 0.25f);
		addSpring(index0, index3, 0.56f);
		addSpring(index1, index2, 0.5f);
		addSpring(index1, index5, 0.56f);
		addSpring(index2, index3, 0.5f);
		addSpring(index2, index5, 0.25f);
		addSpring(index3, index4, 0.25f);
		addSpring(index3, index5, 0.56f);
		addSpring(index4, index7, 0.25f);
		addSpring(index5, index7, 0.56f);
		addSpring(index5, index6, 0.25f);
		addSpring(index5, index9, 0.56f);
		addSpring(index6, index7, 0.5f);
		addSpring(index6, index8, 0.25f);
		addSpring(index6, index9, 0.5f);
		addSpring(index7, index8, 0.56f);
		addSpring(index8, index9, 0.56f);
		}
		break;
	default:
		cout << "Empty Test!\n";
		break;
	}
}

void MassSpringSystemSimulator::externalForcesCalculations(float timeElapsed)
{
	// Apply the mouse deltas to g_vfMovableObjectPos (move along cameras view plane)
	Point2D mouseDiff;
	mouseDiff.x = m_trackmouse.x - m_oldtrackmouse.x;
	mouseDiff.y = m_trackmouse.y - m_oldtrackmouse.y;
	if (mouseDiff.x != 0 || mouseDiff.y != 0)
	{
		Mat4 worldViewInv = Mat4(DUC->g_camera.GetWorldMatrix() * DUC->g_camera.GetViewMatrix());
		worldViewInv = worldViewInv.inverse();
		Vec3 inputView = Vec3((float)mouseDiff.x, (float)-mouseDiff.y, 0);
		Vec3 inputWorld = worldViewInv.transformVectorNormal(inputView);
		// find a proper scale!
		float inputScale = 0.001f;
		inputWorld = inputWorld * inputScale;
	}
	else {
		//m_vfMovableObjectFinalPos = m_vfMovableObjectPos;
	}
}

void MassSpringSystemSimulator::simulateTimestep(float timeStep)
{
	// update current setup for each frame
	switch (m_iIntegrator)
	{// handling different cases
	case 0:
		//Euler stuff
		calculateEulerIntegration(timeStep);
		break;
	case 1:
		//midpoint stuff
		calculateMidpointIntegration(timeStep);
		break;
	case 2:
		//leapfrog stuff
		calculateMidpointIntegration(timeStep);
		break;
	default:
		break;
	}
}

void MassSpringSystemSimulator::drawStuff() {
	DUC->setUpLighting(Vec3(), Vec3(0.62, 0.62, 0.62), 0.5f, Vec3(0.62, 0.62, 0.62));
	for (massPoint* x : m_lMassPoints) {
		DUC->drawSphere(x->position, Vec3(0.05, 0.05, 0.05));
	}
	for (spring* x : m_lSprings) {
		DUC->beginLine();
		DUC->drawLine(getPositionOfMassPoint(x->masspoint1), Vec3(0.54, 0.1, 0.1), getPositionOfMassPoint(x->masspoint2), Vec3(0.54, 0.1, 0.1));
		DUC->endLine();
	}
}

void MassSpringSystemSimulator::drawFrame(ID3D11DeviceContext* pd3dImmediateContext)
{
	switch (m_iTestCase)
	{// handling different cases
	case 0:
		break;
	case 1:
		drawStuff();
		break;
	case 2:
		drawStuff();
	case 3:
		setGravity(m_fGravity);
		drawStuff();
		break;
	default:
		break;
	}
}

void MassSpringSystemSimulator::onClick(int x, int y)
{
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}

void MassSpringSystemSimulator::onMouse(int x, int y)
{
	m_oldtrackmouse.x = x;
	m_oldtrackmouse.y = y;
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}

//specific functions
void MassSpringSystemSimulator::setMass(float mass) {
	m_fMass = mass;
}

void MassSpringSystemSimulator::setStiffness(float stiffness) {
	m_fStiffness = stiffness;
}

void MassSpringSystemSimulator::setDampingFactor(float damping) {
	m_fDamping = damping;
}

int MassSpringSystemSimulator::addMassPoint(Vec3 position, Vec3 velocity, bool isFixed) {
	//create temporary mass point
	massPoint* tmp = new massPoint;
	tmp->index = m_iIDcounter;
	tmp->position = position;
	tmp->velocity = velocity;
	tmp->isFixed = isFixed;
	tmp->Force = 0;

	//increment idCounter
	m_iIDcounter++;

	//add mass point to list
	m_lMassPoints.push_back(tmp);

	//retunr index
	return tmp->index;
}

void MassSpringSystemSimulator::addSpring(int masspoint1, int masspoint2, float initialLength) {
	//create temporary spring
	spring* tmp = new spring;
	tmp->masspoint1 = masspoint1;
	tmp->masspoint2 = masspoint2;
	tmp->initialLength = initialLength;

	//add spring to list 
	m_lSprings.push_back(tmp);
}

int MassSpringSystemSimulator::getNumberOfMassPoints() {
	return m_lMassPoints.size();
}

int MassSpringSystemSimulator::getNumberOfSprings() {
	return m_lSprings.size();
}

Vec3 MassSpringSystemSimulator::getPositionOfMassPoint(int index) {
	//looping over list, searching specific masspoint
	for (massPoint* x : m_lMassPoints) {
		if (x->index == index) {
			//returning its position
			return x->position;
		}
	}

	//error if no masspoint found
	cout << "[ERROR] index not found\n";
}

Vec3 MassSpringSystemSimulator::getVelocityOfMassPoint(int index) {
	//looping over list, searching specific masspoint
	for (massPoint* x : m_lMassPoints) {
		if (x->index == index) {
			//returning its velocity
			return x->velocity;
		}
	}

	//error if no masspoint found
	cout << "[ERROR] index not found\n";
}

MassSpringSystemSimulator::massPoint* MassSpringSystemSimulator::getMassPoint(int index) {
	//looping over list, searching specific masspoint
	for (massPoint* x : m_lMassPoints) {
		if (x->index == index) {
			//returning the masspoint
			return x;
		}
	}

	//error if no masspoint found
	cout << "[ERROR] index not found\n";
}

void MassSpringSystemSimulator::applyExternalForce(Vec3 force) {
	for (massPoint* x : m_lMassPoints) {
		x->Force = x->Force + force;
	}
}

float MassSpringSystemSimulator::getInitialLength(int masspoint1, int masspoint2) {
	//looping over list, searching specific spring
	for (spring* x : m_lSprings) {
		if (x->masspoint1 == masspoint1 && x->masspoint2 == masspoint2) {
			//returning its inital length
			return x->initialLength;
		}
	}

	//error if no masspoint found
	cout << "[ERROR] spring not found\n";
}


//exercise calculations
void MassSpringSystemSimulator::calculateEulerIntegration(float timeStep) {
	for (spring* x : m_lSprings) {
		//Calculates elastic Force for Masspoint1
		Vec3 elasticForce = computeElasticForces(x);
		massPoint* m1 = getMassPoint(x->masspoint1);
		massPoint* m2 = getMassPoint(x->masspoint2);
		//add calculated Force to massPoint1
		m1->Force = m1->Force + elasticForce;
		//add negetaed Force to massPoint2
		m2->Force = m2->Force - elasticForce;
	}
	applyExternalForce(m_externalForce);
	for (massPoint* x : m_lMassPoints) {
		//calculate new Position based on old Velocity
		x->position = x->position + timeStep * x->velocity;
		if (x->position.y < -1) {
			x->position.y = -1;
		}
		//calculate new Velocity
		x->velocity = x->velocity + timeStep * (x->Force / m_fMass);
		x->Force = 0;
	}
}

void MassSpringSystemSimulator::calculateMidpointIntegration(float timeStep) {
	//create list for original position
	std::list<Vec3> og_pos;

	//create list for original velocity
	std::list<Vec3> og_vel;
	
	//calculate forces
	for (spring* x : m_lSprings) {
		//Calculating elastic Force for Masspoint1
		Vec3 elasticForce = computeElasticForces(x);
		massPoint* m1 = getMassPoint(x->masspoint1);
		massPoint* m2 = getMassPoint(x->masspoint2);

		//add calculated Force to massPoint1
		m1->Force = m1->Force + elasticForce;

		//add negetaed Force to massPoint2
		m2->Force = m2->Force - elasticForce;
	}
	

	//calcultaing values for midstep
	for (massPoint* x : m_lMassPoints) {
		//saving og position and velocity
		og_pos.push_back(x->position);
		og_vel.push_back(x->velocity);

		//doing first half a time step and calculating values
		x->position = x->position + timeStep/2 * x->velocity;
		x->velocity = x->velocity + timeStep/2 * (x->Force / m_fMass);
		
		//clear force
		x->Force = 0;
	}

	//calculate forces
	for (spring* x : m_lSprings) {
		//Calculates elastic Force for Masspoint1
		Vec3 elasticForce = computeElasticForces(x);
		massPoint* m1 = getMassPoint(x->masspoint1);
		massPoint* m2 = getMassPoint(x->masspoint2);

		//add calculated Force to massPoint1
		m1->Force = m1->Force + elasticForce;

		//addnegetaed Force to massPoint2
		m2->Force = m2->Force - elasticForce;
	}
	applyExternalForce(m_externalForce);
	//calcultaing values for endstep
	for (massPoint* x : m_lMassPoints) {
		//doing second half of the time step and calculating values
		x->position = og_pos.front() + timeStep * x->velocity;
		og_pos.pop_front();
		if (x->position.y < -1) {
			x->position.y = -1;
		}
		x->velocity = og_vel.front() + timeStep * (x->Force / m_fMass);
		og_vel.pop_front();

		//clear force
		x->Force = 0;
	}
}

Vec3 MassSpringSystemSimulator::computeElasticForces(spring* inputSpring) {
	//getting the possition of the masspoints
	Vec3 m1Pos = getPositionOfMassPoint(inputSpring->masspoint1);
	Vec3 m2Pos = getPositionOfMassPoint(inputSpring->masspoint2);
	Vec3 forceVec = m1Pos - m2Pos;

	//getting the current length of the spring
	float currentLength = norm(forceVec);

	//using the hookean spring formula
	Vec3 force = -m_fStiffness * (currentLength-inputSpring->initialLength)*getNormalized(forceVec);
	return force;
}

void MassSpringSystemSimulator::setGravity(float gravity) {
	m_externalForce = Vec3(0, -gravity, 0);
};