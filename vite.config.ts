import { defineConfig } from 'vite';
import { resolve } from 'path';
import fg from 'fast-glob';

function getHtmlInputs() {
    const entries = fg.sync('src/pages/**/index.html');
    const input: Record<string, string> = {};

    entries.forEach((entry) => {
        const name = entry
            .replace(/^src\/pages\//, '') // убираем начальный путь
            .replace(/\/index\.html$/, ''); // убираем index.html

        input[name || 'main'] = resolve(__dirname, entry);
    });

    return input;
}

export default defineConfig({
    build: {
        rollupOptions: {
            input: getHtmlInputs()
        }
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://fit-quest.ru',
                changeOrigin: true,
            }
        },
    }
});
