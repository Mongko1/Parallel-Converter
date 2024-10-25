#include <omp.h>
#include <stdio.h>

int main() {
    int i;
    #pragma omp parallel for 
    for (i = 0; i < 100; i++) {
        printf("%d ", i);
    }
    return 0;
}
