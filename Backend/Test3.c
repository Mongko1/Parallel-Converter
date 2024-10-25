#include <stdio.h>

void multiplyMatrices(int first[SIZE][SIZE], int second[SIZE][SIZE], int result[SIZE][SIZE]) {
    int i;
    for (i = 0; i < 100; i++) {
        printf("%d ", i);
    }

    for (int i = 0; i < SIZE; i++) {
        for (int j = 0; j < SIZE; j++) {
            for (int k = 0; k < SIZE; k++) {
                result[i][j] += first[i][k] * second[k][j];
            }
        }
    }

    for (i = 0; i < 100; i++) {
        printf("%d ", i);
    }
}