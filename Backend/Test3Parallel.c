#include <omp.h>
#include <stdio.h>

void multiplyMatrices(int first[SIZE][SIZE], int second[SIZE][SIZE], int result[SIZE][SIZE]) {
    int i;
    #pragma omp parallel for num_threads(4)
    for (i = 0; i < 100; i++) {
        printf("%d ", i);
    }

    #pragma omp parallel for collapse(3) num_threads(4)
    for (int i = 0; i < SIZE; i++) {
        for (int j = 0; j < SIZE; j++) {
            for (int k = 0; k < SIZE; k++) {
                result[i][j] += first[i][k] * second[k][j];
            }
        }
    }

    #pragma omp parallel for num_threads(4)
    for (i = 0; i < 100; i++) {
        printf("%d ", i);
    }
}
