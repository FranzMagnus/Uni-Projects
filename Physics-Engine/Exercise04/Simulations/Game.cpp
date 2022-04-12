#include "Game.h"
#include "pcgsolver.h"
#define RAND_MAX
using namespace std;

Grid::Grid(int x, int y) {
	for (int i = 0; i < x*y; i++) {
		grid.push_back(0);
	}
	this->w = x;
	this->l = y;
}

float Grid::get(int x, int y) {
	return grid[x + y * w];
}

void Grid::set(int x, int y, float v) {
	grid[x + y * w] = v;
}

int Grid::getWidth() {
	return w;
}

int Grid::getLength() {
	return l;
}

Game::Game()
{
	m_iTestCase = 0;
	m_vfMovableObjectPos = Vec3();
	m_vfMovableObjectFinalPos = Vec3();
	m_vfRotate = Vec3();
	T_width = 50;
	T_length = 50;
	spacing = 0.05f;
	alpha = 0.05f;
	num_sources = 1;
	m_externalForce = Vec3(0,-500,0);
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
	index_counter = 0;
	index_counter_stationary = 0;
	m_fGravity = 0.0f;
	shoot = true;
	shoot_counter = 0;
}

const char * Game::getTestCasesStr() {
	return "Explicit_solver, Implicit_solver";
}

void Game::reset() {
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
	m_lRigidbodies.clear();
	m_lRigidbodies_stationary.clear();
}

void Game::initUI(DrawingUtilitiesClass * DUC)
{
	
	this->DUC = DUC;
	/*
	// setting width
	TwAddVarRW(DUC->g_pTweakBar, "Width", TW_TYPE_INT32, &T_width, "min=10.00 max=500.0 step=1.0");

	// setting length
	TwAddVarRW(DUC->g_pTweakBar, "Length", TW_TYPE_INT32, &T_length, "min=10.00 max=40.0 step=1.0");

	// setting alpha
	TwAddVarRW(DUC->g_pTweakBar, "alpha", TW_TYPE_FLOAT, &alpha, "min=0.00 max=1.0 step=0.01");

	// setting number of sources
	TwAddVarRW(DUC->g_pTweakBar, "sources", TW_TYPE_INT32, &num_sources, "min=1.00 max=50.0 step=1.0");
	*/
}

void Game::notifyCaseChanged(int testCase)
{
	m_iTestCase = testCase;
	m_vfMovableObjectPos = Vec3(0, 0, 0);
	m_vfRotate = Vec3(0, 0, 0);
	m_lRigidbodies.clear();
	index_counter = 0;
	m_lRigidbodies_stationary.clear();
	index_counter_stationary = 0;

	cout << "Paintball Shootingrange!\n";
	T = new Grid(T_width, T_length); 
	addRigidBodiesToList();
	
}

void Game::externalForcesCalculations(float timeElapsed)
{

}

Grid* Game::diffuseTemperatureExplicit(float timeStep) {
	//code adepted from the Hints translated into 2D

	//defining new Grid
	Grid* newT = new Grid(T->getWidth(), T->getLength());

	//computing the Factor
	float F = alpha * timeStep / (spacing*spacing);

	//computing the new Grid entries
	for (int x = 1; x < T->getWidth() - 1; x++) {
		for (int y = 1; y < T->getLength() - 1; y++) {
			float x_change = T->get(x - 1, y) - (2 * T->get(x, y)) + T->get(x + 1, y);
			float y_change = T->get(x, y - 1) - (2 * T->get(x, y)) + T->get(x, y + 1);
			float full_change = F * (x_change + y_change) + T->get(x, y);

			newT->set(x, y, full_change);
		}
	}


	//make sure that the temperature in boundary cells stays zero
	for (int x = 0; x < newT->getWidth(); x++) {
		for (int y = 0; y < newT->getLength(); y++) {
			if (x == 0 || y == 0 || x == newT->getWidth() - 1 || y == newT->getLength() - 1) {
				newT->set(x, y, 0);
			}
		}
	}

	return newT;
}

