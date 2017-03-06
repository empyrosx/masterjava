package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    private static Boolean calculateRow(int[][] matrixA, int[][] matrixB, int[][] matrixC, int rowIndex) {
        int columnsCount = matrixA.length;
        for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
            int sum = 0;
            for (int k = 0; k < columnsCount; k++) {
                sum = sum + matrixA[columnIndex][k] * matrixB[rowIndex][k];
            }
            matrixC[columnIndex][rowIndex] = sum;
        }
        return true;
    }


    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {

        final int matrixSize = matrixA.length;

        int BT[][] = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }
        final int[][] matrixC = new int[matrixSize][matrixSize];


        CompletionService<Boolean> service = new ExecutorCompletionService<>(executor);
        List<Future> futures = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            int finalRowIndex = rowIndex;
            Callable<Boolean> callable = () -> calculateRow(matrixA, BT, matrixC, finalRowIndex);
            Future<Boolean> future = service.submit(callable);
            futures.add(future);
        }

        while (!futures.isEmpty()) {
            Future future = service.poll(1, TimeUnit.SECONDS);
            if (future != null) {
                if (future.isDone()) {
                    futures.remove(future);
                }
            }
        }

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int BT[][] = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * BT[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
