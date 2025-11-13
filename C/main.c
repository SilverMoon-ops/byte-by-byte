#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <math.h>
#include <time.h>
#include <unistd.h>
#include <stdatomic.h>

// Define M_PI manually if not provided by <math.h>
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define DEFAULT_DURATION 30 // seconds
#define DEFAULT_THREADS 4

atomic_int running = 1;
atomic_ullong total_ops = 0;

typedef struct {
    int id;
} ThreadArgs;

void* worker(void* arg) {
    ThreadArgs* t = (ThreadArgs*)arg;
    double x = t->id + 1;
    unsigned long long local_ops = 0;

    while (atomic_load(&running)) {
        // Heavy floating-point work to stress the CPU
        for (int i = 0; i < 100000; i++) {
            x = sin(x) * cos(x) + tan(x) * sqrt(fabs(x));
            x = fmod(x, M_PI);
            local_ops++;
        }

        // Add completed work to total (avoid too frequent atomic ops)
        atomic_fetch_add(&total_ops, local_ops);
        local_ops = 0;
    }

    return NULL;
}

int main(int argc, char* argv[]) {
    int threads = DEFAULT_THREADS;
    int duration = DEFAULT_DURATION;

    if (argc > 1) threads = atoi(argv[1]);
    if (argc > 2) duration = atoi(argv[2]);

    printf("🔥 CPU Blaster starting...\n");
    printf("Threads: %d | Duration: %d seconds\n", threads, duration);
    printf("Press Ctrl+C to stop early.\n\n");

    pthread_t* thread_ids = malloc(sizeof(pthread_t) * threads);
    ThreadArgs* args = malloc(sizeof(ThreadArgs) * threads);

    // Start threads
    for (int i = 0; i < threads; i++) {
        args[i].id = i;
        pthread_create(&thread_ids[i], NULL, worker, &args[i]);
    }

    // Sleep for given duration
    for (int i = 0; i < duration; i++) {
        sleep(1);
        unsigned long long ops = atomic_load(&total_ops);
        printf("⏱️ %2d sec | total operations: %llu\n", i + 1, ops);
    }

    // Stop workers
    atomic_store(&running, 0);
    printf("\n🛑 Stopping workers...\n");

    // Join threads
    for (int i = 0; i < threads; i++) {
        pthread_join(thread_ids[i], NULL);
    }

    unsigned long long ops = atomic_load(&total_ops);
    printf("✅ Done! Total operations performed: %llu\n", ops);

    free(thread_ids);