void setupB(std::vector<Real>& b, Grid* T) {//add your own parameters

	//coping the grid entries into the vector b
	for (int x = 0; x < T->getWidth(); x++) {
		for (int y = 0; y < T->getLength(); y++) {
			b.at(x + y * T->getWidth()) = T->get(x, y);
		}
	}
}

void fillT(Grid* T, vector<Real> x_v) {//add your own parameters

	//coping new values from x back into T
	for (int x = 0; x < T->getWidth(); x++) {
		for (int y = 0; y < T->getLength(); y++) {
			T->set(x, y, x_v[x + y * T->getWidth()]);
		}
	}

	//make sure that the temperature in boundary cells stays zero
	for (int x = 0; x < T->getWidth(); x++) {
		for (int y = 0; y < T->getLength(); y++) {
			if (x == 0 || y == 0 || x == T->getWidth() - 1 || y == T->getLength() - 1) {
				T->set(x, y, 0);
			}
		}
	}
}

void setupA(SparseMatrix<Real>& A, double factor, Grid* T, float spacing, float timeStep) {//add your own parameters
	// code adepted from the Hints translated into 2D

	//computing the Factor 
	float F = factor * timeStep / (spacing*spacing);

	// avoid zero rows in A -> set the diagonal value for boundary cells to 1.0
	for (int i = 0; i < T->getWidth() * T->getLength(); i++) {
		A.set_element(i, i, 1); // set diagonal
	}

	//computing the remaining matrix entries
	for (int x = 1; x < T->getWidth() - 1; x++) {
		for (int y = 1; y < T->getLength() - 1; y++) {
			//Filling the matrix with the rigth Factors
			A.add_to_element(x + y * T->getWidth(), (x + 1) + y * T->getWidth(), -F);
			A.add_to_element(x + y * T->getWidth(), (x - 1) + y * T->getWidth(), -F);
			A.add_to_element(x + y * T->getWidth(), x + y * T->getWidth(), (2 * F) * 2);
			A.add_to_element(x + y * T->getWidth(), x + (y + 1) * T->getWidth(), -F);
			A.add_to_element(x + y * T->getWidth(), x + (y - 1) * T->getWidth(), -F);

		}
	}
}

void Game::diffuseTemperatureImplicit(float timeStep) {//add your own parameters
	// solve A T = b
	const int N = T->getWidth()*T->getLength();//N = sizeX*sizeY*sizeZ
	SparseMatrix<Real> *A = new SparseMatrix<Real>(N);
	std::vector<Real> *b = new std::vector<Real>(N);

	setupA(*A, alpha, T, spacing, timeStep);
	setupB(*b, T);

	// perform solve
	Real pcg_target_residual = 1e-05;
	Real pcg_max_iterations = 1000;
	Real ret_pcg_residual = 1e10;
	int  ret_pcg_iterations = -1;

	SparsePCGSolver<Real> solver;
	solver.set_solver_parameters(pcg_target_residual, pcg_max_iterations, 0.97, 0.25);

	std::vector<Real> x(N);
	for (int j = 0; j < N; ++j) { x[j] = 0.; }

	// preconditioners: 0 off, 1 diagonal, 2 incomplete cholesky
	solver.solve(*A, *b, x, ret_pcg_residual, ret_pcg_iterations, 0);
	// x contains the new temperature values
	fillT(T, x);//copy x to T
}

void Game::simulateTimestep(float timeStep)
{
	if (shoot_counter == 15) {
		shoot_counter = 0;
		shoot = true;
	}
	shoot_counter++;
	// update current setup for each frame
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
	T = diffuseTemperatureExplicit(timeStep);
 	if (DXUTWasKeyPressed(VK_SPACE) && shoot) {
		shoot = false;
		shoot_func();
	}
}

void Game::shoot_func() {
	float rand_x = ((std::rand() % T_width) - (T->getWidth() / 2)) / 10.0f;
	float rand_y = ((std::rand() % T_length) - (T->getLength() / 3)) / 10.0f;
	int index = addRigidBody(Vec3(rand_x, rand_y, -1), 0.1, 1);
	setVelocityOf(index, Vec3(0, 0, 50));
	Mat4 orientation = Mat4();
	int rand_x_rot = std::rand() % 90;
	int rand_y_rot = std::rand() % 90;
	int rand_z_rot = std::rand() % 90;
	orientation.initRotationXYZ(rand_x_rot, rand_y_rot, rand_z_rot);
	setOrientationOf(index, Quat(orientation));
	getRigidBody(index)->fixed = false;
}

