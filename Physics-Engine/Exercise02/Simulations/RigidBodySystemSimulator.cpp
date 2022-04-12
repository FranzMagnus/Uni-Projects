#include "RigidBodySystemSimulator.h"

RigidBodySystemSimulator::RigidBodySystemSimulator()
{
	// UI Attributes
	m_externalForce = Vec3();
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
	index_counter = 0;
	m_fGravity = 0.0f;
}

//UI functions
const char * RigidBodySystemSimulator::getTestCasesStr()
{
	return "Demo 1,Demo 2,Demo 3,Demo 4";
}

void RigidBodySystemSimulator::reset()
{
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
}

void RigidBodySystemSimulator::initUI(DrawingUtilitiesClass * DUC)
{
	this->DUC = DUC;
	switch (m_iTestCase)
	{
	case 0:
		//nothing needed, just console prints
		break;
	case 1:
		TwAddVarRW(DUC->g_pTweakBar, "Gravity", TW_TYPE_FLOAT, &m_fGravity, "min=-10.00 max=10.0 step=0.01");
		break;
	case 2:
		break;
	case 3:
		TwAddVarRW(DUC->g_pTweakBar, "Gravity", TW_TYPE_FLOAT, &m_fGravity, "min=-10.00 max=10.0 step=0.01");
		break;
	default:
		break;
	}
}

void RigidBodySystemSimulator::clear() {
	//resetting class by clearing list of rigidbodies
	index_counter = 0;
	m_fGravity = 0.0f;
	for (RigidBodySystem* x : m_lRigidbodies) {
		free(x);
	}
	m_lRigidbodies.clear();
}

void RigidBodySystemSimulator::notifyCaseChanged(int testCase)
{

	m_iTestCase = testCase;
	clear();
	switch (m_iTestCase)
	{
	case 0:
	{
		cout << "------- Demo 1! -------\n";

		//adding values for demo01
		Vec3 center = { 0, 0, 0 };
		Vec3 size = { 1, 0.6f, 0.5f };
		int mass = 2;
		Mat4 orientation = Mat4();
		orientation.initRotationXYZ(0, 0, 90);
		Vec3 externalForce = { 1, 1, 0 };
		Vec3 forcePosition = { 0.3f, 0.5f, 0.25f };


		//adding rigidbody
		addRigidBody(center, size, mass);
		setOrientationOf(0, Quat(orientation));
		applyForceOnBody(0, forcePosition, externalForce);

		//just calling the timestep function instead of calling all functions manually
		simulateTimestep(2);

		//calculating world space velocity of given point
		Vec3 worldVel = getLinearVelocityOfRigidBody(0) + cross(getAngularVelocityOfRigidBody(0), { -0.3, -0.5, -0.25 });

		//printing calculated values after one timestep
		cout << "[DEMO 1] linear velocity of the rigidbody is: " << getLinearVelocityOfRigidBody(0) << endl;
		cout << "[DEMO 1] angular velocity of the rigidbody is: " << getAngularVelocityOfRigidBody(0) << endl;
		cout << "[DEMO 1] world space velocity of (-0.3, -0.5, -0.25) is: " << worldVel << endl;
	}
	break;
	case 1:
	{
		cout << "------- Demo 2! -------\n";

		/*not sure if the timestep has to be set manually to 0.01f,
		since in main.cpp it is 0.001f.
		But then there has to be a loop in here for simulating the timesteps,
		which should be in main.cpp*/

		//adding values
		Vec3 center = { 0, 0, 0 };
		Vec3 size = { 1, 0.6f, 0.5f };
		int mass = 2;
		Mat4 orientation = Mat4();
		orientation.initRotationXYZ(0, 0, 90);
		Vec3 externalForce = { 1, 1, 0 };
		Vec3 forcePosition = { 0.3f, 0.5f, 0.25f };
		//adding single rigidbody for simulation
		addRigidBody(center, size, mass);
		applyForceOnBody(0, forcePosition, externalForce);
		setOrientationOf(0, Quat(orientation));
	}
	break;
	case 2:
	{
		cout << "------- Demo 3! -------\n";
		//first ridigbody: not rotated, bottom, moving upwards
		Vec3 center = { 0, -0.5f, 0 };
		Vec3 size = { 0.1, 0.1, 0.1 };
		int mass = 1;
		Vec3 velocity = { 0, 1, 0 };

		//adding first rigidbody
		addRigidBody(center, size, mass);
		setVelocityOf(0, velocity);

		//second rigidbody: rotated, up in the air, moving downwards
		center = { 0, 0.5f, 0 };
		velocity = { 0, -1, 0 };
		Mat4 orientation = Mat4();
		orientation.initRotationXYZ(45, 0, 45);

		//adding second rigidbody
		addRigidBody(center, size, mass);
		setOrientationOf(1, Quat(orientation));
		setVelocityOf(1, velocity);
	}
	break;
	case 3:
	{
		cout << "------- Demo 4! -------\n";
		Vec3 center = { 0.5, -0.5f, 0 };
		Vec3 size = { 0.15, 0.15, 0.15 };
		float mass = 1;
		Vec3 velocity = { -0.5f, 0.5f, 0 };
		Mat4 orientation = Mat4();
		orientation.initRotationXYZ(45, 0, 45);
		addRigidBody(center, size, mass);
		setVelocityOf(0, velocity);
		setOrientationOf(0, Quat(orientation));

		center = { -0.5f, -0.5f, 0 };
		size = { 0.15, 0.15, 0.15 };
		mass = 1;
		velocity = { 0.5f, 0.5f, 0 };
		orientation = Mat4();
		orientation.initRotationXYZ(45, 0, 45);
		addRigidBody(center, size, mass);
		setVelocityOf(1, velocity);
		setOrientationOf(1, Quat(orientation));

		center = { 0.5f, 0.5f, 0 };
		size = { 0.15, 0.15, 0.15 };
		mass = 1;
		velocity = { -0.5f, -0.5f, 0 };
		orientation = Mat4();
		orientation.initRotationXYZ(45, 0, 45);
		addRigidBody(center, size, mass);
		setVelocityOf(2, velocity);
		setOrientationOf(2, Quat(orientation));

		center = { -0.5f, 0.5f, 0 };
		size = { 0.15, 0.15, 0.15 };
		mass = 1;
		velocity = { 0.5f, -0.5f, 0 };
		orientation = Mat4();
		orientation.initRotationXYZ(45, 0, 45);
		addRigidBody(center, size, mass);
		setVelocityOf(3, velocity);
		setOrientationOf(3, Quat(orientation));
	}
	break;
	default:
		break;
	}
}


