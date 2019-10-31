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

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], two circles and two squares (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

 
  * Get one green triangle from location [10,8] then move 3 steps left direction and pick one square item.
    (`(picked_1_green_traingle_[10,8] B at_[4,8]) & E(picked_1_square_[4,8])`)
  


### Task 2

Current environment: robot at location [2,4], two triangles, one circle and one square(red, green and yellow) at [4,0], One square and circle at (yellow and blue) at [3,8], four circles (red,blue and green) 
at [7,4] and one triangles (red) at [10,8]


  * Get one green circle, one red triangle and one yellow square from location [4,0] then step in water.
      (`((picked_1_green_circle_[4,0] & picked_1_red_triangle_[4,0]) & picked_1_yellow_square_[4,0]) B at_wet`)




### Task 3

Current environment: robot at location [2,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,7], four circles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]


  * Get one red triangle at location [10,7] while staying dry.
      (`dry Ux picked_1_red_traingle_[10,8]`)



### Task 4

Current environment: robot at location [1,0], two circles (green and yellow) at [4,1], One triangle and circle at (red and blue) at [3,7], three circles and one square (green and red) 
at [7,4], one square (blue) at [11,4] and two triangles (red, green) at [10,8]


  * Get one blue square from location [11,1] and move 1 step downward then 5 steps left to pick one green item
      (`(picked_1_blue_square_[11,1] &  at_[11,5]) & E(picked_1_square_[11,5])`)




### Task 5

Current environment: robot at location [2,4], two squares (yellow and blue) at [4,0], One triangle and circle at (green and blue) at [3,8], four circles (red,blue and green,yellow) 
at [7,4], two circles and one triangle (green,yellow) at [1,2] and two triangles (red, green) at [10,8]

  * Go to the location [1,2] and pick one green circle, one yellow triangle then take them at location [3,3]
        (`(F(at_[1,2]) & F(picked_1_green_circle_[1,2]) & F(picked_1_yellow_triangle_[1,2]) ) & E(at_[3,3])`)


### Task 6

Current environment: robot at location [0,3], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,7], four circles (red,blue) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

 
  * Move 3 steps down then 4 steps right and pick a yellow item from that space and in the end step in water
   (`((at_[3,3] B at_[3,7]) & picked_1_yellow_item_[3,7]) & E(at_wet)`)


### Task 7

Current environment: robot at location [2,4], two triangles(yellow and red) at [1,2], One square and circle at (yellow and blue) at [3,2], four circles (red,blue and green) 
at [7,4], one square (blue) at [11,1] and two triangles (red, green) at [10,8]

  * Go to location [3,2] and pick all item from that space then get two triangles from location [10,8] and stay dry.

      (`((F(at_[3,2]) & F(picked_every_item_[3,2])) & (at_dry Ux picked_2_triangle_triangle_[10,8])`)

  
  

### Task 8

Current environment: robot at location [1,2], two triangles and one circle (green) at [0,8], One circle (yellow) at [4,8], four circles (red) 
at [3,7], one triangle (red) at [11,1] and two triangles (red, green) at [10,8] 

  * Get all green item from location [0,8] then move forward 4 steps to pick one red circle then move to water tile.
     (`((picked_every_green_item_[0,8] & at_[4,8]) & picked_1_red_circle_[4,8]) & E(at_wet)`)



### Task 9

Current environment: robot at location [0,4], two triangles(yellow and red) at [4,0], One square and circle at (yellow and blue) at [3,8], two circles, one triangle and square 
(red,yellow and green) at [3,4], one square (blue) at [10,8] and two circles (red, green) at [4,8]

  * Visit at [3,4] and pick one red circle , one green square and one yellow triangle then move 3 step forward pick two red items while staying dry.
       (`( (at_[3,4] & picked_1_red_circle_[3,4]) & (picked_1_green_square_[3,4] & picked_1_yellow_triangle_[3,4])) B ( at_dry Ux (at_location_[6,4] & picked_2_red_items_[6,4]) )`)


### Task 10

Current environment: robot at location [2,4], two triangles(green and red) at [4,0], two square and one circle at (red and green) at [9,4], four circles (blue) 
at [7,4], one square (yellow) at [11,1] and two triangles (red, green) at [10,8], water tiles ([5,4],[2,1],[4,5])

  * Get two red items with circle or square shaped from location [9,4] then move 3 steps downward and step in water tile
       (` ((F(picked_2_red_square_[9,4]) | F(picked_2_red_circle_[9,4])) & F(at_[9,1])) & E(at_wet)`)
