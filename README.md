# HumanSimulation
This is a simple java application where you can simulate humans searching for food. Each human has a different behaviour and will create variations on that behaviour when creating children. With this application, you can play around with different evoirements and test which behaviour will perform best.  

The jar file is located inside the out/artifacts/MyFirstGui_jar folder.  

# Features
- Adding humans and food to a 2d grid world.  
- Advancing time and viewing the behaviour of the humans in the 2d grid world.  
- Changing up the parameters of the grid world and humans (ex. choosing how much the humans need to eat and how often).  
- Creating a simple graph that shows the amount of humans as time goes on.  
- A information panel that displays information about all grid world entities or grid world entities at a certain position.  

# Tutorial
Say you want to simulate 5 humans in a gridworld. First add the humans by clicking the 'Add' button and after that you can choose to spawn humans randomly troughout the grid or choose the position yourself by clicking the 'Add Human Position' button and then clicking a certain cell in the grid.  
The 'Add Food Random' and 'Add Food Position' buttons work the same as their human counterpart. These can be used to create some extra food on the map manually.  
  
Now you can start advancing time. You can do this by pressing the 'Advance' button and then choosing the 'Advance Time' button (advances time one day) or the 'Automatic' button (start advancing time automatically). To stop advancing automatically you just need to press the 'Automatic' button again. You can also increase or decrease the automatic speed by pressing one of the other buttons.  
  
If at any time you want to view the stats of a human, you can click the human in the grid world. The information about the human is displayed in the info screen. If there are multiple grid world entities on the position you clicked, they will all be displayed. You can also sort all information by either days survived of current food reserves.  
  
You can also change the parameters of the grid world and humans. This means that you can change the amount humans eat, the frequency humans eat, the amount of food humans need to breed, the frequency humans will try to breed, the amount of food generates automatically and the frequency of food spawns. First press the 'Grid World' button and then you can freely change the variables. After you change the variables, don't forget to press apply!  
Here you can also display all current alive humans if you want.   
   
Lastly you can create a simple graph of the amount of humans each day by clicking the 'Statistics' button and then clicking the 'Display Human Population Graph' button. You can also reset the statistics whenever you want.  

# What I learned
- I learned a lot about creating a GUI with java swing.
- I learned a lot about creating a complex system to simulate behaviours in a changing envoirement.
