import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
    root: 'src',
    publicDir: '../public',
    build: {
        outDir: '../dist',
        rollupOptions: {
            input: {
                main: resolve(__dirname, 'src/index.html'),
                trainings: resolve(__dirname, 'src/trainings/index.html'),
                'sign-in': resolve(__dirname, 'src/sign-in/index.html'),
                'sign-up': resolve(__dirname, 'src/sign-up/index.html'),
            },
        },
    },
});