#include <stdio.h>
#include <math.h>

int main() {

    double radius = 0.0;
    double area = 0.0;
    double surface_area = 0.0;
    double volume = 0.0;
    const double PI = 3.14159;

    printf("Enter the radius of the circle: ");
    scanf("%lf", &radius);

    area = PI * pow(radius, 2); // math.h function to calculate power
    surface_area = 4 * PI * pow(radius, 2); // math.h function to calculate power
    volume = (4.0 / 3.0) * PI * pow(radius, 3); // math.h function to calculate power


    printf("The area of the circle with radius %.2lf is %.2lf\n", radius, area);
    printf("The surface area of the sphere with radius %.2lf is %.2lf\n", radius, surface_area);
    printf("The volume of the sphere with radius %.2lf is %.2lf\n", radius, volume);
    






    return 0;
}