# LTLTalk: Interactive Synthesis from Natural Language and Examples
This document describes the setup of the full (frontend and backend)
demo robotic world for the LTLTalk.

## Backend
Backend consists of two modules: grammar expansion module and specification synthesis module.
- if it does not exist, clone the backend repository (`git clone https://gitlab.mpi-sws.org/gavran/flipper-backend.git`). 
Enter the repository and switch to the branch examples (`git checkout examples`). 

### Grammar expansion module

Grammar expansion module takes a natural language description `d` and an LTL specification `s` and infers new production
rules to be added to the underlying grammar.

**Installation**: 
- in order to install the interactive semantic parser (if it is not already installed) do:
  - have Java 8 (not 7), Ant 1.8.2, Ruby 1.8.7 or 1.9, wget installed on the system
  - run `./pull-dependencies core` to get necessary libraries

**Running**
- run `ant interactive` to compile any changes
- if necessary to delete any previously learned production rules of the grammar, run `rm int-output/grammar.log.json`
-run `./interactive/run @mode=voxelurn`. The interactive semantic parser now listens at port 8410

### Specification synthesis module
Specification synthesis module takes a natural language description `d` and an example trace `t` and creates a number
 of candidate specifications.
Subsequently, through interaction with the user, it narrows it down to only one.

**Installation**
Enter the subfolder `examplesServer`. 
- the module is implemented in Python3. For running it cleanly, create a virtualenvironment 
([link](https://virtualenvwrapper.readthedocs.io/en/latest/)). 
  - install the virtualenvwrapper package by running `pip install virtualenvwrapper` and create a folder where the different 
  virtual environments will be stored (e.g., `export WORKON_HOME=~/Envs`, `mkdir -p $WORKON_HOME`).
  - run `source /usr/local/bin/virtualenvwrapper.sh` (Linux) or 
  `source /Library/Frameworks/Python.framework/Versions/2.7/bin/virtualenvwrapper.sh` (Mac)
  - run `mkvirtualenv -p python3 specSynth` to start create a virtual environment `specSynth` with python3 as interpreter. 
 - run `workon specSynth` to use the virtual machine. Now, `python` and `pip` will be used from that virtual machine. If you decided not to use a virtual machine, make sure
 to use `python3` and `pip3` instead.
 - run `pip install -r requirements.txt` to install the necessary python modules
 
 **Running**
 - run `export FLASK_APP=flask-routes.py` and then `flask run`
 
 **Tuning**
 - file `examplesServer/constants.py` contains the parameters. For example, `NUM_CANDIDATE_FORMULAS` specifies how many
 candidate formulas are generated before the interaction begins. 
 
## Frontend

**Installation**:
- clone the repository (`git clone https://gitlab.mpi-sws.org/gavran/flipper-frontend.git`)
- enter the folder `voxelurn` inside the repository
- run `yarn install` to install the packages

**Running**:
run `yarn start`