RigidBodySystem* Game::getRigidBody(int index) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (index == x->index) {
			return x;
		}
	}
}

void Game::addRigidBodiesToList() {
	//iterating over the grid and adding a rigidbody to the list for each point in the grid
	for (float x = 0; x < T->getWidth(); x++) {
		for (float y = 0; y < T->getLength(); y++) {
			addRigidBody_stationary(Vec3((x-(T->getWidth()/2))/10.0f,( y-(T->getLength() / 5)) / 10.0f, 0), Vec3(0.1, 0.1, 0.1), 1);
		}
	}
}

int Game::addRigidBody_stationary(Vec3 position, Vec3 size, int mass)
{
	RigidBodySystem* tmp = new RigidBodySystem(index_counter_stationary, position, size, mass);
	tmp->calculateGlobalPosition();
	tmp->calculateInitialInertiaTensor();
	index_counter_stationary++;
	m_lRigidbodies_stationary.push_back(tmp);
	return tmp->index;
}

void Game::setFixed() {
	
}

void Game::calculateColors()
{
	//iterating over the grid and calculating colors for rigib bodies
	for (int x = 0; x < T->getWidth(); x++) {
		for (int y = 0; y < T->getLength(); y++) {

			//calculating the colour
			Vec3 col = Vec3(1, 1 / (1 + T->get(x, y)), 1 / (1 + T->get(x, y)));

			//setting color to the rigid body
			//index at list is x*(y*x) (reverse to "get(x, y)")
			setColorOf(x + (T->getWidth() * y), col);
		}
	}
}

void Game::drawStuff() {
	Mat4 scaling = Mat4();
	Mat4 translation = Mat4();
	Mat4 obj2world = Mat4();
	calculateColors();
	//iterating over all ridigbodies in list
	for (RigidBodySystem* x : m_lRigidbodies) {
		//getting scaling matrix
		scaling.initScaling(x->m_size.x, x->m_size.y, x->m_size.z);

		//getting translation matrix
		translation.initTranslation(x->m_center.x, x->m_center.y, x->m_center.z);

		//calculating obj2world matrix
		obj2world = scaling * x->rotation.getRotMat() * translation;

		//setting up colo
		DUC->setUpLighting(Vec3(1,0,0), Vec3(0, 0, 0), 1.0f, Vec3(0, 0, 0));

		//draw rigidbody
		DUC->drawRigidBody(obj2world);
	}
	for (RigidBodySystem* x : m_lRigidbodies_stationary) {
		//getting scaling matrix
		scaling.initScaling(x->m_size.x, x->m_size.y, x->m_size.z);

		//getting translation matrix
		translation.initTranslation(x->m_center.x, x->m_center.y, x->m_center.z);

		//calculating obj2world matrix
		obj2world = scaling * x->rotation.getRotMat() * translation;

		//setting up color
		Vec3 colordummy = x->color;
		DUC->setUpLighting(colordummy, Vec3(0, 0, 0), 1.0f, Vec3(0, 0, 0));

		//draw rigidbody
		DUC->drawRigidBody(obj2world);
	}
}

void Game::drawFrame(ID3D11DeviceContext* pd3dImmediateContext)
{
	drawStuff();
}

void Game::onClick(int x, int y)
{
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}

void Game::onMouse(int x, int y)
{
	m_oldtrackmouse.x = x;
	m_oldtrackmouse.y = y;
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}

int Game::getNumberOfRigidBodies()
{
	return m_lRigidbodies.size();
}

Vec3 Game::getPositionOfRigidBody(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			return x->m_center;
		}
	}
}

Vec3 Game::getLinearVelocityOfRigidBody(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			return x->linearVelocity;
		}
	}
}

Vec3 Game::getAngularVelocityOfRigidBody(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			return x->angularVelocity;
		}
	}
}

