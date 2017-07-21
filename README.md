# SEMPRE 3.0-robo: Semantic Parsing with Execution, modified for usage in robotic world 
(based on original interactive sempre repository https://github.com/sidaw/sempre-interactive)

## What is SEMPRE and semantic parsing?

See [SEMPRE-DOCUMENTATION.md](/SEMPRE-DOCUMENTATION.md).

## What does this "robo" version of SEMPRE do?

This specific version of SEMPRE is a modification of the Voxelurn project which
uses SEMPRE as the basis for a block-building world where input utterances act
as build instructions for the block world (displayed with a browser-based
JavaScript application). This version SEMPRE spcifically models a simple
environment where a robot can move around a small map and pick/drop colored
items. In addition to using the core language to control the robot, a user can
define custom commands which are learned by the system improving its ability to
understand more natural language-style commands.

# Installation

## Requirements

You must have the following already installed on your system.

- Java 8 (not 7)
- Ant 1.8.2
- Ruby 1.8.7 or 1.9
- wget

Other dependencies will be downloaded as you need them. SEMPRE has been tested
on Ubuntu Linux 16.04, Debian 8, and MacOS X. Your mileage will vary depending
on how similar your system is.

## Easy setup

1. Clone this GitHub repository:

        git clone https://gavran@gitlab.mpi-sws.org/gavran/sempre-interactive.git (https)
        git clone git-rts@gitlab.mpi-sws.org:gavran/sempre-interactive.git(ssh)

2. Download the minimal core dependencies (all dependencies will be placed in `lib`):

        ./pull-dependencies core

3. Compile the source code (this produces `libsempre/sempre-core.jar`):

        ant interactive

4. Run server (defaults to port 8410):

        ./interactive/run @mode=voxelurn

The command should execute, display some information about the grammar, and
wait for input from the browser client (server can be stopeed with `Ctrl+D`.
To go further with running sempre in the context of Robotic world, check
[ROBO-DOCUMENTATION.md](https://gitlab.mpi-sws.org/gavran/sempre-interactive/blob/master/ROBO-DOCUMENTATION.md)

### General Troubleshooting

Here are some general tips for troubleshooting:

- If the browser client is acting weirdly
	- Pull the newest version for the repository (no need to restart yarn)
	- On the panel on the right side, press `Clear`
- If the code does not compile or immediately fails at runtime
	- Pull newest version of the code
	- Run `ant clean` and then `ant interactive`
- If the server throws an error during use with the browser client
	- Open an issue detailing the server-side error and the client-side input

# Browser Client

The corresponding browser-based client to this project can be found
[here](https://gitlab.mpi-sws.org/gavran/naturalizing-robotic-language).