void RigidBodySystemSimulator::externalForcesCalculations(float timeElapsed)
{
	if (DXUTIsKeyDown(0x31)) {
		arrowKeys(0);
	}
	else if (DXUTIsKeyDown(0x32)) {
		arrowKeys(1);
	}
	else if (DXUTIsKeyDown(0x33)) {
		arrowKeys(2);
	}
	else if (DXUTIsKeyDown(0x34)) {
		arrowKeys(3);
	}

}

void RigidBodySystemSimulator::arrowKeys(int index) {
	if (DXUTIsKeyDown(VK_LEFT)) {
		for (RigidBodySystem* x : m_lRigidbodies) {
			if (x->index == index) {
				setVelocityOf(x->index, x->linearVelocity + Vec3(-0.001, 0, 0));
			}
		}
	}
	else if (DXUTIsKeyDown(VK_RIGHT)) {
		for (RigidBodySystem* x : m_lRigidbodies) {
			if (x->index == index) {
				setVelocityOf(x->index, x->linearVelocity + Vec3(0.001, 0, 0));
			}
		}
	}
	else if (DXUTIsKeyDown(VK_UP)) {
		for (RigidBodySystem* x : m_lRigidbodies) {
			if (x->index == index) {
				setVelocityOf(x->index, x->linearVelocity + Vec3(0, 0.001, 0));
			}
		}
	}
	else if (DXUTIsKeyDown(VK_DOWN)) {
		for (RigidBodySystem* x : m_lRigidbodies) {
			if (x->index == index) {
				setVelocityOf(x->index, x->linearVelocity + Vec3(0, -0.001, 0));
			}
		}
	}
}

