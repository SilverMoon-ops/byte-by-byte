#include <stdio.h>
#include <math.h>

int main() {

    double radius = 0.0;
    double area = 0.0;
    const double PI = 3.14159;

    printf("Enter the radius of the circle: ");
    scanf("%lf", &radius);

    area = PI * pow(radius, 2); // math.h function to calculate power
    printf("The area of the circle with radius %.2f is %.2f\n", radius, area);


    






    return 0;
}