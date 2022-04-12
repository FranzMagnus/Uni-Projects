#include "DiffusionSimulator.h"
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


DiffusionSimulator::DiffusionSimulator()
{
	m_iTestCase = 0;
	m_vfMovableObjectPos = Vec3();
	m_vfMovableObjectFinalPos = Vec3();
	m_vfRotate = Vec3();
	T_width = 40;
	T_length = 40;
	spacing = 0.05f;
	alpha = 0.42f;
	num_sources = 1;
}

const char * DiffusionSimulator::getTestCasesStr() {
	return "Explicit_solver, Implicit_solver";
}

void DiffusionSimulator::reset() {
	m_mouse.x = m_mouse.y = 0;
	m_trackmouse.x = m_trackmouse.y = 0;
	m_oldtrackmouse.x = m_oldtrackmouse.y = 0;
	
}

void DiffusionSimulator::initUI(DrawingUtilitiesClass * DUC)
{
	this->DUC = DUC;
	// setting width
	TwAddVarRW(DUC->g_pTweakBar, "Width", TW_TYPE_INT32, &T_width, "min=10.00 max=500.0 step=1.0");
	
	// setting length
	TwAddVarRW(DUC->g_pTweakBar, "Length", TW_TYPE_INT32, &T_length, "min=10.00 max=40.0 step=1.0");

	// setting alpha
	TwAddVarRW(DUC->g_pTweakBar, "alpha", TW_TYPE_FLOAT, &alpha, "min=0.00 max=1.0 step=0.01");

	// setting number of sources
	TwAddVarRW(DUC->g_pTweakBar, "sources", TW_TYPE_INT32, &num_sources, "min=1.00 max=50.0 step=1.0");
}

void DiffusionSimulator::notifyCaseChanged(int testCase)
{
	m_iTestCase = testCase;
	m_vfMovableObjectPos = Vec3(0, 0, 0);
	m_vfRotate = Vec3(0, 0, 0);

	switch (m_iTestCase)
	{
	case 0:
		cout << "Explicit solver!\n";
		T = new Grid(T_width, T_length);

		//setting random temperatures for a selected muber of sources
		for (int i = 0; i < num_sources; i++) {

			//using % to get random value in the right interval
			int rand_x = std::rand() % T_width;
			int rand_y = std::rand() % T_length;

			//we are using -1 to the power of a random value to get positiv and negative
			//temperatures, negative will be blue and positive will be red
			//5000 is a randomly chose value, which is working well as maximum for temperature
			int rand_temp = ((int)pow((-1), std::rand())) * std::rand() % 5000;

			T->set(rand_x, rand_y, rand_temp);
		}

		break;
	case 1:
		cout << "Implicit solver!\n";
		T = new Grid(T_width, T_length);

		//setting random temperatures for a selected muber of sources
		for (int i = 0; i < num_sources; i++) {

			//using % to get random value in the right interval
			int rand_x = std::rand() % T_width;
			int rand_y = std::rand() % T_length;

			//we are using -1 to the power of a random value to get positiv and negative
			//temperatures, negative will be blue and positive will be red
			//5000 is a randomly chose value, which is working well as maximum for temperature
			int rand_temp = ((int)pow((-1), std::rand())) * std::rand() % 5000;

			T->set(rand_x, rand_y, rand_temp);
		}
		break;
	default:
		cout << "Empty Test!\n";
		break;
	}
}

Grid* DiffusionSimulator::diffuseTemperatureExplicit(float timeStep) {
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
	float F = factor*timeStep / (spacing*spacing);

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
			A.add_to_element(x + y * T->getWidth(), x + y * T->getWidth(), (2 * F)*2);
			A.add_to_element(x + y * T->getWidth(), x + (y + 1) * T->getWidth(), -F);
			A.add_to_element(x + y * T->getWidth(), x + (y - 1) * T->getWidth(), -F);

		}
	}
}


void DiffusionSimulator::diffuseTemperatureImplicit(float timeStep) {//add your own parameters
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
	fillT(T,x);//copy x to T
}



void DiffusionSimulator::simulateTimestep(float timeStep)
{
	// to be implemented
	// update current setup for each frame
	switch (m_iTestCase)
	{
	case 0:
		T = diffuseTemperatureExplicit(timeStep);
		break;
	case 1:
		diffuseTemperatureImplicit(timeStep);
		break;
	}
}

void DiffusionSimulator::drawObjects()
{
	//setting scale of sphere as the spacing so it looks kinda like a screen
	Vec3 scale = (spacing, spacing, spacing);
	//iterating over the grid and drawing spheres
	for (int x = 0; x < T->getWidth(); x++) {
		for (int y = 0; y < T->getLength(); y++) {

			//calculating the position of the sphere
			Vec3 pos = Vec3((x - T->getWidth() / 2) * spacing, (y - T->getLength() / 2) * spacing, 0);

			//calculating the colour of the sphere
			//more or less red for positive values (and 0)
			Vec3 col;
			if (T->get(x, y) >= 0) {
				col = Vec3(1, 1 / (1 + T->get(x, y)), 1 / (1 + T->get(x, y)));
			}
			else {
				col = Vec3(1 / (1 - T->get(x, y)), 1 / (1 - T->get(x, y)), 1);
			}

			//setting the colour of the sphere
			DUC->setUpLighting(col, Vec3(0, 0, 0), 1.0f, Vec3(0, 0, 0));

			//drawing a sphere
			this->DUC->drawSphere(pos, scale);
		}
	}
}


void DiffusionSimulator::drawFrame(ID3D11DeviceContext* pd3dImmediateContext)
{
	drawObjects();
}

void DiffusionSimulator::onClick(int x, int y)
{
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}

void DiffusionSimulator::onMouse(int x, int y)
{
	m_oldtrackmouse.x = x;
	m_oldtrackmouse.y = y;
	m_trackmouse.x = x;
	m_trackmouse.y = y;
}
