// allow nav mode to be tested outside of BB simulator
if (!blackberry.focus.DOWN) {
    blackberry.focus.DOWN = navigationController.DOWN;
    blackberry.focus.UP = navigationController.UP;
    blackberry.focus.RIGHT = navigationController.RIGHT;
    blackberry.focus.LEFT = navigationController.LEFT;
}
