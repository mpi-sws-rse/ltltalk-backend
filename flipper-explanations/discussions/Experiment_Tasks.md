# Experiment Tasks 

In our new interactive Flipper, different types of tests are going to be added with complexity of 
Beginner, Medium and difficult. List of tasks are as follow:


## Tasks

There are three different level of tasks sets ahead. Please, read all of them first to get the rough idea how they look like.

Now start solving them (if you haven't already, check the tutorial section to get you started). After finishing each set of tasks,
reset the environment (blue *RESET button*). Remember, reseting only affects front-end, all the learnt rules are preserved. 
While you can solve the tasks in the best way you see fit, there is a benefit in trying to provide a general solution 
(that would work in more than one situation) because you can reuse solutions as building blocks in the tasks that follow. 
It might also be useful to define some basic building blocks that are not tasks themselves (e.g. `pick green`for `pick item is green`).



### Task 1

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

 
  * Get one green triangle from location [10,8] then move 6 steps backward and pick one square.
    (`picked_1_green_traingle_[10,8] B at_[4,8] & E(picked_1_square_[4,8])`)
  


### Task 2

Current environment: robot at location [2,4], two triangles, one circle and one square(red, green and yellow) at [4,0], One square and circle at (yellow and blue) at [3,8], four cicles (red,blue and green) 
at [7,4] and one triangles (red) at [10,8]


  * Get one green circle, one red triangle and one yellow square from location [4,0] then step in water.
      (`picked_1_green_circle_[4,0] & picked_1_red_triangle_[4,0] & picked_1_yellow_square_[4,0] B at_wet`)




### Task 3

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]


  * Get one red triangle at location [10,8] while staying dry.
      (`dry Ux picked_1_red_traingle_[10,8]`)



### Task 4

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]


  * Get one blue square from location [11,1] and move forward to four steps then pick one green item
      (`picked_1_blue_square_[11,1] &  at_[4,8] & E(picked_1_square_[4,8])`)




### Task 5

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

  * Go to the location [1,2] and pick one green circle, one yellow triangle then take them at location [3,3]

### Task 6

Current environment: robot at location [0,3], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

 
  * Move 3 steps right then 4 steps and pick a yellow item from that space. Afterward visit any field containing both circle and square-shaped item 
  then pick from that location and step in water.
  


### Task 7

Current environment: robot at location [2,4], two triangles(yellow and red) at [1,2], One square and circle at (yellow and blue) at [3,2], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

  * Go to location [3,2] and pick all item from that space and afterward get two triangle from location [10,8] and stay dry. 
  

### Task 8

Current environment: robot at location [1,2], two triangles and one circle (green) at [1,7], One square (yellow) at [3,8], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8] 

  * Get all green item at [1,7] then move forward 6 steps to pick one red circle and then move to water tile.


### Task 9

Current environment: robot at location [0,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], two cicles, one triangle and square 
(red,yellow and green) at [3,4], one square (blue) at [10,8] and two circles (red, green) at [4,8]

  * Visit at [3,4] and pick one red circle , one green square and one yellow triangle then move 3 step forward pick two red items while staying dry.

### Task 10

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], two square and one circle at (red and green) at [9,4], four cicles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8], water tiles ([5,4],[2,1],[4,5])

  * Get two red and green items with circle or square shaped (it can be any defined either green or red) from location [9,4] then move 5 steps forward and step in water tile