void Game::applyForceOnBody(int i, Vec3 loc, Vec3 force)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		x->applyForce(loc, force);
	}
}

int Game::addRigidBody(Vec3 position, Vec3 size, int mass)
{
	RigidBodySystem* tmp = new RigidBodySystem(index_counter, position, size, mass);
	tmp->calculateGlobalPosition();
	tmp->calculateInitialInertiaTensor();
	index_counter++;
	m_lRigidbodies.push_back(tmp);
	return tmp->index;
}

void Game::setOrientationOf(int i, Quat orientation)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->rotation = orientation;
		}
	}
}

void Game::setVelocityOf(int i, Vec3 velocity)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->linearVelocity = velocity;
		}
	}
}

void Game::setColorOf(int i, Vec3 col)
{
	for (RigidBodySystem* x : m_lRigidbodies_stationary) {
		if (i == x->index) {
			x->color = col;
		}
	}
}

//precompute inertia Tensor
void Game::precomputeInertiaTensor(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->calculateInitialInertiaTensor();
		}
	}
}

// Functions for step 1-6 from the tutorial
// Also take a look at lecture Slide 14

void Game::computeForce(int i)
{
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->computeForce();
		}
	}
}

// Step 1
void Game::computeTorque(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->computeTorque();
		}
	}
}

//Step2
void Game::integrateRotation(int i, float timeStep) {
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
void Game::integrateAngularMomentum(int i, float timeStep) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			Vec3 newAngularMomentum = x->angularMomentum + (timeStep)*x->torque;
			x->angularMomentum = newAngularMomentum;
		}
	}
}
//Step4
void Game::updateInertiaTensor(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->updateInertiaTensor();
		}
	}
}
//Step5
void Game::updateAngularVelocity(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->angularVelocity = x->currentInertiaTensor.transformVector(x->angularMomentum);
		}
	}
}
//Step6
void Game::updateWorldSpace(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->calculateGlobalPosition();
		}
	}
}

void Game::clearForces(int i) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->clearForces();
		}
	}
}
//function for e.G. Gravity (used in euler integration)
void Game::applyExternalForce(Vec3 force) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		x->Force = x->Force + force;
	}
}
//function for Movement(lecture Slide 14)
void Game::calculateEulerIntegration(int i, float timeStep) {
	for (RigidBodySystem* x : m_lRigidbodies) {
		if (i == x->index) {
			x->m_center = x->m_center + timeStep * x->linearVelocity;
			x->linearVelocity = x->linearVelocity + timeStep * (x->Force / x->f_mass);
		}
	}
}

void Game::checkCollisions() {
	for (RigidBodySystem* x : m_lRigidbodies) {
		for (RigidBodySystem* y : m_lRigidbodies_stationary) {
			if (((x->m_center.x <= y->m_center.x+0.4 && x->m_center.x >= y->m_center.x - 0.4)
				&&(x->m_center.y <= y->m_center.y + 0.4 && x->m_center.y >= y->m_center.y - 0.4)
				&&(x->m_center.z <= y->m_center.z + 0.4 && x->m_center.z >= y->m_center.z - 0.4))) {

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
					if (y->fixed) {
						int x_temp = y->index%T->getWidth();
						int y_temp = y->index / T->getWidth();
						T->set(x_temp, y_temp, 50);
					}
				}
			}
		}
	}

	for (RigidBodySystem* x : m_lRigidbodies) {
		for (RigidBodySystem* y : m_lRigidbodies) {
			if ((x->index < y->index 
				&& (x->m_center.x <= y->m_center.x + 0.4 && x->m_center.x >= y->m_center.x - 0.4)
				&& (x->m_center.y <= y->m_center.y + 0.4 && x->m_center.y >= y->m_center.y - 0.4)
				&& (x->m_center.z <= y->m_center.z + 0.4 && x->m_center.z >= y->m_center.z - 0.4))) {

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

void Game::computeImpact(RigidBodySystem* x, RigidBodySystem* y, CollisionInfo info)
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

void Game::setGravity(float gravity) {
	m_externalForce = Vec3(0, -gravity, 0);
}
