# Data visualization

## How to use

The program must be run by passing the following arguments on the command line:
- diagram type: 
`circle`(`circleDiagram`), `barChart`(`bar_chart`), `plot`, `polar`(`polarChart`), `tree`
- name of the file with the data for the chart
- The name of the png file for the diagram (optional, in case there is no name, the diagram will be placed in the file "Diagram.png")

The data in the file must be in the following format:
- fields must be separated by a comma.
- if a field contains commas, the field must be enclosed in double quotes.

Depending on the diagram, the content of the lines must be different: 
- Plot - coordinate, coordinate, name, additional information
- Tree - name, additional information, names of "children" separated by commas
- Others - name, value, additional information.

When you put the cursor over a column/point/sector, additional information is displayed. 
The chart can be dragged with the mouse or with the arrow keys on the keyboard. 
The graph can also be zoomed in and out with the mouse wheel.

The tests/ folder has examples of input data. Also in the comments before the main, arguments are prepared to run
program with this data.

## Examples

### Bar chart

![Bar chart](https://user-images.githubusercontent.com/77232868/183286099-2bed3b63-8a88-48d6-98cc-e6e43f977b64.png)

### Circle diagram

![image](https://user-images.githubusercontent.com/77232868/183286158-44139d3d-99ba-4685-9fbb-6b60acbec68a.png)


### Plot

![image](https://user-images.githubusercontent.com/77232868/183286168-220a6650-39d4-48c4-88e3-b4d75b6fdf16.png)

### Polar diagram

![image](https://user-images.githubusercontent.com/77232868/183286176-b44795ee-d1df-4085-bfc3-cdbbcf3893be.png)

### Tree diagram

![image](https://user-images.githubusercontent.com/77232868/183286185-7528ff55-7c50-4541-8274-4c8593c387e6.png)


## Testing

The tests/ folder has prepared examples of input and output data. By running the test class Automation, the corresponding files
output.png files, the results of the program's work will be displayed for the corresponding input data files. In the tests
CheckImagesNotChanged.kt file checks that the image of each diagram hasn't changed. If the image 
diagram has changed intentionally, the expected value must be updated (see documentation). For different operating systems 
systems may have different images.