void RigidBodySystemSimulator::simulateTimestep(float timeStep)
{
	setGravity(m_fGravity);
	for (RigidBodySystem* x : m_lRigidbodies) {
		computeForce(x->index);
		applyExternalForce(m_externalForce);
		externalForcesCalculations(timeStep);
		computeTorque(x->index);
		calculateEulerIntegration(x->index, timeStep);
		integrateRotation(x->index, timeStep);
		integrateAngularMomentum(x->index, timeStep);
		updateInertiaTensor(x->index);
		updateAngularVelocity(x->index);
		updateWorldSpace(x->index);
		clearForces(x->index);
	}
	checkCollisions();
}

void RigidBodySystemSimulator::drawStuff() {
	DUC->setUpLighting(Vec3(), Vec3(0.62, 0.62, 0.62), 0.5f, Vec3(0.62, 0.62, 0.62));
	Mat4 scaling = Mat4();
	Mat4 translation = Mat4();
	Mat4 obj2world = Mat4();

	//iterating over all ridigbodies in list
	for (RigidBodySystem* x : m_lRigidbodies) {
		//getting scaling matrix
		scaling.initScaling(x->m_size.x, x->m_size.y, x->m_size.z);

		//getting translation matrix
		translation.initTranslation(x->m_center.x, x->m_center.y, x->m_center.z);

		//calculating obj2world matrix
		obj2world = scaling * x->rotation.getRotMat() * translation;

		//draw rigidbody
		DUC->drawRigidBody(obj2world);
	}
}

void RigidBodySystemSimulator::drawFrame(ID3D11DeviceContext * pd3dImmediateContext)
{
	switch (m_iTestCase)
	{
	case 0:
		//nothing to draw, only console prints
		break;
	case 1:
		drawStuff();
		break;
	case 2:
		drawStuff();
		break;
	case 3:
		drawStuff();
		break;
	default:
		break;
	}
}

void RigidBodySystemSimulator::onClick(int x, int y)
{
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}

void RigidBodySystemSimulator::onMouse(int x, int y)
{
	m_oldtrackmouse.x = x;
	m_oldtrackmouse.y = y;
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}


//specific functions
int RigidBodySystemSimulator::getNumberOfRigidBodies()
{
	return m_lRigidbodies.size();
}

Vec3 RigidBodySystemSimulator::getPositionOfRigidBody(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			return x->m_center;
		}
	}
}

Vec3 RigidBodySystemSimulator::getLinearVelocityOfRigidBody(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			return x->linearVelocity;
		}
	}
}

Vec3 RigidBodySystemSimulator::getAngularVelocityOfRigidBody(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			return x->angularVelocity;
		}
	}
}

void RigidBodySystemSimulator::applyForceOnBody(int i, Vec3 loc, Vec3 force)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		x->applyForce(loc, force);
	}
}

void RigidBodySystemSimulator::addRigidBody(Vec3 position, Vec3 size, int mass)
{
	RigidBodySystem* tmp = new RigidBodySystem(index_counter, position, size, mass);
	tmp->calculateGlobalPosition();
	tmp->calculateInitialInertiaTensor();
	index_counter++;
	m_lRigidbodies.push_back(tmp);
}

void RigidBodySystemSimulator::setOrientationOf(int i, Quat orientation)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->rotation = orientation;
		}
	}
}

void RigidBodySystemSimulator::setVelocityOf(int i, Vec3 velocity)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->linearVelocity = velocity;
		}
	}
}

//precompute inertia Tensor
void RigidBodySystemSimulator::precomputeInertiaTensor(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->calculateInitialInertiaTensor();
		}
	}
}

// Functions for step 1-6 from the tutorial
// Also take a look at lecture Slide 14

void RigidBodySystemSimulator::computeForce(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->computeForce();
		}
	}
}

// Step 1
void RigidBodySystemSimulator::computeTorque(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->computeTorque();
		}
	}
}

