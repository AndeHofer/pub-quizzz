import {defineConfig} from 'vite';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
    root: 'src',
    publicDir: '../public',
    plugins: [
        tailwindcss(),
    ],
    build: {
        outDir: '../../resources/static',
        emptyOutDir: true,
        rollupOptions: {
            input: {
                main: './index.html',
                admin_main: './admin/admin_main.html',
                create_quiz: './admin/create_quiz.html',
                register_user: './admin/register_user.html',
                leaderboard: './leaderboard.html',
                team: './team.html',
                quizzes: './quizzes.html',
                quiz: './quiz.html'
            }
        }
    },
});
