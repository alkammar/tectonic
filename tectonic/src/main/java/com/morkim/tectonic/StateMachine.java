package com.morkim.tectonic;

class StateMachine {

    private State state = new NotCreated();

    private State createdState = new Created();
    private State inProgressState = new InProgress();
    private State pendingInput = new PendingInput();
    private State deadState = new Dead();

    void create() {
        state.create(this);
    }

    void start() {
        state.start(this);
    }

    void finish() {
        state.finish(this);
    }

    void askForInput() {
        state.askForInput(this);
    }

    void kill() {
        state.kill(this);
    }

    void setState(State state) {
        this.state = state;
    }

    State getCreatedState() {
        return createdState;
    }

    State getInProgressState() {
        return inProgressState;
    }

    State getPendingInput() {
        return pendingInput;
    }

    State getDeadState() {
        return deadState;
    }

    boolean isExecutable() {
        return state.isExecutable();
    }

    boolean isInProgress() {
        return state.isInProgress();
    }

    boolean isCachedExecutable() {
        return state.isCachedExecutable();
    }

    boolean isDead() {
        return state.isDead();
    }
}

interface State {
    void create(StateMachine stateMachine);

    void start(StateMachine stateMachine);

    void finish(StateMachine stateMachine);

    void askForInput(StateMachine stateMachine);

    void kill(StateMachine stateMachine);

    boolean isExecutable();

    boolean isInProgress();

    boolean isCachedExecutable();

    boolean isDead();
}

class NotCreated implements State {

    @Override
    public void create(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getCreatedState());
    }

    @Override
    public void start(StateMachine stateMachine) {

    }

    @Override
    public void finish(StateMachine stateMachine) {

    }

    @Override
    public void askForInput(StateMachine stateMachine) {

    }

    @Override
    public void kill(StateMachine stateMachine) {

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    @Override
    public boolean isCachedExecutable() {
        return false;
    }

    @Override
    public boolean isDead() {
        return false;
    }
}

class Created implements State {

    @Override
    public void create(StateMachine stateMachine) {

    }

    @Override
    public void start(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getInProgressState());
    }

    @Override
    public void finish(StateMachine stateMachine) {

    }

    @Override
    public void askForInput(StateMachine stateMachine) {

    }

    @Override
    public void kill(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getDeadState());
    }

    @Override
    public boolean isExecutable() {
        return true;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    @Override
    public boolean isCachedExecutable() {
        return true;
    }

    @Override
    public boolean isDead() {
        return false;
    }
}

class InProgress implements State {

    @Override
    public void create(StateMachine stateMachine) {

    }

    @Override
    public void start(StateMachine stateMachine) {

    }

    @Override
    public void finish(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getDeadState());
    }

    @Override
    public void askForInput(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getPendingInput());
    }

    @Override
    public void kill(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getDeadState());
    }

    @Override
    public boolean isExecutable() {
        return true;
    }

    @Override
    public boolean isInProgress() {
        return true;
    }

    @Override
    public boolean isCachedExecutable() {
        return false;
    }

    @Override
    public boolean isDead() {
        return false;
    }
}

class PendingInput implements State {

    @Override
    public void create(StateMachine stateMachine) {

    }

    @Override
    public void start(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getInProgressState());
    }

    @Override
    public void finish(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getDeadState());
    }

    @Override
    public void askForInput(StateMachine stateMachine) {

    }

    @Override
    public void kill(StateMachine stateMachine) {
        stateMachine.setState(stateMachine.getDeadState());
    }

    @Override
    public boolean isExecutable() {
        return true;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    @Override
    public boolean isCachedExecutable() {
        return false;
    }

    @Override
    public boolean isDead() {
        return false;
    }
}

class Dead implements State {

    @Override
    public void create(StateMachine stateMachine) {

    }

    @Override
    public void start(StateMachine stateMachine) {

    }

    @Override
    public void finish(StateMachine stateMachine) {

    }

    @Override
    public void askForInput(StateMachine stateMachine) {

    }

    @Override
    public void kill(StateMachine stateMachine) {

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    @Override
    public boolean isCachedExecutable() {
        return true;
    }

    @Override
    public boolean isDead() {
        return true;
    }
}