//Step2
void RigidBodySystemSimulator::integrateRotation(int i, float timeStep) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			Quat newRotation = x->rotation + (timeStep / 2)*Quat(0, x->angularVelocity.x, x->angularVelocity.y, x->angularVelocity.z)*x->rotation;
			if (newRotation.norm() == 0) {
				x->rotation = newRotation;
			}
			else {
				x->rotation = newRotation.unit();
			}
		}
	}
}
//Step3
void RigidBodySystemSimulator::integrateAngularMomentum(int i, float timeStep) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			Vec3 newAngularMomentum = x->angularMomentum + (timeStep)*x->torque;
			x->angularMomentum = newAngularMomentum;
		}
	}
}
//Step4
void RigidBodySystemSimulator::updateInertiaTensor(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->updateInertiaTensor();
		}
	}
}
//Step5
void RigidBodySystemSimulator::updateAngularVelocity(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->angularVelocity = x->currentInertiaTensor.transformVector(x->angularMomentum);
		}
	}
}
//Step6
void RigidBodySystemSimulator::updateWorldSpace(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->calculateGlobalPosition();
		}
	}
}

void RigidBodySystemSimulator::clearForces(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->clearForces();
		}
	}
}
//function for e.G. Gravity (used in euler integration)
void RigidBodySystemSimulator::applyExternalForce(Vec3 force) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		x->Force = x->Force + force;
	}
}
//function for Movement(lecture Slide 14)
void RigidBodySystemSimulator::calculateEulerIntegration(int i, float timeStep) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->m_center = x->m_center + timeStep * x->linearVelocity;
			x->linearVelocity = x->linearVelocity + timeStep * (x->Force / x->f_mass);
		}
	}
}


void RigidBodySystemSimulator::checkCollisions() {
	for (RigidBodySystem* x : m_lRigidbodies) {
		for (RigidBodySystem* y : m_lRigidbodies) {
			if (x->index < y->index) {

				Mat4 scalingX = Mat4();
				Mat4 translationX = Mat4();
				Mat4 obj2worldX = Mat4();
				scalingX.initScaling(x->m_size.x, x->m_size.y, x->m_size.z);
				translationX.initTranslation(x->m_center.x, x->m_center.y, x->m_center.z);
				obj2worldX = scalingX * x->rotation.getRotMat() * translationX;

				Mat4 scalingY = Mat4();
				Mat4 translationY = Mat4();
				Mat4 obj2worldY = Mat4();
				scalingY.initScaling(y->m_size.x, y->m_size.y, y->m_size.z);
				translationY.initTranslation(y->m_center.x, y->m_center.y, y->m_center.z);
				obj2worldY = scalingY * y->rotation.getRotMat() * translationY;

				CollisionInfo check = checkCollisionSAT(obj2worldX, obj2worldY);
				if (check.isValid) {
					computeImpact(x, y, check);
				}
			}
		}
	}
}

void RigidBodySystemSimulator::computeImpact(RigidBodySystem* x, RigidBodySystem* y, CollisionInfo info)
{
	Vec3 cP_wS = info.collisionPointWorld;


	Vec3 cP_oS_X = cP_wS - x->m_center; //"collision Point object Space X"
	Vec3 cP_oS_Y = cP_wS - y->m_center; //"collision Point object Space Y"


	Vec3 n = info.normalWorld;
	float c = 0.75f;

	Vec3 im_velX = x->linearVelocity + cross(x->angularVelocity, cP_oS_X);
	Vec3 im_velY = y->linearVelocity + cross(y->angularVelocity, cP_oS_Y);
	Vec3 vel_rel = im_velX - im_velY;

	if (dot(vel_rel, n) <= 0) {
		float zaehler = dot((-(1 + c)*vel_rel), n);
		float nenner = (1 / x->f_mass) + (1 / y->f_mass) +
			dot((cross((x->currentInertiaTensor.transformVector(cross(cP_oS_X, n))), cP_oS_X) +
				cross((y->currentInertiaTensor.transformVector(cross(cP_oS_Y, n))), cP_oS_Y)), n);

		float impuls = zaehler / nenner;

		setVelocityOf(x->index, (x->linearVelocity + ((impuls*n) / x->f_mass)));
		setVelocityOf(y->index, (y->linearVelocity - ((impuls*n) / y->f_mass)));

		x->angularMomentum = x->angularMomentum + cross(cP_oS_X, impuls*n);
		y->angularMomentum = y->angularMomentum - cross(cP_oS_Y, impuls*n);
	}
}

void RigidBodySystemSimulator::setGravity(float gravity) {
	m_externalForce = Vec3(0, -gravity, 0);
}
