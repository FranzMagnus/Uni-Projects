#include "RigidBodySystem.h"

RigidBodySystem::RigidBodySystem(int index, Vec3 center, Vec3 size, float mass)
{
	this->m_center = center;
	this->m_size = size;
	this->f_mass = mass;
	this->index = index;

	this->rotation = Quat();
	this->angularVelocity = Vec3();
	this->angularMomentum = Vec3();
	this->torque = Vec3();
	this->initialInertiaTensor = Mat4();
	this->currentInertiaTensor = Mat4();

	this->linearVelocity=Vec3();
	this->Force = Vec3();
	indexCounter = 0;
	//calculate positions for all points (8, because box lol) from the center position and size
	calculateLocalPosition();
}

void RigidBodySystem::calculateLocalPosition() {
	Point* point1 = new Point;
	point1->index = indexCounter;
	point1->localPosition = Vec3((-m_size.x / 2), (m_size.y / 2), (m_size.z / 2));
	point1->force = Vec3();
	point1->velocity = Vec3();
	indexCounter++;

	Point* point2 = new Point;
	point2->index = indexCounter;
	point2->localPosition = Vec3((-m_size.x / 2), (m_size.y / 2), (-m_size.z / 2));
	point2->force = Vec3(); 
	point2->velocity = Vec3();
	indexCounter++;

	Point* point3 = new Point;
	point3->index = indexCounter;
	point3->localPosition = Vec3((m_size.x / 2), (m_size.y / 2), (-m_size.z / 2));
	point3->force = Vec3();
	point3->velocity = Vec3();
	indexCounter++;

	Point* point4 = new Point;
	point4->index = indexCounter;
	point4->localPosition = Vec3((m_size.x / 2), (m_size.y / 2), (m_size.z / 2));
	point4->force = Vec3();
	point4->velocity = Vec3();
	indexCounter++;

	Point* point5 = new Point;
	point5->index = indexCounter;
	point5->localPosition = Vec3((-m_size.x / 2), (-m_size.y / 2), (m_size.z / 2));
	point5->force = Vec3();
	point5->velocity = Vec3();
	indexCounter++;

	Point* point6 = new Point;
	point6->index = indexCounter;
	point6->localPosition = Vec3((-m_size.x / 2), (-m_size.y / 2), (-m_size.z / 2));
	point6->force = Vec3();
	point6->velocity = Vec3();
	indexCounter++;

	Point* point7 = new Point;
	point7->index = indexCounter;
	point7->localPosition = Vec3((m_size.x / 2), (-m_size.y / 2), (-m_size.z / 2));
	point7->force = Vec3();
	point7->velocity = Vec3();
	indexCounter++;

	Point* point8 = new Point;
	point8->index = indexCounter;
	point8->localPosition = Vec3((m_size.x / 2), (-m_size.y / 2), (m_size.z / 2));
	point8->force = Vec3();
	point8->velocity = Vec3();
	indexCounter++;

	m_lpoints.push_back(point1);
	m_lpoints.push_back(point2);
	m_lpoints.push_back(point3);
	m_lpoints.push_back(point4);
	m_lpoints.push_back(point5);
	m_lpoints.push_back(point6);
	m_lpoints.push_back(point7);
	m_lpoints.push_back(point8);
}

void RigidBodySystem::calculateGlobalPosition() {
	for (Point* x : m_lpoints) {
		x->globalPosition = m_center + (rotation.getRotMat().transformVector(x->localPosition));
		x->velocity = linearVelocity + cross(angularVelocity, x->localPosition);
	}
}

void RigidBodySystem::calculateInitialInertiaTensor() {	
	float bruch = 1.0f / 12.0f;
	initialInertiaTensor.value[0][0] = bruch * f_mass*(m_size.y*m_size.y + m_size.z*m_size.z);
	initialInertiaTensor.value[1][1] = bruch * f_mass*(m_size.x*m_size.x + m_size.z*m_size.z);
	initialInertiaTensor.value[2][2] = bruch * f_mass*(m_size.y*m_size.y + m_size.x*m_size.x);
	initialInertiaTensor.value[3][3] = 1.0;
}

void RigidBodySystem::updateInertiaTensor() {
	//transpose rotation Matrix
	Mat4 rotTran = rotation.getRotMat();
	rotTran.transpose();
	//compute current Inertia tensor Step 5
	currentInertiaTensor = rotation.getRotMat() * initialInertiaTensor.inverse() * rotTran;
}

void RigidBodySystem::applyForce(Vec3 loc, Vec3 force)
{
	Point* pointTmp = new Point;
	pointTmp->index = indexCounter;
	indexCounter++;
	pointTmp->localPosition = loc;
	pointTmp->force = force;
	pointTmp->velocity = Vec3();
	m_lpoints.push_back(pointTmp);
}

void RigidBodySystem::computeForce() {
	Vec3 newForce = Vec3();
	for (Point* x : m_lpoints) {
		newForce += x->force;
	}
	this->Force = newForce;
}

void RigidBodySystem::computeTorque() {
	Vec3 newTorque = Vec3();
	for (Point* x : m_lpoints) {
		newTorque += cross(x->localPosition, x->force);
	}
	this->torque = newTorque;
}

void  RigidBodySystem::clearForces() {
	for (int i = 0; i < m_lpoints.size() - 8; i++) {
		m_lpoints.pop_back();
	}
}



